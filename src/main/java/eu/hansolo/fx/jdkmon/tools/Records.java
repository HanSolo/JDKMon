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

import io.foojay.api.discoclient.pkg.VersionNumber;
import io.foojay.api.discoclient.util.OutputFormat;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.foojay.api.discoclient.util.Constants.COLON;
import static io.foojay.api.discoclient.util.Constants.COMMA;
import static io.foojay.api.discoclient.util.Constants.CURLY_BRACKET_CLOSE;
import static io.foojay.api.discoclient.util.Constants.CURLY_BRACKET_OPEN;
import static io.foojay.api.discoclient.util.Constants.QUOTES;


public class Records {

    public record CVE(String id, double score, Severity severity, List<VersionNumber> affectedVersions) implements Comparable<CVE> {
        public static final String FIELD_ID                = "id";
        public static final String FIELD_SCORE             = "score";
        public static final String FIELD_SEVERITY          = "severity";
        public static final String FIELD_URL               = "url";
        public static final String FIELD_AFFECTED_VERSIONS = "affected_versions";

        public String url() { return "http://cve.mitre.org/cgi-bin/cvename.cgi?name=" + id; }

        @Override public String toString() {
            return new StringBuilder().append(CURLY_BRACKET_OPEN)
                                      .append(QUOTES).append(FIELD_ID).append(QUOTES).append(COLON).append(QUOTES).append(id).append(QUOTES).append(COMMA)
                                      .append(QUOTES).append(FIELD_SCORE).append(QUOTES).append(COLON).append(score).append(COMMA)
                                      .append(QUOTES).append(FIELD_SEVERITY).append(QUOTES).append(COLON).append(QUOTES).append(severity.name()).append(QUOTES).append(COMMA)
                                      .append(QUOTES).append(FIELD_URL).append(QUOTES).append(COLON).append(QUOTES).append(url()).append(QUOTES).append(COMMA)
                                      .append(QUOTES).append(FIELD_AFFECTED_VERSIONS).append(QUOTES).append(COLON)
                                      .append(affectedVersions.stream().map(version -> version.toString(OutputFormat.REDUCED_COMPRESSED, true, false)).collect(Collectors.joining("\",\"", "[\"", "\"]")))
                                      .append(CURLY_BRACKET_CLOSE).toString();
        }

        @Override public boolean equals(final Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            CVE cve2 = (CVE) o;
            return Double.compare(cve2.score, score) == 0 && id.equals(cve2.id);
        }
        @Override public int hashCode() {
            return Objects.hash(id, score);
        }

        @Override public int compareTo(final CVE other) { return id.compareTo(other.id()); }
    }
}
