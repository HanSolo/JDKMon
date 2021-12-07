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

import eu.hansolo.fx.jdkmon.Main.SemVerUri;
import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.Architecture;
import io.foojay.api.discoclient.pkg.LibCType;
import io.foojay.api.discoclient.pkg.OperatingSystem;
import io.foojay.api.discoclient.pkg.Pkg;
import io.foojay.api.discoclient.pkg.SemVer;
import io.foojay.api.discoclient.pkg.VersionNumber;
import io.foojay.api.discoclient.util.OutputFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Finder {
    public static final  String                                        MACOS_JAVA_INSTALL_PATH   = "/System/Volumes/Data/Library/Java/JavaVirtualMachines/";
    public static final  String                                        WINDOWS_JAVA_INSTALL_PATH = "C:\\Program Files\\Java\\";
    public static final  String                                        LINUX_JAVA_INSTALL_PATH   = "/usr/lib/jvm";
    private static final Pattern                                       GRAALVM_VERSION_PATTERN   = Pattern.compile("(.*graalvm\\s)(.*)(\\s\\(.*)");
    private static final Matcher                                       GRAALVM_VERSION_MATCHER   = GRAALVM_VERSION_PATTERN.matcher("");
    private static final Pattern                                       ZULU_BUILD_PATTERN        = Pattern.compile("\\((build\\s)(.*)\\)");
    private static final Matcher                                       ZULU_BUILD_MATCHER        = ZULU_BUILD_PATTERN.matcher("");
    private static final String[]                                      MAC_JAVA_HOME_CMDS        = { "/bin/sh", "-c", "echo $JAVA_HOME" };
    private static final String[]                                      LINUX_JAVA_HOME_CMDS      = { "/usr/bin/sh", "-c", "echo $JAVA_HOME" };
    private static final String[]                                      WIN_JAVA_HOME_CMDS        = { "cmd.exe", "/c", "echo %JAVA_HOME%" };
    private static final String[]                                      DETECT_ALPINE_CMDS        = { "/bin/sh", "-c", "cat /etc/os-release | grep 'NAME=' | grep -ic 'Alpine'" };
    private static final String[]                                      UX_DETECT_ARCH_CMDS       = { "/bin/sh", "-c", "uname -m" };
    private static final String[]                                      WIN_DETECT_ARCH_CMDS      = { "cmd.exe", "/c", "SET Processor" };
    private static final Pattern                                       ARCHITECTURE_PATTERN      = Pattern.compile("(PROCESSOR_ARCHITECTURE)=([a-zA-Z0-9_\\-]+)");
    private static final Matcher                                       ARCHITECTURE_MATCHER      = ARCHITECTURE_PATTERN.matcher("");
    private              ExecutorService                               service                   = Executors.newSingleThreadExecutor();
    private              Properties                                    releaseProperties         = new Properties();
    private              io.foojay.api.discoclient.pkg.OperatingSystem operatingSystem           = detectOperatingSystem();
    private              Architecture                                  architecture              = detectArchitecture();
    private              String                                        javaFile                  = OperatingSystem.WINDOWS == operatingSystem ? "java.exe" : "java";
    private              String                                        javaHome                  = "";
    private              String                                        javafxPropertiesFile      = "javafx.properties";
    private              boolean                                       isAlpine                  = false;
    private              DiscoClient                                   discoclient;


    public Finder() {
        this(new DiscoClient("JDKMon"));
    }
    public Finder(final DiscoClient discoclient) {
        this.discoclient = discoclient;
        getJavaHome();
        if (this.javaHome.isEmpty()) { this.javaHome = System.getProperties().get("java.home").toString(); }
        checkIfAlpineLinux();
    }


    public Set<Distribution> getDistributions(final List<String> searchPaths) {
        Set<Distribution> distros = new HashSet<>();
        if (null == searchPaths || searchPaths.isEmpty()) { return distros; }

        if (service.isShutdown()) {
            service = Executors.newSingleThreadExecutor();
        }

        searchPaths.forEach(searchPath -> {
            final Path       path      = Paths.get(searchPath);
            final List<Path> javaFiles = findByFileName(path, javaFile);
            javaFiles.stream().filter(java -> !java.toString().contains("jre")).forEach(java -> checkForDistribution(java.toString(), distros));
        });
        service.shutdown();
        try {
            service.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return distros;
    }

    public Map<Distribution, List<Pkg>> getAvailableUpdates(final List<Distribution> distributions) {
        Map<Distribution, List<Pkg>>  distrosToUpdate = new ConcurrentHashMap<>();
        //List<CompletableFuture<Void>> updateFutures   = Collections.synchronizedList(new ArrayList<>());
        //distributions.forEach(distribution -> updateFutures.add(discoclient.updateAvailableForAsync(DiscoClient.getDistributionFromText(distribution.getApiString()), SemVer.fromText(distribution.getVersion()).getSemVer1(), Architecture.fromText(distribution.getArchitecture()), distribution.getFxBundled(), null).thenAccept(pkgs -> distrosToUpdate.put(distribution, pkgs))));
        //CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[updateFutures.size()])).join();

        distributions.forEach(distribution -> {
            List<Pkg> availableUpdates = discoclient.updateAvailableFor(DiscoClient.getDistributionFromText(distribution.getApiString()), SemVer.fromText(distribution.getVersion()).getSemVer1(), operatingSystem, Architecture.fromText(distribution.getArchitecture()), distribution.getFxBundled(), null, distribution.getFeature());
            if (null != availableUpdates) {
                distrosToUpdate.put(distribution, availableUpdates);
            }

            if (OperatingSystem.ALPINE_LINUX == operatingSystem) {
                availableUpdates = availableUpdates.stream().filter(pkg -> pkg.getLibCType() == LibCType.MUSL).collect(Collectors.toList());
            } else if (OperatingSystem.LINUX == operatingSystem) {
                availableUpdates = availableUpdates.stream().filter(pkg -> pkg.getLibCType() != LibCType.MUSL).collect(Collectors.toList());
            }
            if (Architecture.NOT_FOUND != architecture) {
                availableUpdates = availableUpdates.stream().filter(pkg -> pkg.getArchitecture() == architecture).collect(Collectors.toList());
            }

            distrosToUpdate.put(distribution, availableUpdates);
        });

        // Check if there are newer versions from other distributions
        distrosToUpdate.entrySet()
                       .stream()
                       .filter(entry -> !entry.getKey().getApiString().startsWith("graal"))
                       .filter(entry -> !entry.getKey().getApiString().equals("mandrel"))
                       .filter(entry -> !entry.getKey().getApiString().equals("liberica_native"))
                       .forEach(entry -> {
            if (entry.getValue().isEmpty()) {
                Distribution distro = entry.getKey();
                entry.setValue(discoclient.updateAvailableFor(null, SemVer.fromText(distro.getVersion()).getSemVer1(), Architecture.fromText(distro.getArchitecture()), distro.getFxBundled()));
            }
        });

        LinkedHashMap<Distribution, List < Pkg >> sorted = new LinkedHashMap<>();
        distrosToUpdate.entrySet()
                       .stream()
                       .sorted(Map.Entry.comparingByKey(Comparator.comparing(Distribution::getName)))
                       .forEachOrdered(entry -> sorted.put(entry.getKey(), entry.getValue()));

        return sorted;
    }

    public OperatingSystem getOperatingSystem() { return operatingSystem; }

    public Architecture getArchitecture() { return architecture; }

    public static final OperatingSystem detectOperatingSystem() {
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            return OperatingSystem.WINDOWS;
        } else if (os.indexOf("mac") >= 0) {
            return OperatingSystem.MACOS;
        } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            try {
                final ProcessBuilder processBuilder = new ProcessBuilder(DETECT_ALPINE_CMDS);
                final Process        process        = processBuilder.start();
                final String         result         = new BufferedReader(new InputStreamReader(process.getInputStream())).lines().collect(Collectors.joining("\n"));
                return null == result ? OperatingSystem.LINUX : result.equals("1") ? OperatingSystem.ALPINE_LINUX : OperatingSystem.LINUX;
            } catch (IOException e) {
                e.printStackTrace();
                return OperatingSystem.LINUX;
            }
        } else if (os.indexOf("sunos") >= 0) {
            return OperatingSystem.SOLARIS;
        } else {
            return OperatingSystem.NOT_FOUND;
        }
    }

    public static Architecture detectArchitecture() {
        final OperatingSystem operatingSystem = detectOperatingSystem();
        try {
            final ProcessBuilder processBuilder = OperatingSystem.WINDOWS == operatingSystem ? new ProcessBuilder(WIN_DETECT_ARCH_CMDS) : new ProcessBuilder(UX_DETECT_ARCH_CMDS);
            final Process        process        = processBuilder.start();
            final String         result         = new BufferedReader(new InputStreamReader(process.getInputStream())).lines().collect(Collectors.joining("\n"));
            switch(operatingSystem) {
                case WINDOWS -> {
                    ARCHITECTURE_MATCHER.reset(result);
                    final List<MatchResult> results     = ARCHITECTURE_MATCHER.results().collect(Collectors.toList());
                    final int               noOfResults = results.size();
                    if (noOfResults > 0) {
                        final MatchResult   res = results.get(0);
                        return Architecture.fromText(res.group(2));
                    } else {
                        return Architecture.NOT_FOUND;
                    }
                }
                case MACOS, LINUX -> {
                    return Architecture.fromText(result);
                }
            }

            // If not found yet try via system property
            final String arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
            if (arch.contains("sparc")) return Architecture.SPARC;
            if (arch.contains("amd64") || arch.contains("86_64")) return Architecture.AMD64;
            if (arch.contains("86")) return Architecture.X86;
            if (arch.contains("s390x")) return Architecture.S390X;
            if (arch.contains("ppc64")) return Architecture.PPC64;
            if (arch.contains("arm") && arch.contains("64")) return Architecture.AARCH64;
            if (arch.contains("arm")) return Architecture.ARM;
            if (arch.contains("aarch64")) return Architecture.AARCH64;
            return Architecture.NOT_FOUND;
        } catch (IOException e) {
            e.printStackTrace();
            return Architecture.NOT_FOUND;
        }
    }

    private List<Path> findByFileName(final Path path, final String fileName) {
        List<Path> result;
        try (Stream<Path> pathStream = Files.find(path, Integer.MAX_VALUE, (p, basicFileAttributes) -> {
                                                      // if directory or no-read permission, ignore
                                                      if(Files.isDirectory(p) || !Files.isReadable(p)) { return false; }
                                                      return p.getFileName().toString().equalsIgnoreCase(fileName);
                                                  })
        ) {
            result = pathStream.collect(Collectors.toList());
        } catch (IOException e) {
            result = new ArrayList<>();
        }
        return result;
    }

    private void checkForDistribution(final String java, final Set<Distribution> distros) {
        AtomicBoolean inUse = new AtomicBoolean(false);

        try {
            List<String> commands = new ArrayList<>();
            commands.add(java);
            commands.add("-version");

            final String fileSeparator = File.separator;
            final String binFolder     = new StringBuilder(fileSeparator).append("bin").append(fileSeparator).append(".*").toString();

            ProcessBuilder builder  = new ProcessBuilder(commands).redirectErrorStream(true);
            Process        process  = builder.start();
            Streamer streamer = new Streamer(process.getInputStream(), d -> {
                final String parentPath       = OperatingSystem.WINDOWS == operatingSystem ? java.replaceAll("bin\\\\java.exe", "") : java.replaceAll(binFolder, fileSeparator);
                final File   releaseFile      = new File(parentPath + "release");
                String[]     lines            = d.split("\\|");
                String       name             = "Unknown build of OpenJDK";
                String       apiString        = "";
                String       operatingSystem  = "";
                String       architecture     = "";
                String       feature          = "";
                Boolean      fxBundled        = Boolean.FALSE;
                //FPU          fpu              = FPU.UNKNOWN;

                if (!this.javaHome.isEmpty() && !inUse.get() && parentPath.contains(javaHome)) {
                    inUse.set(true);
                }

                final File   jreLibExtFolder  = new File(new StringBuilder(parentPath).append("jre").append(fileSeparator).append("lib").append(fileSeparator).append("ext").toString());
                if (jreLibExtFolder.exists()) {
                    fxBundled = Stream.of(jreLibExtFolder.listFiles()).filter(file -> !file.isDirectory()).map(File::getName).collect(Collectors.toSet()).stream().filter(filename -> filename.equalsIgnoreCase("jfxrt.jar")).count() > 0;
                }
                final File   jmodsFolder      = new File(new StringBuilder(parentPath).append("jmods").toString());
                if (jmodsFolder.exists()) {
                    fxBundled = Stream.of(jmodsFolder.listFiles()).filter(file -> !file.isDirectory()).map(File::getName).collect(Collectors.toSet()).stream().filter(filename -> filename.startsWith("javafx")).count() > 0;
                }

                VersionNumber version = null;

                String        line1         = lines[0];
                String        line2         = lines[1];
                String        withoutPrefix = line1;
                if (line1.startsWith("openjdk")) {
                    withoutPrefix = line1.replaceFirst("openjdk version", "");
                } else if (line1.startsWith("java")) {
                    withoutPrefix = line1.replaceFirst("java version", "");
                    name          = "Oracle";
                    apiString     = "oracle";
                }
                if (line2.contains("Zulu")) {
                    name      = "Zulu";
                    apiString = "zulu";
                    ZULU_BUILD_MATCHER.reset(line2);
                    final List<MatchResult> results = ZULU_BUILD_MATCHER.results().collect(Collectors.toList());
                    if (!results.isEmpty()) {
                        MatchResult result = results.get(0);
                        version = VersionNumber.fromText(result.group(2));
                    }
                } else if (line2.contains("Semeru")) {
                    if (line2.contains("Certified")) {
                        name      = "Semeru certified";
                        apiString = "semeru_certified";
                    } else {
                        name      = "Semeru";
                        apiString = "semeru";
                    }
                } else if (line2.contains("Tencent")) {
                    name      = "Kona";
                    apiString = "kona";
                } else if (line2.contains("Bisheng")) {
                    name      = "Bishenq";
                    apiString = "bisheng";
                }

                if (null == version) { version = VersionNumber.fromText(withoutPrefix.substring(withoutPrefix.indexOf("\"") + 1, withoutPrefix.lastIndexOf("\""))); }
                VersionNumber graalVersion = version;

                if (releaseFile.exists()) {
                    try (FileInputStream propFile = new FileInputStream(releaseFile)) {
                        releaseProperties.load(propFile);
                    } catch (IOException ex) {
                        System.out.println("Error reading release properties file. " + ex);
                    }
                    if (!releaseProperties.isEmpty()) {
                        if (releaseProperties.containsKey("IMPLEMENTOR") && name.equals("Unknown build of OpenJDK")) {
                            switch(releaseProperties.getProperty("IMPLEMENTOR").replaceAll("\"", "")) {
                                case "AdoptOpenJDK"      : name = "Adopt OpenJDK";  apiString = "aoj";            break;
                                case "Alibaba"           : name = "Dragonwell";     apiString = "dragonwell";     break;
                                case "Amazon.com Inc."   : name = "Corretto";       apiString = "corretto";       break;
                                case "Azul Systems, Inc.": name = "Zulu";           apiString = "zulu";           break;
                                case "mandrel"           : name = "Mandrel";        apiString = "mandrel";        break;
                                case "Microsoft"         : name = "Microsoft";      apiString = "microsoft";      break;
                                case "ojdkbuild"         : name = "OJDK Build";     apiString = "ojdk_build";     break;
                                case "Oracle Corporation": name = "Oracle OpenJDK"; apiString = "oracle_openjdk"; break;
                                case "Red Hat, Inc."     : name = "Red Hat";        apiString = "redhat";         break;
                                case "SAP SE"            : name = "SAP Machine";    apiString = "sap_machine";    break;
                                case "OpenLogic"         : name = "OpenLogic";      apiString = "openlogic";      break;
                                case "JetBrains s.r.o."  : name = "JetBrains";      apiString = "jetbrains";      break;
                                case "Eclipse Foundation": name = "Temurin";        apiString = "temurin";        break;
                                case "Tencent"           : name = "Kona";           apiString = "kona";           break;
                                case "Bisheng"           : name = "Bisheng";        apiString = "bisheng";        break;
                                case "Debian"            : name = "Debian";         apiString = "debian";         break;
                                case "N/A"               : /* GraalVM */ break;
                            }
                        }
                        if (releaseProperties.containsKey("OS_ARCH")) {
                            architecture = releaseProperties.getProperty("OS_ARCH").toLowerCase().replaceAll("\"", "");
                        }
                        if (releaseProperties.containsKey("JVM_VARIANT")) {
                            if (name == "Adopt OpenJDK") {
                                String jvmVariant = releaseProperties.getProperty("JVM_VARIANT").toLowerCase().replaceAll("\"", "");
                                if (jvmVariant.equals("dcevm")) {
                                    name      = "Trava OpenJDK";
                                    apiString = "trava";
                                } else if (jvmVariant.equals("openj9")) {
                                    name      = "Adopt OpenJDK J9";
                                    apiString = "aoj_openj9";
                                }
                            }
                        }
                        if (releaseProperties.containsKey("OS_NAME")) {
                            switch(releaseProperties.getProperty("OS_NAME").toLowerCase().replaceAll("\"", "")) {
                                case "darwin" : operatingSystem = "macos"; break;
                                case "linux"  : operatingSystem = "linux"; break;
                                case "windows": operatingSystem = "windows"; break;
                            }
                        }
                        if (releaseProperties.containsKey("MODULES") && !fxBundled) {
                            fxBundled = (releaseProperties.getProperty("MODULES").contains("javafx"));
                        }
                        /*
                        if (releaseProperties.containsKey("SUN_ARCH_ABI")) {
                            String abi = releaseProperties.get("SUN_ARCH_ABI").toString();
                            switch (abi) {
                                case "gnueabi"   -> fpu = FPU.SOFT_FLOAT;
                                case "gnueabihf" -> fpu = FPU.HARD_FLOAT;
                            }
                        }
                        */
                    }
                }


                if (lines.length > 2) {
                    String line3 = lines[2].toLowerCase();
                    if (!PropertyManager.INSTANCE.hasKey(PropertyManager.FEATURES)) {
                        PropertyManager.INSTANCE.setString(PropertyManager.FEATURES, "loom,panama,metropolis,valhalla");
                        PropertyManager.INSTANCE.storeProperties();
                    }

                    String[] features = PropertyManager.INSTANCE.getString(PropertyManager.FEATURES).split(",");
                    for (String feat : features) {
                            feat = feat.trim().toLowerCase();
                            if (line3.contains(feat)) {
                                feature = feat;
                                break;
                            }
                        }

                }

                if (name.equals("Unknown build of OpenJDK") && lines.length > 2) {
                    String line3      = lines[2].toLowerCase();
                    File   readmeFile = new File(parentPath + "readme.txt");
                    if (readmeFile.exists()) {
                        try {
                            List<String> readmeLines = Helper.readTextFileToList(readmeFile.getAbsolutePath());
                            if (readmeLines.stream().filter(l -> l.toLowerCase().contains("liberica native image kit")).count() > 0) {
                                name      = "Liberica Native";
                                apiString = "liberica_native";
                                GRAALVM_VERSION_MATCHER.reset(line3);
                                final List<MatchResult> results = GRAALVM_VERSION_MATCHER.results().collect(Collectors.toList());
                                if (!results.isEmpty()) {
                                    MatchResult result = results.get(0);
                                    version = VersionNumber.fromText(result.group(2));
                                }
                            } else if (readmeLines.stream().filter(l -> l.toLowerCase().contains("liberica")).count() > 0) {
                                name      = "Liberica";
                                apiString = "liberica";
                            }
                        } catch (IOException e) {

                        }
                    } else {
                        if (line3.contains("graalvm")) {
                            name      = "GraalVM";
                            apiString = graalVersion.getMajorVersion().getAsInt() >= 8 ? "graalvm_ce" + graalVersion.getMajorVersion().getAsInt() : "";

                            GRAALVM_VERSION_MATCHER.reset(line3);
                            final List<MatchResult> results = GRAALVM_VERSION_MATCHER.results().collect(Collectors.toList());
                            if (!results.isEmpty()) {
                                MatchResult result = results.get(0);
                                version = VersionNumber.fromText(result.group(2));
                            }
                        } else if (line3.contains("microsoft")) {
                            name      = "Microsoft";
                            apiString = "microsoft";
                        } else if (line3.contains("corretto")) {
                            name      = "Corretto";
                            apiString = "corretto";
                        } else if (line3.contains("temurin")) {
                            name      = "Temurin";
                            apiString = "temurin";
                        }
                    }
                }

                if (architecture.isEmpty()) { architecture = this.architecture.name().toLowerCase(); }

                Distribution distributionFound = new Distribution(name, apiString, version.toString(OutputFormat.REDUCED_COMPRESSED, true, true), operatingSystem, architecture, fxBundled, parentPath, feature);
                if (inUse.get()) { distributionFound.setInUse(true); }

                distros.add(distributionFound);
            });
            service.submit(streamer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkIfAlpineLinux() {
        if (OperatingSystem.WINDOWS == operatingSystem || OperatingSystem.MACOS == operatingSystem) { return; }
        try {
            Process p      = Runtime.getRuntime().exec(DETECT_ALPINE_CMDS);
            String  result = new BufferedReader(new InputStreamReader(p.getInputStream())).lines().collect(Collectors.joining("\n"));
            this.isAlpine  = null == result ? false : result.equals("1");
            if (this.isAlpine) { this.operatingSystem = OperatingSystem.ALPINE_LINUX; }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getJavaHome() {
        try {
            ProcessBuilder processBuilder = OperatingSystem.WINDOWS == operatingSystem ? new ProcessBuilder(WIN_JAVA_HOME_CMDS) : OperatingSystem.MACOS == operatingSystem ? new ProcessBuilder(MAC_JAVA_HOME_CMDS) : new ProcessBuilder(LINUX_JAVA_HOME_CMDS);
            Process        process        = processBuilder.start();
            Streamer       streamer       = new Streamer(process.getInputStream(), d -> this.javaHome = d);
            service.submit(streamer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<SemVer, SemVerUri> checkForJavaFXUpdates(final List<String> javafxSearchPaths) {
        // Find the javafx sdk folders starting at the folder given by JAVAFX_SEARCH_PATH
        if (null == javafxSearchPaths || javafxSearchPaths.isEmpty()) { return new HashMap<>(); }

        Set<String> searchPaths = new HashSet<>();
        javafxSearchPaths.forEach(searchPath -> {
            try {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(searchPath))) {
                    for (Path path : stream) {
                        if (Files.isDirectory(path)) {
                            String folderName = path.getFileName().toString();
                            if (folderName.toLowerCase().startsWith("javafx")) {
                                searchPaths.add(String.join(File.separator, searchPath, folderName, "lib"));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Check every update found for validity and only return the ones that are valid
        Map<SemVer, SemVerUri> validUpdatesFound  = new HashMap<>();
        Map<SemVer, String> javafxUpdatesFound = findJavaFX(searchPaths);
        javafxUpdatesFound.entrySet().forEach(entry -> {
            validUpdatesFound.put(entry.getKey(), checkForJavaFXUpdate(entry.getKey()));
        });
        return validUpdatesFound;
    }

    private Map<SemVer, String> findJavaFX(final Set<String> searchPaths) {
        Map<SemVer, String> versionsFound = new HashMap<>();
        searchPaths.forEach(searchPath -> {
            final String javafxPropertiesFilePath = new StringBuilder(searchPath).append(File.separator).append(javafxPropertiesFile).toString();
            Path path = Paths.get(javafxPropertiesFilePath);
            if (!Files.exists(path)) { return; }

            Properties javafxPropertes = new Properties();
            try (FileInputStream javafxPropertiesFileIS = new FileInputStream(javafxPropertiesFilePath)) {
                javafxPropertes.load(javafxPropertiesFileIS);
                String runtimeVersion = javafxPropertes.getProperty(Constants.JAVAFX_RUNTIME_VERSION, "");
                if (!runtimeVersion.isEmpty()) {
                    SemVer versionFound = SemVer.fromText(runtimeVersion).getSemVer1();
                    versionsFound.put(versionFound, javafxPropertiesFilePath);
                }
            } catch (IOException ex) {
                System.out.println("Error reading javafx properties file. " + ex);
            }
        });
        return versionsFound;
    }

    private SemVerUri checkForJavaFXUpdate(final SemVer versionToCheck) {
       List<SemVer> openjfxVersions         = getAvailableOpenJfxVersions();
       List<SemVer> filteredOpenjfxVersions = openjfxVersions.stream()
                                                             .filter(semver -> semver.getFeature() == versionToCheck.getFeature())
                                                             .sorted(Comparator.comparing(SemVer::getVersionNumber).reversed())
                                                             .collect(Collectors.toList());

       if (!filteredOpenjfxVersions.isEmpty()) {
           SemVer latestVersion = filteredOpenjfxVersions.get(0);
           OperatingSystem operatingSystem = getOperatingSystem();
           Architecture    architecture    = Detector.getArchitecture();
           if (latestVersion.greaterThan(versionToCheck)) {
               StringBuilder linkBuilder = new StringBuilder();
               linkBuilder.append("https://download2.gluonhq.com/openjfx/").append(latestVersion.getFeature());
               if (latestVersion.getUpdate() != 0) { linkBuilder.append(".").append(latestVersion.getInterim()).append(".").append(latestVersion.getUpdate()); }
               linkBuilder.append("/openjfx-").append(latestVersion.toString(true)).append("_");
               switch(operatingSystem) {
                   case WINDOWS -> linkBuilder.append("windows");
                   case LINUX   -> linkBuilder.append("linux");
                   case MACOS   -> linkBuilder.append("osx");
                   default      -> { return new SemVerUri(latestVersion, ""); }
               }
               linkBuilder.append("-");
               switch(architecture) {
                   case X86            -> linkBuilder.append("x86");
                   case X64            -> linkBuilder.append("x64");
                   case AARCH64, ARM64 -> linkBuilder.append("aarch64");
                   case AARCH32, ARM   -> linkBuilder.append("arm32");
                   default             -> { return new SemVerUri(latestVersion, ""); }
               }
               linkBuilder.append("_bin-sdk.zip");
               final String uri = linkBuilder.toString();
               return new SemVerUri(latestVersion, Helper.isUriValid(uri) ? uri : "");
           } else {
               return new SemVerUri(latestVersion, "");
           }
       } else {
           return new SemVerUri(versionToCheck, "");
       }
    }

    public List<SemVer> getAvailableOpenJfxVersions() {
        List<SemVer> availableOpenJfxVersions = new ArrayList<>();
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            final DocumentBuilder db  = dbf.newDocumentBuilder();
            final Document        doc = db.parse(Constants.OPENJFX_MAVEN_METADATA);
            doc.getDocumentElement().normalize();
            final NodeList list = doc.getElementsByTagName("version");
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                availableOpenJfxVersions.add(SemVer.fromText(node.getTextContent()).getSemVer1());
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return availableOpenJfxVersions;
    }

    private class Streamer implements Runnable {
        private InputStream      inputStream;
        private Consumer<String> consumer;

        public Streamer(final InputStream inputStream, final Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer    = consumer;
        }

        @Override public void run() {
            final StringBuilder builder = new StringBuilder();
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(line -> builder.append(line).append("|"));
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
            consumer.accept(builder.toString());
        }
    }
}
