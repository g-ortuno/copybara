/*
 * Copyright (C) 2023 Google Inc.
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

package com.google.copybara.rust;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.copybara.exception.ValidationException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/** Represents a Cargo version requirement */
abstract class RustVersionRequirement {
  public static RustVersionRequirement getVersionRequirement(String requirement)
      throws ValidationException {
    // TODO(chriscampos): Support additional types of version requirements
    return DefaultRustVersionRequirement.create(requirement);
  }

  /**
   * Given a semantic version string, returns true if the version fulfills this version requirement.
   *
   * @param version The semantic version string.
   * @return A boolean indicating if the version fulfills this version requirement.
   * @throws ValidationException If there is an issue parsing the version string.
   */
  public abstract boolean fulfills(String version) throws ValidationException;

  @AutoValue
  abstract static class SemanticVersion implements Comparable<SemanticVersion> {
    private static final Pattern VALID_VERSION_PATTERN =
        Pattern.compile("^[0-9]+(\\.[0-9]+)?(\\.[0-9]+)?$");

    private static final Comparator<SemanticVersion> COMPARATOR =
        Comparator.comparing(SemanticVersion::majorVersion)
            .thenComparing(SemanticVersion::minorVersion)
            .thenComparing(SemanticVersion::patchVersion);

    public static SemanticVersion create(int majorVersion, int minorVersion, int patchVersion) {
      return new AutoValue_RustVersionRequirement_SemanticVersion(
          majorVersion, minorVersion, patchVersion);
    }

    public static SemanticVersion createFromVersionString(String version)
        throws ValidationException {
      ValidationException.checkCondition(
          VALID_VERSION_PATTERN.matcher(version).matches(),
          String.format("The string %s is not a valid Rust semantic version.", version));

      List<String> versionParts = Splitter.on(".").splitToList(version);

      int majorVersion = Integer.parseInt(versionParts.get(0));
      int minorVersion = versionParts.size() >= 2 ? Integer.parseInt(versionParts.get(1)) : 0;
      int patchVersion = versionParts.size() >= 3 ? Integer.parseInt(versionParts.get(2)) : 0;

      return new AutoValue_RustVersionRequirement_SemanticVersion(
          majorVersion, minorVersion, patchVersion);
    }

    @Override
    public int compareTo(SemanticVersion other) {
      return COMPARATOR.compare(this, other);
    }

    public abstract int majorVersion();

    public abstract int minorVersion();

    public abstract int patchVersion();
  }
}
