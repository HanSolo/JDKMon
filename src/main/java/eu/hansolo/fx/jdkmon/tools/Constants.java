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

import java.io.File;
import java.util.regex.Pattern;


public class Constants {
    public static final String  RELEASES_URI                          = "https://github.com/HanSolo/JDKMon/releases/latest";
    public static final long    INITIAL_DELAY_IN_MINUTES              = 5;
    public static final long    RESCAN_INTERVAL_IN_MINUTES            = 60;
    public static final long    INITIAL_CVE_DELAY_IN_MINUTES          = 5;
    public static final long    CVE_UPDATE_INTERVAL_IN_MINUTES        = 360;
    public static final long    INITIAL_PKG_DOWNLOAD_DELAY_IN_MINUTES = 5;
    public static final long    UPDATE_PKGS_INTERVAL_IN_MINUTES       = 60;

    public static final long    INITIAL_CHECK_DELAY_IN_SECONDS        = 10;

    public static final long    CHECK_INTERVAL_IN_SECONDS             = 60;
    public static final Pattern POSITIVE_INTEGER_PATTERN              = Pattern.compile("\\d+");
    public static final String  HOME_FOLDER                           = new StringBuilder(System.getProperty("user.home")).append(File.separator).toString();
    public static final String  OPENJFX_MAVEN_METADATA                = "https://repo1.maven.org/maven2/org/openjfx/javafx/maven-metadata.xml";
    public static final String  JAVAFX_RUNTIME_VERSION                = "javafx.runtime.version";
    public static final String  ALL_PKGS_MINIMIZED_URI                = "https://api.foojay.io/disco/v3.0/packages/all_builds_of_openjdk?downloadable=true&include_ea=true&minimized=true";

    public static final String  TEST_CONNECTIVITY_URL                 = "https://api.foojay.io";
}
