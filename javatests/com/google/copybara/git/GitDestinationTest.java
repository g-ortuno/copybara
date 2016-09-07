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

import com.google.copybara.ValidationException;
  private Glob destinationFiles;
    destinationFiles = new Glob(ImmutableList.of("**"));
  public void errorIfUrlMissing() throws ValidationException {
  public void errorIfFetchMissing() throws ValidationException {
  public void errorIfPushMissing() throws ValidationException {
      throws ValidationException {
  private GitDestination destination() throws ValidationException {
      throws ValidationException {
  private void process(Destination.Writer writer, DummyReference originRef)
      throws ValidationException, RepoException, IOException {
    processWithBaseline(writer, originRef, /*baseline=*/ null);
  private void processWithBaseline(Destination.Writer writer, DummyReference originRef,
      throws RepoException, ValidationException, IOException {
    processWithBaselineAndConfirmation(writer, originRef, baseline,
  private void processWithBaselineAndConfirmation(Destination.Writer writer,
      throws ValidationException, RepoException, IOException {
    TransformResult result = TransformResults.of(workdir, originRef);
    WriterResult destinationResult = writer.write(result, console);
    process(
        destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("origin_ref"));
    processWithBaselineAndConfirmation(destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("origin_ref"),
    processWithBaselineAndConfirmation(
        destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("origin_ref1"),
    processWithBaselineAndConfirmation(
        destination().newWriter(destinationFiles),
        new DummyReference("origin_ref2"),
    processWithBaselineAndConfirmation(destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("origin_ref"),
    process(destinationFirstCommit().newWriter(destinationFiles), ref);
    process(destination().newWriter(destinationFiles), ref);
  }

  @Test
  public void processEmptyCommitWithExcludes() throws Exception {
    fetch = "master";
    push = "master";
    Files.write(workdir.resolve("excluded"), "some content".getBytes());
    repo().withWorkTree(workdir).simpleCommand("add", "excluded");
    repo().withWorkTree(workdir).simpleCommand("commit", "-m", "first commit");

    Files.delete(workdir.resolve("excluded"));

    destinationFiles = new Glob(ImmutableList.of("**"), ImmutableList.of("excluded"));
    thrown.expect(EmptyChangeException.class);
    thrown.expectMessage("empty change");
    process(destination().newWriter(destinationFiles), new DummyReference("origin_ref"));
    process(destination().newWriter(destinationFiles), new DummyReference("origin_ref"));
    process(destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("origin_ref"));
    process(destination().newWriter(destinationFiles), new DummyReference("origin_ref"));
  @Test
  public void doNotDeleteIncludedFilesInNonMatchingSubdir() throws Exception {
    fetch = "master";
    push = "master";

    Files.createDirectories(workdir.resolve("foo"));
    Files.write(workdir.resolve("foo/bar"), "content".getBytes(UTF_8));
    repo().withWorkTree(workdir).simpleCommand("add", "foo/bar");
    repo().withWorkTree(workdir).simpleCommand("commit", "-m", "message");

    Files.write(workdir.resolve("foo/baz"), "content".getBytes(UTF_8));

    // Note the glob foo/** does not match the directory itself called 'foo',
    // only the contents.
    destinationFiles = new Glob(ImmutableList.of("foo/**"));
    process(destination().newWriter(destinationFiles),
        new DummyReference("origin_ref"));

    GitTesting.assertThatCheckout(repo(), "master")
        .containsFile("foo/bar", "content")
        .containsFile("foo/baz", "content")
        .containsNoMoreFiles();
  }

    Destination.Writer writer = destinationFirstCommit().newWriter(destinationFiles);
    assertThat(writer.getPreviousRef(DummyOrigin.LABEL_NAME)).isNull();
    process(writer, new DummyReference("first_commit"));
    writer = destination().newWriter(destinationFiles);
    assertThat(writer.getPreviousRef(DummyOrigin.LABEL_NAME)).isEqualTo("first_commit");
    process(writer, new DummyReference("second_commit"));
    writer = destination().newWriter(destinationFiles);
    assertThat(writer.getPreviousRef(DummyOrigin.LABEL_NAME)).isEqualTo("second_commit");
    process(writer, new DummyReference("third_commit"));
    process(destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("first_commit"));
    assertThat(destination().newWriter(destinationFiles).getPreviousRef(DummyOrigin.LABEL_NAME))
    process(destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("first_commit"));
    destination().newWriter(destinationFiles).getPreviousRef(DummyOrigin.LABEL_NAME);
    process(destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("first_commit").withTimestamp(1414141414));
    process(destination().newWriter(destinationFiles),
        new DummyReference("second_commit").withTimestamp(1515151515));
    process(destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("first_commit").withTimestamp(1414141414));
    process(destination().newWriter(destinationFiles),
        new DummyReference("second_commit").withTimestamp(1414141490));
    process(destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("first_commit").withTimestamp(1414141414));
    process(destination().newWriter(destinationFiles),
        new DummyReference("second_commit").withTimestamp(1414141490));
    process(destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("first_commit"));
    process(destinationFirstCommit().newWriter(destinationFiles),
        new DummyReference("first_commit"));
    process(destinationFirstCommit().newWriter(destinationFiles), firstCommit);
    destinationFiles = new Glob(ImmutableList.of("**"), ImmutableList.of("excluded.txt"));
    process(destination().newWriter(destinationFiles), new DummyReference("ref"));
    destinationFiles = new Glob(ImmutableList.of("**"), ImmutableList.of("**/HEAD"));
    process(destination().newWriter(destinationFiles), new DummyReference("ref"));
    process(destinationFirstCommit().newWriter(destinationFiles), ref);
    process(destination().newWriter(destinationFiles), ref);
    processWithBaseline(destination().newWriter(destinationFiles), ref, firstCommit);
    process(destinationFirstCommit().newWriter(destinationFiles), ref);
    process(destination().newWriter(destinationFiles), ref);
    processWithBaseline(destination().newWriter(destinationFiles), ref, firstCommit);
    process(destinationFirstCommit().newWriter(destinationFiles), ref);
    process(destination().newWriter(destinationFiles), ref);
    processWithBaseline(destination().newWriter(destinationFiles), ref, firstCommit);
    Destination.Writer writer = destinationFirstCommit().newWriter(destinationFiles);
    WriterResult result =
        writer.write(TransformResults.of(workdir, new DummyReference("ref1")), console);