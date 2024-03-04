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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Constants {
    public static final String  RELEASES_URI                           = "https://github.com/HanSolo/JDKMon/releases/latest";
    public static final String  MAJOR_VERSIONS_URI                     = "https://api.foojay.io/disco/v3.0/major_versions/?ga=true&include_build=false";
    public static final String  JDKMON_RELEASES_URI                    = "https://github.com/HanSolo/JDKMon/releases";
    public static final String  JEP_BASE_URL                           = "https://openjdk.org/jeps/0";
    public static final String  JEP_URL                                = "https://openjdk.org/jeps/";
    public static final String  JSR_BASE_URL                           = "https://jcp.org/en/jsr/all";
    public static final String  JSR_URL                                = "https://jcp.org/en/jsr/detail?id=";
    public static final String  OPENJDK_PROJECT_URL                    = "https://openjdk.org/projects/";

    public static final long    INITIAL_DELAY_IN_SECONDS               = 30;
    public static final long    RESCAN_INTERVAL_IN_SECONDS             = 3600;
    public static final long    INITIAL_CVE_DELAY_IN_MINUTES           = 2;
    public static final long    CVE_UPDATE_INTERVAL_IN_MINUTES         = 60;
    public static final long    INITIAL_GRAALVM_CVE_DELAY_IN_MINUTES   = 3;
    public static final long    GRAALVM_CVE_UPDATE_INTERVAL_IN_MINUTES = 60;
    public static final long    INITIAL_PKG_DOWNLOAD_DELAY_IN_MINUTES  = 5;
    public static final long    UPDATE_PKGS_INTERVAL_IN_MINUTES        = 60;
    public static final long    INITIAL_JEP_TASK_DELAY                 = 13;
    public static final long    INITIAL_JSR_TASK_DELAY                 = 18;
    public static final long    INITIAL_PROJECT_TASK_DELAY             = 21;

    public static final Pattern JEP_PATTERN                            = Pattern.compile("(class=\"jep\">)([0-9]{1,7})(<\\/td><td><a href=\"[0-9]{1,7}\">)([0-9a-zA-Z\\-\\s&;\\(\\)]+)");
    public static final Matcher JEP_MATCHER                            = JEP_PATTERN.matcher("");

    public static final Pattern JSR_PATTERN                            = Pattern.compile("(\\/en\\/jsr\\/detail\\?id=)([0-9]{1,5})(\">)([0-9a-zA-Z,'\\(\\)\\-\s]+)");
    public static final Matcher JSR_MATCHER                            = JSR_PATTERN.matcher("");
    public static final Pattern JSR_TM_PATTERN                         = Pattern.compile("(<sup><font size=\"-2\">TM<\\/font><\\/sup>)|(<font size=\"-2\"><sup>TM<\\/sup><\\/font>)");
    public static final Matcher JSR_TM_MATCHER                         = JSR_TM_PATTERN.matcher("");

    public static final Pattern PROJECT_PATTERN                        = Pattern.compile("href=\"\\/projects\\/([a-zA-Z0-9_\\-]+)\">([a-zA-Z0-9_\\:\\.\\-\\(\\)]+)<");
    public static final Matcher PROJECT_MATCHER                        = PROJECT_PATTERN.matcher("");

    public static final Pattern JAVA_PATH_PATTERN                      = Pattern.compile("^(.*/java)\\s");
    public static final Pattern JAVA_PATH_PATTERN_WIN                  = Pattern.compile("^(.*\\\\java\\.exe)\\s");

    public static final String  OS_NAME_PROPERTY                       = System.getProperty("os.name");
    public static final String  OS_ARCH_PROPERTY                       = System.getProperty("os.arch");

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

    public static Map<String,String> HTML_ENTITIES = new ConcurrentHashMap<>(){{
        // XML predefined entities:
        put("&quot;", "\"");
        put("&amp;" , "&");
        put("&apos;", "'");
        put("&lt;"  , "<");
        put("&gt;"  , ">");
        // HTML character entity references:
        put("&nbsp;", "\u00A0");
        put("&iexcl;" , "\u00A1");
        put("&cent;", "\u00A2");
        put("&pound;" , "\u00A3");
        put("&curren;", "\u00A4");
        put("&yen;" , "\u00A5");
        put("&brvbar;", "\u00A6");
        put("&sect;", "\u00A7");
        put("&uml;" , "\u00A8");
        put("&copy;", "\u00A9");
        put("&ordf;", "\u00AA");
        put("&laquo;" , "\u00AB");
        put("&not;" , "\u00AC");
        put("&shy;" , "\u00AD");
        put("&reg;" , "\u00AE");
        put("&macr;", "\u00AF");
        put("&deg;" , "\u00B0");
        put("&plusmn;", "\u00B1");
        put("&sup2;", "\u00B2");
        put("&sup3;", "\u00B3");
        put("&acute;" , "\u00B4");
        put("&micro;" , "\u00B5");
        put("&para;", "\u00B6");
        put("&middot;", "\u00B7");
        put("&cedil;" , "\u00B8");
        put("&sup1;", "\u00B9");
        put("&ordm;", "\u00BA");
        put("&raquo;" , "\u00BB");
        put("&frac14;", "\u00BC");
        put("&frac12;", "\u00BD");
        put("&frac34;", "\u00BE");
        put("&iquest;", "\u00BF");
        put("&Agrave;", "\u00C0");
        put("&Aacute;", "\u00C1");
        put("&Acirc;" , "\u00C2");
        put("&Atilde;", "\u00C3");
        put("&Auml;", "\u00C4");
        put("&Aring;" , "\u00C5");
        put("&AElig;" , "\u00C6");
        put("&Ccedil;", "\u00C7");
        put("&Egrave;", "\u00C8");
        put("&Eacute;", "\u00C9");
        put("&Ecirc;" , "\u00CA");
        put("&Euml;", "\u00CB");
        put("&Igrave;", "\u00CC");
        put("&Iacute;", "\u00CD");
        put("&Icirc;" , "\u00CE");
        put("&Iuml;", "\u00CF");
        put("&ETH;", "\u00D0");
        put("&Ntilde;", "\u00D1");
        put("&Ograve;", "\u00D2");
        put("&Oacute;", "\u00D3");
        put("&Ocirc;", "\u00D4");
        put("&Otilde;", "\u00D5");
        put("&Ouml;", "\u00D6");
        put("&times;", "\u00D7");
        put("&Oslash;", "\u00D8");
        put("&Ugrave;", "\u00D9");
        put("&Uacute;", "\u00DA");
        put("&Ucirc;", "\u00DB");
        put("&Uuml;", "\u00DC");
        put("&Yacute;", "\u00DD");
        put("&THORN;", "\u00DE");
        put("&szlig;", "\u00DF");
        put("&agrave;", "\u00E0");
        put("&aacute;", "\u00E1");
        put("&acirc;", "\u00E2");
        put("&atilde;", "\u00E3");
        put("&auml;", "\u00E4");
        put("&aring;", "\u00E5");
        put("&aelig;", "\u00E6");
        put("&ccedil;", "\u00E7");
        put("&egrave;", "\u00E8");
        put("&eacute;", "\u00E9");
        put("&ecirc;", "\u00EA");
        put("&euml;", "\u00EB");
        put("&igrave;", "\u00EC");
        put("&iacute;", "\u00ED");
        put("&icirc;", "\u00EE");
        put("&iuml;", "\u00EF");
        put("&eth;", "\u00F0");
        put("&ntilde;", "\u00F1");
        put("&ograve;", "\u00F2");
        put("&oacute;", "\u00F3");
        put("&ocirc;", "\u00F4");
        put("&otilde;", "\u00F5");
        put("&ouml;", "\u00F6");
        put("&divide;", "\u00F7");
        put("&oslash;", "\u00F8");
        put("&ugrave;", "\u00F9");
        put("&uacute;", "\u00FA");
        put("&ucirc;", "\u00FB");
        put("&uuml;", "\u00FC");
        put("&yacute;", "\u00FD");
        put("&thorn;", "\u00FE");
        put("&yuml;", "\u00FF");
        put("&OElig;", "\u0152");
        put("&oelig;", "\u0153");
        put("&Scaron;", "\u0160");
        put("&scaron;", "\u0161");
        put("&Yuml;", "\u0178");
        put("&fnof;", "\u0192");
        put("&circ;", "\u02C6");
        put("&tilde;", "\u02DC");
        put("&Alpha;", "\u0391");
        put("&Beta;", "\u0392");
        put("&Gamma;", "\u0393");
        put("&Delta;", "\u0394");
        put("&Epsilon;", "\u0395");
        put("&Zeta;", "\u0396");
        put("&Eta;" , "\u0397");
        put("&Theta;", "\u0398");
        put("&Iota;", "\u0399");
        put("&Kappa;", "\u039A");
        put("&Lambda;", "\u039B");
        put("&Mu;", "\u039C");
        put("&Nu;", "\u039D");
        put("&Xi;", "\u039E");
        put("&Omicron;", "\u039F");
        put("&Pi;", "\u03A0");
        put("&Rho;", "\u03A1");
        put("&Sigma;", "\u03A3");
        put("&Tau;", "\u03A4");
        put("&Upsilon;", "\u03A5");
        put("&Phi;", "\u03A6");
        put("&Chi;", "\u03A7");
        put("&Psi;", "\u03A8");
        put("&Omega;", "\u03A9");
        put("&alpha;", "\u03B1");
        put("&beta;", "\u03B2");
        put("&gamma;", "\u03B3");
        put("&delta;", "\u03B4");
        put("&epsilon;", "\u03B5");
        put("&zeta;", "\u03B6");
        put("&eta;", "\u03B7");
        put("&theta;", "\u03B8");
        put("&iota;", "\u03B9");
        put("&kappa;", "\u03BA");
        put("&lambda;", "\u03BB");
        put("&mu;", "\u03BC");
        put("&nu;", "\u03BD");
        put("&xi;", "\u03BE");
        put("&omicron;", "\u03BF");
        put("&pi;", "\u03C0");
        put("&rho;", "\u03C1");
        put("&sigmaf;", "\u03C2");
        put("&sigma;", "\u03C3");
        put("&tau;", "\u03C4");
        put("&upsilon;", "\u03C5");
        put("&phi;", "\u03C6");
        put("&chi;", "\u03C7");
        put("&psi;", "\u03C8");
        put("&omega;", "\u03C9");
        put("&thetasym;", "\u03D1");
        put("&upsih;", "\u03D2");
        put("&piv;", "\u03D6");
        put("&ensp;", "\u2002");
        put("&emsp;", "\u2003");
        put("&thinsp;", "\u2009");
        put("&zwnj;", "\u200C");
        put("&zwj;", "\u200D");
        put("&lrm;", "\u200E");
        put("&rlm;", "\u200F");
        put("&ndash;", "\u2013");
        put("&mdash;", "\u2014");
        put("&lsquo;", "\u2018");
        put("&rsquo;", "\u2019");
        put("&sbquo;", "\u201A");
        put("&ldquo;", "\u201C");
        put("&rdquo;", "\u201D");
        put("&bdquo;", "\u201E");
        put("&dagger;", "\u2020");
        put("&Dagger;", "\u2021");
        put("&bull;", "\u2022");
        put("&hellip;", "\u2026");
        put("&permil;", "\u2030");
        put("&prime;", "\u2032");
        put("&Prime;", "\u2033");
        put("&lsaquo;", "\u2039");
        put("&rsaquo;", "\u203A");
        put("&oline;", "\u203E");
        put("&frasl;", "\u2044");
        put("&euro;", "\u20AC");
        put("&image;", "\u2111");
        put("&weierp;", "\u2118");
        put("&real;", "\u211C");
        put("&trade;", "\u2122");
        put("&alefsym;", "\u2135");
        put("&larr;", "\u2190");
        put("&uarr;", "\u2191");
        put("&rarr;", "\u2192");
        put("&darr;", "\u2193");
        put("&harr;", "\u2194");
        put("&crarr;", "\u21B5");
        put("&lArr;", "\u21D0");
        put("&uArr;", "\u21D1");
        put("&rArr;", "\u21D2");
        put("&dArr;", "\u21D3");
        put("&hArr;", "\u21D4");
        put("&forall;", "\u2200");
        put("&part;", "\u2202");
        put("&exist;", "\u2203");
        put("&empty;", "\u2205");
        put("&nabla;", "\u2207");
        put("&isin;", "\u2208");
        put("&notin;", "\u2209");
        put("&ni;", "\u220B");
        put("&prod;", "\u220F");
        put("&sum;", "\u2211");
        put("&minus;", "\u2212");
        put("&lowast;", "\u2217");
        put("&radic;", "\u221A");
        put("&prop;", "\u221D");
        put("&infin;", "\u221E");
        put("&ang;", "\u2220");
        put("&and;", "\u2227");
        put("&or;", "\u2228");
        put("&cap;", "\u2229");
        put("&cup;", "\u222A");
        put("&int;", "\u222B");
        put("&there4;", "\u2234");
        put("&sim;", "\u223C");
        put("&cong;", "\u2245");
        put("&asymp;", "\u2248");
        put("&ne;", "\u2260");
        put("&equiv;", "\u2261");
        put("&le;", "\u2264");
        put("&ge;", "\u2265");
        put("&sub;", "\u2282");
        put("&sup;", "\u2283");
        put("&nsub;", "\u2284");
        put("&sube;", "\u2286");
        put("&supe;", "\u2287");
        put("&oplus;", "\u2295");
        put("&otimes;", "\u2297");
        put("&perp;", "\u22A5");
        put("&sdot;", "\u22C5");
        put("&lceil;", "\u2308");
        put("&rceil;", "\u2309");
        put("&lfloor;", "\u230A");
        put("&rfloor;", "\u230B");
        put("&lang;", "\u2329");
        put("&rang;", "\u232A");
        put("&loz;", "\u25CA");
        put("&spades;", "\u2660");
        put("&clubs;", "\u2663");
        put("&hearts;", "\u2665");
        put("&diams;", "\u2666");
    }};
}
