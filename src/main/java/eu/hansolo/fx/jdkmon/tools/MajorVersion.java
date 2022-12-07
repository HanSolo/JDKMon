/*
 * Copyright (c) 2021 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.jdkmon.tools;


import eu.hansolo.jdktools.TermOfSupport;
import eu.hansolo.jdktools.versioning.VersionNumber;

import java.util.Comparator;
import java.util.Objects;


public class MajorVersion implements Comparable<MajorVersion> {
    public  static final String        FIELD_MAJOR_VERSION   = "major_version";
    public  static final String        FIELD_TERM_OF_SUPPORT = "term_of_support";
    public  static final String        FIELD_MAINTAINED      = "maintained";
    public  static final String        FIELD_VERSIONS        = "versions";
    private        final int           majorVersion;
    private        final TermOfSupport termOfSupport;
    private        final boolean       maintained;


    public MajorVersion(final int majorVersion) {
        this(majorVersion, Helper.getTermOfSupport(majorVersion));
    }
    public MajorVersion(final int majorVersion, final TermOfSupport termOfSupport) {
        if (majorVersion <= 0) { throw new IllegalArgumentException("Major version cannot be <= 0"); }
        this.majorVersion  = majorVersion;
        this.termOfSupport = termOfSupport;
        this.maintained = false;
    }


    public int getAsInt() { return majorVersion; }

    public TermOfSupport getTermOfSupport() { return termOfSupport; }

    // VersionNumber
    public VersionNumber getVersionNumber() { return new VersionNumber(majorVersion); }

    // Maintained
    public Boolean isMaintained() { return maintained; }

    @Override public int compareTo(final MajorVersion majorVersion) {
        return Integer.compare(majorVersion.getAsInt(), getAsInt());
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) { return true; }
        if (obj == null || getClass() != obj.getClass()) { return false; }
        MajorVersion other = (MajorVersion) obj;
        return majorVersion == other.majorVersion && maintained == other.maintained;
    }

    @Override public int hashCode() {
        return Objects.hash(majorVersion, maintained);
    }
}
