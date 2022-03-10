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
import com.google.gson.JsonObject;
import eu.hansolo.fx.jdkmon.Main;
import eu.hansolo.fx.jdkmon.tools.Detector.MacosAccentColor;
import eu.hansolo.fx.jdkmon.tools.Records.CVE;
import eu.hansolo.jdktools.TermOfSupport;
import eu.hansolo.jdktools.versioning.VersionNumber;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;


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
        if (featureVersion < 13) { return false; }
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

    public static final String colorToCss(final Color color) { return color.toString().replace("0x", "#"); }

    public static CompletableFuture<HttpResponse<String>> checkForJDKMonUpdateAsync() {
        return io.foojay.api.discoclient.util.Helper.getAsync(Constants.RELEASES_URI, "JDKMon");
    }

    public static boolean isUpdateAvailable() {
        final HttpResponse<String> response = io.foojay.api.discoclient.util.Helper.get(Constants.RELEASES_URI, "JDKMon");
        if (null == response || null == response.body() || response.body().isEmpty()) {
            return false;
        } else {
            final Gson       gson       = new Gson();
            final JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
            if (jsonObject.has("tag_name")) {
                VersionNumber latestVersion = VersionNumber.fromText(jsonObject.get("tag_name").getAsString());
                return latestVersion.compareTo(Main.VERSION) > 0;
            }
        }
        return false;
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
            System.out.println(uri);
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
        return cves.stream().filter(cve -> cve.affectedVersions().contains(versionNumber)).collect(Collectors.toList());
    }

    public static final Color getColorForCVE(final CVE cve, final boolean darkMode) {
        final double score = cve.score();
        if (score >= 9.0) {
            return darkMode ? MacosAccentColor.RED.colorDark : MacosAccentColor.RED.colorAqua;
        } else if (score >= 7.0) {
            return darkMode ? MacosAccentColor.ORANGE.colorDark : MacosAccentColor.ORANGE.colorAqua;
        } else if (score >= 4.0) {
            return darkMode ? MacosAccentColor.YELLOW.colorDark : MacosAccentColor.YELLOW.colorAqua;
        } else {
            return darkMode ? MacosAccentColor.GREEN.colorDark : MacosAccentColor.GREEN.colorAqua;
        }
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
