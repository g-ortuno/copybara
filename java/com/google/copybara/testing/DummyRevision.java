/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.copybara.testing;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.copybara.Change;
import com.google.copybara.LabelFinder;
import com.google.copybara.Origin;
import com.google.copybara.RepoException;
import com.google.copybara.Revision;
import com.google.copybara.authoring.Author;
import com.google.copybara.authoring.Authoring;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * A reference of a change used for testing. This can be used with a {@link DummyOrigin} instance or
 * without an actual {@link Origin} implementation.
 */
public class DummyRevision implements Revision {

  private static final Author DEFAULT_AUTHOR = new Author("Dummy Author", "no-reply@dummy.com");

  private final String reference;
  private final String message;
  private final Author author;
  final Path changesBase;
  private final Instant timestamp;
  @Nullable
  private final String contextReference;
  private final ImmutableMap<String, String> referenceLabels;
  private final boolean matchesGlob;
  @Nullable private final Path previousPath;
  private final ImmutableMap<String, String> descriptionLabels;

  public DummyRevision(String reference) {
    this(reference, "DummyReference message", DEFAULT_AUTHOR,
        Paths.get("/DummyReference", reference), /*timestamp=*/null,
        /*contextReference=*/ null, /*referenceLabels=*/ ImmutableMap.of(),
         /*matchesGlob=*/true, /*previousPath=*/null);
  }

  DummyRevision(
      String reference, String message, Author author, Path changesBase,
      @Nullable Instant timestamp, @Nullable String contextReference,
      ImmutableMap<String, String> referenceLabels, boolean matchesGlob,
      @Nullable Path previousPath) {
    this.reference = Preconditions.checkNotNull(reference);
    this.message = Preconditions.checkNotNull(message);
    this.author = Preconditions.checkNotNull(author);
    this.changesBase = Preconditions.checkNotNull(changesBase);
    this.timestamp = timestamp;
    this.contextReference = contextReference;
    this.referenceLabels = Preconditions.checkNotNull(referenceLabels);
    this.matchesGlob = matchesGlob;
    this.previousPath = previousPath;

    ImmutableMap.Builder<String, String> labels = ImmutableMap.builder();
    for (String line : message.split("\n")) {
      LabelFinder labelFinder = new LabelFinder(line);
      if (labelFinder.isLabel()) {
        labels.put(labelFinder.getName(), labelFinder.getValue());
      }
    }
    this.descriptionLabels = labels.build();
  }

  /**
   * Returns an instance equivalent to this one but with the timestamp set to the specified value.
   */
  public DummyRevision withTimestamp(Instant newTimestamp) {
    return new DummyRevision(
        this.reference, this.message, this.author, this.changesBase, newTimestamp,
        this.contextReference, this.referenceLabels, this.matchesGlob, this.previousPath);
  }

  public DummyRevision withAuthor(Author newAuthor) {
    return new DummyRevision(
        this.reference, this.message, newAuthor, this.changesBase, this.timestamp,
        this.contextReference, this.referenceLabels, this.matchesGlob, this.previousPath);
  }

  public DummyRevision withContextReference(String contextReference) {
    Preconditions.checkNotNull(contextReference);
    return new DummyRevision(
        this.reference, this.message, this.getAuthor(), this.changesBase, this.timestamp,
        contextReference, this.referenceLabels, this.matchesGlob, this.previousPath);
  }

  @Nullable
  @Override
  public Instant readTimestamp() throws RepoException {
    return timestamp;
  }

  @Override
  public String asString() {
    return reference;
  }

  @Override
  public String getLabelName() {
    return DummyOrigin.LABEL_NAME;
  }

  Change<DummyRevision> toChange(Authoring authoring) {
    Author safeAuthor = authoring.useAuthor(this.author.getEmail())
        ? this.author
        : authoring.getDefaultAuthor();
    return new Change<>(this, safeAuthor, message,
        ZonedDateTime.ofInstant(timestamp, ZoneId.systemDefault()), descriptionLabels,
                        computeChangedFiles());
  }

  private Set<String> computeChangedFiles() {
    Map<String, String> pathToContent = readAllFiles(changesBase);
    Map<String, String> previousContent = previousPath == null
        ? ImmutableMap.of()
        : readAllFiles(previousPath);

    MapDifference<String, String> diff = Maps.difference(pathToContent, previousContent);

    return ImmutableSet.<String>builder()
        .addAll(diff.entriesOnlyOnLeft().keySet())
        .addAll(diff.entriesOnlyOnRight().keySet())
        .addAll(diff.entriesDiffering().keySet())
        .build();
  }

  private static Map<String, String> readAllFiles(Path basePath) {
    Map<String, String> pathToContent = new HashMap<>();
    try {
      Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          pathToContent.put(basePath.relativize(file).toString(),
                            new String(Files.readAllBytes(file), UTF_8));
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      throw new RuntimeException("Shouldn't happen", e);
    }
    return pathToContent;
  }

  @Nullable
  @Override
  public String contextReference() {
    return contextReference;
  }

  @Override
  public ImmutableMap<String, String> associatedLabels() {
    return referenceLabels;
  }

  public Author getAuthor() {
    return author;
  }

  public boolean matchesGlob() {
    return matchesGlob;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("reference", reference)
        .add("message", message)
        .add("author", author)
        .add("changesBase", changesBase)
        .add("timestamp", timestamp)
        .add("descriptionLabels", descriptionLabels)
        .toString();
  }
}