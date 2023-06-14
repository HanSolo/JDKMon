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

import eu.hansolo.jdktools.scopes.BuildScope;
import eu.hansolo.jdktools.scopes.Scope;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


public class Constants {
    public static final String  RELEASES_URI                           = "https://github.com/HanSolo/JDKMon/releases/latest";
    public static final long    INITIAL_DELAY_IN_SECONDS               = 30;
    public static final long    RESCAN_INTERVAL_IN_SECONDS             = 3600;
    public static final long    INITIAL_CVE_DELAY_IN_MINUTES           = 2;
    public static final long    CVE_UPDATE_INTERVAL_IN_MINUTES         = 60;
    public static final long    INITIAL_GRAALVM_CVE_DELAY_IN_MINUTES   = 3;
    public static final long    GRAALVM_CVE_UPDATE_INTERVAL_IN_MINUTES = 60;
    public static final long    INITIAL_PKG_DOWNLOAD_DELAY_IN_MINUTES  = 5;
    public static final long    UPDATE_PKGS_INTERVAL_IN_MINUTES        = 60;

    public static final long    INITIAL_CHECK_DELAY_IN_SECONDS         = 5;

    public static final long    CHECK_INTERVAL_IN_SECONDS              = 60;
    public static final Pattern POSITIVE_INTEGER_PATTERN               = Pattern.compile("\\d+");
    public static final String  HOME_FOLDER                            = new StringBuilder(System.getProperty("user.home")).append(File.separator).toString();
    public static final String  OPENJFX_MAVEN_METADATA                 = "https://repo1.maven.org/maven2/org/openjfx/javafx/maven-metadata.xml";
    public static final String  JAVAFX_RUNTIME_VERSION                 = "javafx.runtime.version";
    public static final String  ALL_JDK_PKGS_MINIMIZED_URI             = "https://api.foojay.io/disco/v3.0/packages/all_builds_of_openjdk?downloadable=true&include_ea=true&minimized=true";
    public static final String  ALL_GRAAL_PKGS_MINIMIZED_URI           = "https://api.foojay.io/disco/v3.0/packages/all_builds_of_graalvm?downloadable=true&include_ea=true&minimized=true";

    public static final String  TEST_CONNECTIVITY_URL                  = "https://api.foojay.io";

    public static final String  UNKNOWN_BUILD_OF_OPENJDK               = "Unknown build of OpenJDK";

    public static final String  FEATURES                               = "loom,panama,metropolis,valhalla,lanai,kona_fiber,crac"; // comma separated list of available features

    public static final ConcurrentHashMap<String, BuildScope> SCOPE_LOOKUP = new ConcurrentHashMap<>() {{
        // Builds of OpenJDK
        put("aoj", BuildScope.BUILD_OF_OPEN_JDK);
        put("aoj_openj9", BuildScope.BUILD_OF_OPEN_JDK);
        put("bisheng", BuildScope.BUILD_OF_OPEN_JDK);
        put("corretto", BuildScope.BUILD_OF_OPEN_JDK);
        put("debian", BuildScope.BUILD_OF_OPEN_JDK);
        put("dragonwell", BuildScope.BUILD_OF_OPEN_JDK);
        put("liberica", BuildScope.BUILD_OF_OPEN_JDK);
        put("jetbrains", BuildScope.BUILD_OF_OPEN_JDK);
        put("kona", BuildScope.BUILD_OF_OPEN_JDK);
        put("microsoft", BuildScope.BUILD_OF_OPEN_JDK);
        put("ojdk_build", BuildScope.BUILD_OF_OPEN_JDK);
        put("open_logic", BuildScope.BUILD_OF_OPEN_JDK);
        put("oracle", BuildScope.BUILD_OF_OPEN_JDK);
        put("oracle_open_jdk", BuildScope.BUILD_OF_OPEN_JDK);
        put("red_hat", BuildScope.BUILD_OF_OPEN_JDK);
        put("sap_machine", BuildScope.BUILD_OF_OPEN_JDK);
        put("semeru", BuildScope.BUILD_OF_OPEN_JDK);
        put("semeru_certified", BuildScope.BUILD_OF_OPEN_JDK);
        put("temurin", BuildScope.BUILD_OF_OPEN_JDK);
        put("trava", BuildScope.BUILD_OF_OPEN_JDK);
        put("ubuntu", BuildScope.BUILD_OF_OPEN_JDK);
        put("zulu", BuildScope.BUILD_OF_OPEN_JDK);
        put("zulu_prime", BuildScope.BUILD_OF_OPEN_JDK);
        // Builds of GraalVM
        put("graalvm_ce8", BuildScope.BUILD_OF_GRAALVM);
        put("graalvm_ce11", BuildScope.BUILD_OF_GRAALVM);
        put("graalvm_ce16", BuildScope.BUILD_OF_GRAALVM);
        put("graalvm_ce17", BuildScope.BUILD_OF_GRAALVM);
        put("graalvm_ce19", BuildScope.BUILD_OF_GRAALVM);
        put("graalvm_ce20", BuildScope.BUILD_OF_GRAALVM);
        put("graalvm_community", BuildScope.BUILD_OF_GRAALVM);
        put("graalvm", BuildScope.BUILD_OF_GRAALVM);
        put("liberica_native", BuildScope.BUILD_OF_GRAALVM);
        put("mandrel", BuildScope.BUILD_OF_GRAALVM);
        put("gluon_graalvm", BuildScope.BUILD_OF_GRAALVM);
    }};
    
    public static final ConcurrentHashMap<Scope, List<String>> REVERSE_SCOPE_LOOKUP = new ConcurrentHashMap<>() {{
        put(BuildScope.BUILD_OF_OPEN_JDK, List.of("aoj", "aoj_openj9", "bisheng", "corretto", "debian", "dragonwell", "jetbrains", "kona", "liberica", "microsoft", "ojdk_build", "open_logic", "oracle", "oracle_open_jdk", "red_hat", "sap_machine", "semeru", "semeru_certified", "temurin", "trava", "ubuntu", "zulu", "zulu_prime"));
        put(BuildScope.BUILD_OF_GRAALVM, List.of("graalvm_ce8", "graalvm_ce11", "graalvm_ce16", "graalvm_ce17", "graalvm_ce19", "graalvm_ce21", "liberica_native", "mandrel", "gluon_graalvm"));
    }};

}
