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

import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.Architecture;
import io.foojay.api.discoclient.pkg.OperatingSystem;
import io.foojay.api.discoclient.pkg.Pkg;
import io.foojay.api.discoclient.pkg.SemVer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Finder {
    public  static final String          MACOS_JAVA_INSTALL_PATH   = "/System/Volumes/Data/Library/Java/JavaVirtualMachines/";
    public  static final String          WINDOWS_JAVA_INSTALL_PATH = "C:\\Program Files\\Java\\";
    public  static final String          LINUX_JAVA_INSTALL_PATH   = "/usr/lib/jvm";
    private static final Pattern         GRAALVM_VERSION_PATTERN   = Pattern.compile("(.*graalvm\\s)(.*)(\\s\\(.*)");
    private static final Matcher         GRAALVM_VERSION_MATCHER   = GRAALVM_VERSION_PATTERN.matcher("");
    private              ExecutorService service                   = Executors.newSingleThreadExecutor();
    private              Properties      releaseProperties         = new Properties();
    private              DiscoClient     discoclient               = new DiscoClient();
    private              String          javaFile                  = OperatingSystem.WINDOWS == DiscoClient.getOperatingSystem() ? "java.exe" : "java";


    public Set<Distribution> getDistributions(final String SEARCH_PATH) {
        Set<Distribution> distros = new HashSet<>();

        if (service.isShutdown()) {
            service = Executors.newSingleThreadExecutor();
        }

        final Path       path      = Paths.get(SEARCH_PATH);
        final List<Path> javaFiles = findByFileName(path, javaFile);
        javaFiles.stream().filter(java -> !java.toString().contains("jre")).forEach(java -> checkForDistribution(java.toString(), distros));

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
        List<CompletableFuture<Void>> updateFutures   = Collections.synchronizedList(new ArrayList<>());
        distributions.forEach(distribution -> updateFutures.add(discoclient.updateAvailableForAsync(DiscoClient.getDistributionFromText(distribution.getApiString()), SemVer.fromText(distribution.getVersion()).getSemVer1(), Architecture.fromText(distribution.getArchitecture()), distribution.getFxBundled()).thenAccept(pkgs -> distrosToUpdate.put(distribution, pkgs))));
        CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[updateFutures.size()])).join();

        // Check if there are newer versions from other distributions
        List<CompletableFuture<Void>> pkgFutures = Collections.synchronizedList(new ArrayList<>());
        distrosToUpdate.entrySet()
                       .stream()
                       .filter(entry -> !entry.getKey().getApiString().startsWith("graal"))
                       .filter(entry -> !entry.getKey().getApiString().equals("mandrel"))
                       .filter(entry -> !entry.getKey().getApiString().equals("liberica_native"))
                       .forEach(entry -> {
            if (entry.getValue().isEmpty()) {
                Distribution distro = entry.getKey();
                //pkgFutures.add(discoClient.updateAvailableForAsync(io.foojay.api.discoclient.pkg.Distribution.NONE, SemVer.fromText(distro.getVersion()).getSemVer1(), Architecture.fromText(distro.getArchitecture()), distro.getFxBundled()).thenAccept(l -> entry.setValue(l)));
                entry.setValue(discoclient.updateAvailableForAsync(null, SemVer.fromText(distro.getVersion()).getSemVer1(), Architecture.fromText(distro.getArchitecture()), distro.getFxBundled()).join());
            }
        });
        CompletableFuture.allOf(pkgFutures.toArray(new CompletableFuture[pkgFutures.size()])).join();

        LinkedHashMap<Distribution, List < Pkg >> sorted = new LinkedHashMap<>();
        distrosToUpdate.entrySet()
                       .stream()
                       .sorted(Map.Entry.comparingByKey(Comparator.comparing(Distribution::getName)))
                       .forEachOrdered(entry -> sorted.put(entry.getKey(), entry.getValue()));

        return sorted;
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
        try {
            List<String> commands = new ArrayList<>();
            commands.add(java);
            commands.add("-version");

            final String fileSeparator = File.separator;
            final String binFolder     = new StringBuilder(fileSeparator).append("bin").append(fileSeparator).append(".*").toString();

            ProcessBuilder builder  = new ProcessBuilder(commands).redirectErrorStream(true);
            Process        process  = builder.start();
            Streamer       streamer = new Streamer(process.getInputStream(), d -> {
                final String parentPath    = java.replaceAll(binFolder, fileSeparator);
                final File   releaseFile   = new File(parentPath + "release");

                String[]      lines         = d.split("\\|");

                String        name            = "Unknown build of OpenJDK";
                String        apiString       = "";
                String        operatingSystem = "";
                String        architecture    = "";
                Boolean       fxBundled       = Boolean.FALSE;

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
                }

                VersionNumber version      = VersionNumber.fromText(withoutPrefix.substring(withoutPrefix.indexOf("\"") + 1, withoutPrefix.lastIndexOf("\"")));
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
                        if ((name.equals("Zulu") || name.equals("Unknown build of OpenJDK")) && releaseProperties.containsKey("MODULES")) {
                            fxBundled = (releaseProperties.getProperty("MODULES").contains("javafx"));
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
                            switch (graalVersion.getMajorVersion().getAsInt()) {
                                case 8 : apiString = "graalvm_ce8"; break;
                                case 11: apiString = "graalvm_ce11"; break;
                                case 16: apiString = "graalvm_ce16"; break;
                                case 17: apiString = "graalvm_ce17"; break;
                                case 18: apiString = "graalvm_ce18"; break;
                                default: apiString = "";
                            }
                            GRAALVM_VERSION_MATCHER.reset(line3);
                            final List<MatchResult> results = GRAALVM_VERSION_MATCHER.results().collect(Collectors.toList());
                            if (!results.isEmpty()) {
                                MatchResult result = results.get(0);
                                version = VersionNumber.fromText(result.group(2));
                            }
                        } else if (line3.contains("microsoft")) {
                            name = "Microsoft";
                        }
                    }
                }

                distros.add(new Distribution(name, apiString, version.toString(OutputFormat.REDUCED_COMPRESSED, true, true), operatingSystem, architecture, fxBundled));
            });
            service.submit(streamer);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            builder.setLength(builder.length() - 1);
            consumer.accept(builder.toString());
        }
    }
}
