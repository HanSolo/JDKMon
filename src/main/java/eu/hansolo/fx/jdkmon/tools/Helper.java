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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.hansolo.cvescanner.Constants.CVE;
import eu.hansolo.fx.jdkmon.Main;
import eu.hansolo.fx.jdkmon.tools.Detector.MacosAccentColor;
import eu.hansolo.fx.jdkmon.tools.Records.JDKMonUpdate;
import eu.hansolo.fx.jdkmon.tools.Records.JDKUpdate;
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.TermOfSupport;
import eu.hansolo.jdktools.util.OutputFormat;
import eu.hansolo.jdktools.versioning.VersionNumber;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static eu.hansolo.jdktools.Constants.NEW_LINE;
import static java.nio.charset.StandardCharsets.UTF_8;


public class Helper {
    private static HttpClient httpClient;

    public static double clamp(final double min, final double max, final double value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public static TermOfSupport getTermOfSupport(final VersionNumber versionNumber) {
        if (!versionNumber.getFeature().isPresent() || versionNumber.getFeature().isEmpty()) {
            throw new IllegalArgumentException("VersionNumber need to have a feature version");
        }
        return getTermOfSupport(versionNumber.getFeature().getAsInt());
    }
    public static TermOfSupport getTermOfSupport(final int featureVersion) {
        if (featureVersion < 1) { throw new IllegalArgumentException("Feature version number cannot be smaller than 1"); }
        if (isLTS(featureVersion)) {
            return TermOfSupport.LTS;
        } else if (isMTS(featureVersion)) {
            return TermOfSupport.MTS;
        } else if (isSTS(featureVersion)) {
            return TermOfSupport.STS;
        } else {
            return TermOfSupport.NOT_FOUND;
        }
    }

    public static boolean isSTS(final int featureVersion) {
        if (featureVersion < 9) { return false; }
        switch(featureVersion) {
            case 9 :
            case 10: return true;
            default: return !isLTS(featureVersion);
        }
    }

    public static boolean isMTS(final int featureVersion) {
        if (featureVersion < 13 && featureVersion > 15) { return false; }
        return (!isLTS(featureVersion)) && featureVersion % 2 != 0;
    }

    public static boolean isLTS(final int featureVersion) {
        if (featureVersion < 1) { throw new IllegalArgumentException("Feature version number cannot be smaller than 1"); }
        if (featureVersion <= 8) { return true; }
        if (featureVersion < 11) { return false; }
        return ((featureVersion - 11.0) / 6.0) % 1 == 0;
    }

    public static boolean isPositiveInteger(final String text) {
        if (null == text || text.isEmpty()) { return false; }
        return Constants.POSITIVE_INTEGER_PATTERN.matcher(text).matches();
    }

    public static List<String> readTextFileToList(final String filename) throws IOException {
        final Path           path   = Paths.get(filename);
        final BufferedReader reader = Files.newBufferedReader(path);
        return reader.lines().collect(Collectors.toList());
    }

    public static final void saveToTextFileToUserFolder(final String filename, final String text) {
        if (null == text || text.isEmpty()) { return; }

        final File existingFile = new File(filename);
        if (existingFile.exists()) { existingFile.delete(); }

        try {
            Files.write(Paths.get(Constants.HOME_FOLDER + filename), text.getBytes());
        } catch (IOException e) {
            System.out.println("Error writing text file: " + filename);
        }
    }

    public static final void createJdkSwitcherScript(final OperatingSystem operatingSystem, final Set<Distro> distros) {
        final StringBuilder builder = new StringBuilder();
        switch(operatingSystem) {
            case WINDOWS -> {
                builder.append("@echo off").append(NEW_LINE)
                       .append("setlocal EnableDelayedExpansion").append(NEW_LINE)
                       .append(NEW_LINE)
                       .append("if \"%~1\"==\"\" (").append(NEW_LINE)
                       .append("  echo \"Missing JDK_NAME parameter\"").append(NEW_LINE)
                       .append(") else if \"%~1\"==\"-h\" (").append(NEW_LINE)
                       .append("  echo .\\switch-jdk.bat JDK_NAME").append(NEW_LINE)
                       .append("  echo.").append(NEW_LINE)
                       .append("  echo JDK_NAME ca be one of the following:").append(NEW_LINE);
                distros.forEach(distro -> builder.append("  echo ").append(distro.getApiString().toLowerCase()).append("_")
                                                 .append(distro.getVersionNumber().getFeature().getAsInt()).append("_").append(distro.getVersionNumber().getInterim().orElse(0)).append("_").append(distro.getVersionNumber().getUpdate().orElse(0)).append("_").append(distro.getVersionNumber().getPatch().orElse(0)).append(NEW_LINE));

                distros.forEach(distro -> {
                    builder.append(") else if \"%~1\"==\"").append(distro.getApiString().toLowerCase()).append("_")
                           .append(distro.getVersionNumber().getFeature().getAsInt()).append("_").append(distro.getVersionNumber().getInterim().orElse(0)).append("_").append(distro.getVersionNumber().getUpdate().orElse(0)).append("_").append(distro.getVersionNumber().getPatch().orElse(0)).append("\" (").append(NEW_LINE)
                           .append("  set NEW_JAVA_HOME=").append(distro.getPath()).append(NEW_LINE)
                           .append("  call :replaceInPath \"%JAVA_HOME%bin\\;\" \"!NEW_JAVA_HOME!bin\\;\"").append(NEW_LINE)
                           .append(NEW_LINE)
                           .append("  set JAVA_HOME=!NEW_JAVA_HOME!").append(NEW_LINE)
                           .append("  set JDK_HOME=!NEW_JAVA_HOME!").append(NEW_LINE)
                           .append(NEW_LINE)
                           .append("  if \"!PATH!\"==\"\" (").append(NEW_LINE)
                           .append("    set PATH=\"!JAVA_HOME!\\bin\"").append(NEW_LINE)
                           .append("  ) else (").append(NEW_LINE)
                           .append("    set PATH=\"!JAVA_HOME!\\bin;!PATH!\"").append(NEW_LINE)
                           .append("  )").append(NEW_LINE)
                           .append("  echo Switched to ").append(distro.getApiString().toLowerCase()).append("_")
                                                         .append(distro.getVersionNumber().getFeature().getAsInt()).append("_").append(distro.getVersionNumber().getInterim().orElse(0)).append("_").append(distro.getVersionNumber().getUpdate().orElse(0)).append("_").append(distro.getVersionNumber().getPatch().orElse(0)).append(NEW_LINE)
                           .append("  \"!JAVA_HOME!\\bin\\java\" -version").append(NEW_LINE);
                });
                builder.append(") else (").append(NEW_LINE)
                       .append("  echo JDK_NAME not found").append(NEW_LINE)
                       .append(")").append(NEW_LINE)
                       .append(NEW_LINE)
                       .append("exit  /B %ERROR_LEVEL%")
                       .append(NEW_LINE)
                       .append(NEW_LINE)
                       .append(":replaceInPath").append(NEW_LINE)
                       .append("set ARG1=%~1").append(NEW_LINE)
                       .append("set ARG2=%~2").append(NEW_LINE)
                       .append("call set PATH=%%PATH:%ARG1%=%ARG2%%%").append(NEW_LINE)
                       .append("exit /B 0").append(NEW_LINE);
                saveToTextFileToUserFolder("switch-jdk.bat", builder.toString());
            }
            case LINUX, LINUX_MUSL, ALPINE_LINUX, MACOS -> {
                builder.append("#!/bin/sh").append(NEW_LINE)
                       .append(NEW_LINE)
                       .append("# To switch to specific JDK you need to call the script as follows:").append(NEW_LINE)
                       .append("# . /switch-jdk.sh JDK_NAME").append(NEW_LINE)
                       .append("#").append(NEW_LINE)
                       .append("# JDK_NAME can be one of the following:").append(NEW_LINE);
                distros.forEach(distro -> builder.append("# ").append(distro.getApiString().toLowerCase()).append("_")
                                                 .append(distro.getVersionNumber().getFeature().getAsInt()).append("_").append(distro.getVersionNumber().getInterim().orElse(0)).append("_").append(distro.getVersionNumber().getUpdate().orElse(0)).append("_").append(distro.getVersionNumber().getPatch().orElse(0)).append(NEW_LINE));
                builder.append(NEW_LINE).append(NEW_LINE)
                       .append("function removeFromPath() {").append(NEW_LINE)
                       .append("    export PATH=$(echo $PATH | sed -E -e \"s;:$1;;\" -e \"s;$1:?;;\")").append(NEW_LINE)
                       .append("}").append(NEW_LINE)
                       .append(NEW_LINE).append(NEW_LINE)
                       .append("if [ \"$#\" -eq 0 ]; then").append(NEW_LINE)
                       .append("   echo \"Missing JDK_NAME parameter\"").append(NEW_LINE)
                       .append("elif [ $1 = \"-h\" ]; then").append(NEW_LINE)
                       .append("   echo \". ./switch-jdk.sh JDK_NAME\"").append(NEW_LINE)
                       .append("   echo \"\"").append(NEW_LINE)
                       .append("   echo \"JDK_NAME can be one of the following:\"").append(NEW_LINE);

                distros.forEach(distro -> builder.append("   echo \"").append(distro.getApiString().toLowerCase()).append("_")
                                                                      .append(distro.getVersionNumber().getFeature().getAsInt()).append("_").append(distro.getVersionNumber().getInterim().orElse(0)).append("_").append(distro.getVersionNumber().getUpdate().orElse(0)).append("_").append(distro.getVersionNumber().getPatch().orElse(0)).append("\"").append(NEW_LINE));

                distros.forEach(distro -> builder.append("elif [ $1 = \"").append(distro.getApiString().toLowerCase()).append("_")
                                                 .append(distro.getVersionNumber().getFeature().getAsInt()).append("_").append(distro.getVersionNumber().getInterim().orElse(0)).append("_").append(distro.getVersionNumber().getUpdate().orElse(0)).append("_").append(distro.getVersionNumber().getPatch().orElse(0)).append("\" ]; then").append(NEW_LINE)
                                                 .append("   removeFromPath $JAVA_HOME && export JAVA_HOME=").append(distro.getPath()).append(" && export JDK_HOME=").append(distro.getPath()).append(" && export PATH=$JAVA_HOME/bin:$PATH").append(NEW_LINE)
                                                 .append("   echo \"Switched to ").append(distro.getApiString().toLowerCase()).append(" ").append(distro.getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, true)).append("\"").append(NEW_LINE)
                                                 .append("   java -version").append(NEW_LINE));
                builder.append("else").append(NEW_LINE)
                       .append("  echo \"JDK not found\"").append(NEW_LINE)
                       .append("fi").append(NEW_LINE);
                saveToTextFileToUserFolder("switch-jdk.sh", builder.toString());
            }
            default -> { }
        }
    }

    public static final String colorToCss(final Color color) { return color.toString().replace("0x", "#"); }

    public static JDKMonUpdate checkForJDKMonUpdate() {
        HttpResponse<String> response = io.foojay.api.discoclient.util.Helper.get(Constants.RELEASES_URI, "JDKMon");
        if (null == response || null == response.body() || response.body().isEmpty()) {
            return new JDKMonUpdate(Main.VERSION, false);
        } else {
            final Gson       gson       = new Gson();
            final JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
            if (jsonObject.has("tag_name")) {
                final VersionNumber latestVersion     = VersionNumber.fromText(jsonObject.get("tag_name").getAsString());
                if (latestVersion.compareTo(Main.VERSION) > 0) {
                    return new JDKMonUpdate(latestVersion, true);
                } else {
                    return new JDKMonUpdate(Main.VERSION, false);
                }
            } else {
                return new JDKMonUpdate(Main.VERSION, false);
            }
        }
    }

    public static final boolean isUriValid(final String uri) {
        final HttpClient httpClient = HttpClient.newBuilder()
                                                .connectTimeout(Duration.ofSeconds(10))
                                                .version(Version.HTTP_2)
                                                .followRedirects(Redirect.NORMAL)
                                                .build();
        final HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                                 .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                 .uri(URI.create(uri))
                                 .timeout(Duration.ofSeconds(3))
                                 .build();
        } catch (Exception e) {
            //System.out.println(uri);
            return false;
        }
        try {
            HttpResponse<Void> responseFuture = httpClient.send(request, BodyHandlers.discarding());
            return 200 == responseFuture.statusCode();
        } catch (InterruptedException | IOException e) {
            return false;
        }
    }

    public static final List<CVE> getCVEsForVersion(final List<CVE> cves, final VersionNumber versionNumber) {
        final String version = versionNumber.toString(OutputFormat.REDUCED_COMPRESSED, true, false);
        return cves.stream().filter(cve -> cve.affectedVersions().contains(version)).collect(Collectors.toList());
    }

    public static final Color getColorForCVE(final CVE cve, final boolean darkMode) {
        switch(cve.cvss()) {
            case CVSSV2 -> {
                switch(cve.severity()) {
                    case LOW             : return darkMode ? MacosAccentColor.GREEN.colorDark  : MacosAccentColor.GREEN.colorAqua;
                    case MEDIUM          : return darkMode ? MacosAccentColor.YELLOW.colorDark : MacosAccentColor.YELLOW.colorAqua;
                    case HIGH            : return darkMode ? MacosAccentColor.RED.colorDark    : MacosAccentColor.RED.colorAqua;
                    case NONE, NOT_FOUND :
                    default              : return darkMode ? MacosAccentColor.BLUE.colorDark   : MacosAccentColor.BLUE.colorAqua;
                }
            }
            case CVSSV3 -> {
                switch (cve.severity()) {
                    case LOW             : return darkMode ? MacosAccentColor.GREEN.colorDark  : MacosAccentColor.GREEN.colorAqua;
                    case MEDIUM          : return darkMode ? MacosAccentColor.YELLOW.colorDark : MacosAccentColor.YELLOW.colorAqua;
                    case HIGH            : return darkMode ? MacosAccentColor.ORANGE.colorDark : MacosAccentColor.ORANGE.colorAqua;
                    case CRITICAL        : return darkMode ? MacosAccentColor.RED.colorDark    : MacosAccentColor.RED.colorAqua;
                    case NONE, NOT_FOUND :
                    default              : return darkMode ? MacosAccentColor.BLUE.colorDark   : MacosAccentColor.BLUE.colorAqua;
                }
            }
            default -> { return darkMode ? MacosAccentColor.BLUE.colorDark   : MacosAccentColor.BLUE.colorAqua; }
        }
    }

    public static final void untar(final String compressedFilename, final String targetFolder) {
        if (!compressedFilename.toLowerCase().endsWith("tar.gz")) { System.out.println("File must be zip format"); return; }
        if (!new File(compressedFilename).exists() || new File(compressedFilename).isDirectory()) { System.out.println("Given file either doesn't exist or is folder"); return; }
        try (GzipCompressorInputStream archive = new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(compressedFilename)))) {
            OutputStream out = Files.newOutputStream(Paths.get("un-gzipped.tar"));
            IOUtils.copy(archive, out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * Untar extracted TAR file
         */
        try (TarArchiveInputStream archive = new TarArchiveInputStream(new BufferedInputStream(new FileInputStream("un-gzipped.tar")))) {
            TarArchiveEntry entry;
            while ((entry = archive.getNextTarEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("./")) {
                    name = name.substring(2);
                }
                File file = new File(targetFolder + File.separator + name);
                if (entry.isDirectory()) {
                    Files.createDirectories(file.toPath());
                } else {
                    final int                      mode        = entry.getMode();
                    final Set<PosixFilePermission> permissions = parsePerms(mode);
                    IOUtils.copy(archive, new FileOutputStream(file));
                    try {
                        Files.setPosixFilePermissions(file.toPath(), permissions);
                        file.setExecutable(true);
                    } catch (Exception e) {

                    }
                }
            }
            // Remove extracted TAR file
            File ungzipped = new File("un-gzipped.tar");
            if (ungzipped.exists()) {
                ungzipped.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final void unzip(final String compressedFilename, final String targetFolder) {
        if (!compressedFilename.toLowerCase().endsWith("zip")) { System.out.println("File must be zip format"); return; }
        if (!new File(compressedFilename).exists() || new File(compressedFilename).isDirectory()) { System.out.println("Given file either doesn't exist or is folder"); return; }
        // Create zip file stream.
        try (ZipArchiveInputStream archive = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(compressedFilename)))) {
            ZipArchiveEntry entry;
            while ((entry = archive.getNextZipEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("./")) {
                    name = name.substring(2);
                }
                File file = new File(targetFolder + File.separator + name);
                if (entry.isDirectory()) {
                    Files.createDirectories(file.toPath());
                } else {
                    IOUtils.copy(archive, new FileOutputStream(file));
                    file.setExecutable(true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final Set<PosixFilePermission> parsePerms(int perms) {
        final char[] ds = Integer.toString(perms).toCharArray();
        final char[] ss = {'-','-','-','-','-','-','-','-','-'};
        for (int i = ds.length-1; i >= 0; i--) {
            int n = ds[i] - '0';
            if (i == ds.length-1) {
                if ((n & 1) != 0) ss[8] = 'x';
                if ((n & 2) != 0) ss[7] = 'w';
                if ((n & 4) != 0) ss[6] = 'r';
            } else if (i == ds.length-2) {
                if ((n & 1) != 0) ss[5] = 'x';
                if ((n & 2) != 0) ss[4] = 'w';
                if ((n & 4) != 0) ss[3] = 'r';
            } else if (i == ds.length-3) {
                if ((n & 1) != 0) ss[2] = 'x';
                if ((n & 2) != 0) ss[1] = 'w';
                if ((n & 4) != 0) ss[0] = 'r';
            }
        }
        String sperms = new String(ss);
        //System.out.printf("%d -> %s\n", perms, sperms);
        return PosixFilePermissions.fromString(sperms);
    }

    public static final Map<Integer, List<VersionNumber>> getAvailableVersions() {
        final Map<Integer, List<VersionNumber>> availableVersions = new HashMap<>();
        final HttpResponse<String> response = get (Constants.MAJOR_VERSIONS_URI);
        if (null == response) { return availableVersions; }
        final String bodyText = response.body();
        if (null == bodyText || bodyText.isEmpty()) { return availableVersions; }
        final Gson        gson    = new Gson();
        final JsonElement element = gson.fromJson(response.body(), JsonElement.class);
        if (element instanceof JsonObject) {
            final List<MinimizedPkg> pkgs       = new ArrayList<>();
            final JsonObject         jsonObject = element.getAsJsonObject();
            final JsonArray          jsonArray  = jsonObject.getAsJsonArray("result");
            for (int i = 0; i < jsonArray.size(); i++) {
                try {
                    final JsonObject json = jsonArray.get(i).getAsJsonObject();
                    final int majorVersion = json.get("major_version").getAsInt();
                    final JsonArray versionArray = json.getAsJsonArray("versions");
                    if (versionArray.isEmpty()) { continue; }
                    availableVersions.put(majorVersion, new ArrayList<>());
                    for (int j = 0 ; j < versionArray.size() ; j++) {
                        VersionNumber versionNumber = VersionNumber.fromText(versionArray.get(j).getAsString());
                        availableVersions.get(majorVersion).add(versionNumber);
                    }
                } catch (Exception e) {
                    //System.out.println(e);
                }
            }
        }
        return availableVersions;
    }

    public static final Optional<JDKUpdate> getNextRelease() {
        final LocalDate now             = LocalDate.now();
        final LocalDate updateMarch     = LocalDate.of(now.getYear(), Month.MARCH, 1).with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.TUESDAY));
        final LocalDate updateSeptember = LocalDate.of(now.getYear(), Month.SEPTEMBER, 1).with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.TUESDAY));
        final LocalDate updateNextMarch = LocalDate.of(now.getYear() + 1, Month.MARCH, 1).with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.TUESDAY));

        final long daysToUpdateMarch     = ChronoUnit.DAYS.between(now, updateMarch);
        final long daysToUpdateSeptember = ChronoUnit.DAYS.between(now, updateSeptember);
        final long daysToUpdateNextMarch = ChronoUnit.DAYS.between(now, updateNextMarch);

        final Map<LocalDate, Long> remainingDays = new HashMap<>(4);
        remainingDays.put(updateMarch, daysToUpdateMarch);
        remainingDays.put(updateSeptember, daysToUpdateSeptember);
        remainingDays.put(updateNextMarch, daysToUpdateNextMarch);

        Optional<JDKUpdate> optJDKUpdate = remainingDays.entrySet().stream().filter(d -> d.getValue() >= 0).sorted(Comparator.comparing(Entry::getValue)).map(entry -> new JDKUpdate(entry.getKey(), entry.getValue(), UpdateType.RELEASE)).findFirst();
        return optJDKUpdate;
    }

    public static final Optional<JDKUpdate> getNextUpdate() {
        final LocalDate now               = LocalDate.now();
        final LocalDate updateJanuary     = LocalDate.of(now.getYear(), Month.JANUARY, 1).with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.TUESDAY));
        final LocalDate updateApril       = LocalDate.of(now.getYear(), Month.APRIL, 1).with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.TUESDAY));
        final LocalDate updateJuly        = LocalDate.of(now.getYear(), Month.JULY, 1).with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.TUESDAY));
        final LocalDate updateOctober     = LocalDate.of(now.getYear(), Month.OCTOBER, 1).with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.TUESDAY));
        final LocalDate updateNextJanuary = LocalDate.of(now.getYear() + 1, Month.JANUARY, 1).with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.TUESDAY));

        final long daysToUpdateJanuary     = ChronoUnit.DAYS.between(now, updateJanuary);
        final long daysToUpdateApril       = ChronoUnit.DAYS.between(now, updateApril);
        final long daysToUpdateJuly        = ChronoUnit.DAYS.between(now, updateJuly);
        final long daysToUpdateOctober     = ChronoUnit.DAYS.between(now, updateOctober);
        final long daysToUpdateNextJanuary = ChronoUnit.DAYS.between(now, updateNextJanuary);

        final Map<LocalDate, Long> remainingDays = new HashMap<>(4);
        remainingDays.put(updateJanuary, daysToUpdateJanuary);
        remainingDays.put(updateApril, daysToUpdateApril);
        remainingDays.put(updateJuly, daysToUpdateJuly);
        remainingDays.put(updateOctober, daysToUpdateOctober);
        remainingDays.put(updateNextJanuary, daysToUpdateNextJanuary);

        Optional<JDKUpdate> optJDKUpdate = remainingDays.entrySet().stream().filter(d -> d.getValue() >= 0).sorted(Comparator.comparing(Entry::getValue)).map(entry -> new JDKUpdate(entry.getKey(), entry.getValue(), UpdateType.UPDATE)).findFirst();
        return optJDKUpdate;
    }

    public static final void copyToClipBoard(final String text) {
        Clipboard        clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content   = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    public static final String unescapeHTML(final String source) {
        String result = source;
        for (Entry<String,String> entry : Constants.HTML_ENTITIES.entrySet()) {
            result = result.replaceAll(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static final String getTextFromUrl(final String uri, final Charset charset) {
        try (var stream = URI.create(uri).toURL().openStream()) {
            return new String(stream.readAllBytes(), charset);
        } catch(Exception e) {
            return "";
        }
    }

    public static final List<JEP> getJepsFromHtml() {
        final String   html      = Helper.getTextFromUrl(Constants.JEP_BASE_URL, UTF_8);
        final Set<JEP> jepsFound = new HashSet<>();
        Constants.JEP_MATCHER.reset(html);
        while (Constants.JEP_MATCHER.find()) {
            final int    id          = Integer.parseInt(Constants.JEP_MATCHER.group(2));
            final String description = unescapeHTML(Constants.JEP_MATCHER.group(4));
            final String url         = Constants.JEP_URL + id;
            jepsFound.add(new JEP(id, description, url, id > 999_999));
        }
        return jepsFound.stream().sorted(Comparator.comparing(JEP::id)).collect(Collectors.toList());
    }

    public static final List<JSR> getJsrsFromHtml() {
        final String   html      = Helper.getTextFromUrl(Constants.JSR_BASE_URL, UTF_8);
        final Set<JSR> jsrsFound = new HashSet<>();
        Constants.JSR_TM_MATCHER.reset(html);
        final String txt = Constants.JSR_TM_MATCHER.replaceAll("");
        Constants.JSR_MATCHER.reset(txt);
        while (Constants.JSR_MATCHER.find()) {
            final int    id          = Integer.parseInt(Constants.JSR_MATCHER.group(2));
            final String description = unescapeHTML(Constants.JSR_MATCHER.group(4));
            final String url         = Constants.JSR_URL + id;
            jsrsFound.add(new JSR(id, description, url));
        }
        return jsrsFound.stream().sorted(Comparator.comparing(JSR::id)).collect(Collectors.toList());
    }

    public static final List<Project> getProjectsFromHtml() {
        final String       html          = Helper.getTextFromUrl(Constants.OPENJDK_PROJECT_URL, UTF_8);
        final Set<Project> projectsFound = new HashSet<>();
        Constants.PROJECT_MATCHER.reset(html);
        while (Constants.PROJECT_MATCHER.find()) {
            final String description = unescapeHTML(Constants.PROJECT_MATCHER.group(2));
            final String url         = Constants.OPENJDK_PROJECT_URL + Constants.PROJECT_MATCHER.group(1);
            if (!description.equals("archive")) {
                projectsFound.add(new Project(description, url));
            }
        }
        return projectsFound.stream().sorted(Comparator.comparing(Project::description)).collect(Collectors.toList());
    }

    public static final void openInDefaultBrowser(final Application app, final String url) {
        Platform.runLater(() -> app.getHostServices().showDocument(url));
    }

    public static long getFolderSize(final String folderName) throws IOException {
        final File f = new File(folderName);
        if (f.exists() && f.isDirectory()) {
            final Path folder = Paths.get(folderName);
            final long size   = Files.walk(folder).filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();
            return size;
        } else {
            return -1;
        }
    }

    private static final NavigableMap<Long, String> SUFFIXES = new TreeMap<>(Map.of(1_000L, "k",
                                                                                    1_000_000L, "M",
                                                                                    1_000_000_000L, "G",
                                                                                    1_000_000_000_000L, "T",
                                                                                    1_000_000_000_000_000L, "P",
                                                                                    1_000_000_000_000_000_000L, "E"));
    public static final String shortenNumber(final long value) {
        return shortenNumber(value, Locale.US);
    }
    public static final String shortenNumber(final long value, final Locale locale) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) { return shortenNumber(Long.MIN_VALUE + 1, locale); }
        if (value < 0)               { return "-" + shortenNumber(-value, locale); }
        if (value < 1000)            { return Long.toString(value); }

        final Entry<Long, String> entry    = SUFFIXES.floorEntry(value);
        final Long                divideBy = entry.getKey();
        final String                 suffix     = entry.getValue();
        final long                   truncated  = value / (divideBy / 10);
        final boolean                hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        final java.text.NumberFormat formatter  = java.text.NumberFormat.getNumberInstance(locale);
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(1);
        return hasDecimal ? formatter.format(truncated / 10d) + suffix : (truncated / 10) + suffix;
    }


    // ******************** REST calls ****************************************
    public static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                         .connectTimeout(Duration.ofSeconds(20))
                         .followRedirects(Redirect.NORMAL)
                         .version(java.net.http.HttpClient.Version.HTTP_2)
                         .build();
    }

    public static final HttpResponse<String> get(final String uri) { return get(uri, ""); }
    public static final HttpResponse<String> get(final String uri, final String userAgent) {
        if (null == httpClient) { httpClient = createHttpClient(); }
        final String userAgentText = (null == userAgent || userAgent.isEmpty()) ? "DiscoClient V2" : "DiscoClient V2 (" + userAgent + ")";
        HttpRequest request = HttpRequest.newBuilder()
                                         .GET()
                                         .uri(URI.create(uri))
                                         .setHeader("Accept", "application/json")
                                         .setHeader("User-Agent", userAgentText)
                                         .timeout(Duration.ofSeconds(60))
                                         .build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response;
            } else {
                // Problem with url request
                return response;
            }
        } catch (CompletionException | InterruptedException | IOException e) {
            return null;
        }
    }

    public static final CompletableFuture<HttpResponse<String>> getAsync(final String uri) { return getAsync(uri, ""); }
    public static final CompletableFuture<HttpResponse<String>> getAsync(final String uri, final String userAgent) {
        if (null == httpClient) { httpClient = createHttpClient(); }

        final String userAgentText = (null == userAgent || userAgent.isEmpty()) ? "DiscoClient" : "DiscoClient (" + userAgent + ")";
        final HttpRequest request = HttpRequest.newBuilder()
                                               .GET()
                                               .uri(URI.create(uri))
                                               .setHeader("Accept", "application/json")
                                               .setHeader("User-Agent", userAgentText)
                                               .timeout(Duration.ofSeconds(60))
                                               .build();
        return httpClient.sendAsync(request, BodyHandlers.ofString());
    }
}
