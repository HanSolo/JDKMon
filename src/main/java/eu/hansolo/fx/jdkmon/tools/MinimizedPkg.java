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
import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.ArchiveType;
import eu.hansolo.jdktools.LibCType;
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.PackageType;
import eu.hansolo.jdktools.ReleaseStatus;
import eu.hansolo.jdktools.Verification;
import eu.hansolo.jdktools.versioning.Semver;
import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.MajorVersion;
import io.foojay.api.discoclient.pkg.Pkg;

import static eu.hansolo.jdktools.Constants.COLON;
import static eu.hansolo.jdktools.Constants.COMMA;
import static eu.hansolo.jdktools.Constants.CURLY_BRACKET_CLOSE;
import static eu.hansolo.jdktools.Constants.CURLY_BRACKET_OPEN;
import static eu.hansolo.jdktools.Constants.QUOTES;


public class MinimizedPkg {
    private String          id;
    private ArchiveType     archiveType;
    private Distribution    distribution;
    private MajorVersion    majorVersion;
    private Semver          javaVersion;
    private ReleaseStatus   releaseStatus;
    private OperatingSystem operatingSystem;
    private LibCType        libCType;
    private Architecture    architecture;
    private PackageType     packageType;
    private boolean         javafxBundled;
    private boolean         directlyDownloadable;
    private String          filename;
    private boolean         freeToUseInProduction;
    private Verification    tckTested;
    private String          tckCertUri;
    private Verification    aqavitCertified;
    private String          aqavitCertUri;
    
    
    public MinimizedPkg(final String jsonText) {
        if (null == jsonText || jsonText.isEmpty()) { throw new IllegalArgumentException("Json text cannot be null or empty"); }
        final Gson       gson = new Gson();
        final JsonObject json = gson.fromJson(jsonText, JsonObject.class);

        this.id                    = json.get(Pkg.FIELD_ID).getAsString();
        this.distribution          = DiscoClient.getDistributionFromText(json.get(Pkg.FIELD_DISTRIBUTION).getAsString());
        if (null == this.distribution) {
            throw new IllegalArgumentException("Distribution not found");
        }
        this.majorVersion          = new MajorVersion(json.get(Pkg.FIELD_MAJOR_VERSION).getAsInt());
        this.javaVersion           = Semver.fromText(json.get(Pkg.FIELD_JAVA_VERSION).getAsString()).getSemver1();
        this.architecture          = Architecture.fromText(json.get(Pkg.FIELD_ARCHITECTURE).getAsString());
        this.operatingSystem       = OperatingSystem.fromText(json.get(Pkg.FIELD_OPERATING_SYSTEM).getAsString());
        this.libCType              = json.has(Pkg.FIELD_LIB_C_TYPE) ? LibCType.fromText(json.get(Pkg.FIELD_LIB_C_TYPE).getAsString()) : this.operatingSystem.getLibCType();
        this.packageType           = PackageType.fromText(json.get(Pkg.FIELD_PACKAGE_TYPE).getAsString());
        this.releaseStatus         = ReleaseStatus.fromText(json.get(Pkg.FIELD_RELEASE_STATUS).getAsString());
        this.archiveType           = ArchiveType.fromText(json.get(Pkg.FIELD_ARCHIVE_TYPE).getAsString());
        this.javafxBundled         = json.get(Pkg.FIELD_JAVAFX_BUNDLED).getAsBoolean();
        this.directlyDownloadable  = json.get(Pkg.FIELD_DIRECTLY_DOWNLOADABLE).getAsBoolean();
        this.filename              = json.get(Pkg.FIELD_FILENAME).getAsString();
        this.freeToUseInProduction = json.get(Pkg.FIELD_FREE_USE_IN_PROD).getAsBoolean();
        this.tckTested             = json.has(Pkg.FIELD_TCK_TESTED) ? Verification.fromText(json.get(Pkg.FIELD_TCK_TESTED).getAsString()) : Verification.UNKNOWN;
        this.tckCertUri            = json.has(Pkg.FIELD_TCK_CERT_URI) ? json.get(Pkg.FIELD_TCK_CERT_URI).getAsString() : "";
        this.aqavitCertified       = json.has(Pkg.FIELD_AQAVIT_CERTIFIED) ? Verification.fromText(json.get(Pkg.FIELD_AQAVIT_CERTIFIED).getAsString()) : Verification.UNKNOWN;
        this.aqavitCertUri         = json.has(Pkg.FIELD_AQAVIT_CERT_URI) ? json.get(Pkg.FIELD_AQAVIT_CERT_URI).getAsString() : "";
    }
    public MinimizedPkg(final String id, final ArchiveType archiveType, final Distribution distribution, final MajorVersion majorVersion, final Semver javaVersion,
                        final ReleaseStatus releaseStatus, final OperatingSystem operatingSystem, final Architecture architecture, final LibCType libCType, final PackageType packageType,
                        final boolean javafxBundled, final boolean directlyDownloadable, final String filename, final boolean freeToUseInProduction, final Verification tckTested,
                        final String tckCertUri, final Verification aqavitCertified, final String aqavitCertUri) {
        this.id                    = id;
        this.archiveType           = archiveType;
        this.distribution          = distribution;
        this.majorVersion          = majorVersion;
        this.javaVersion           = javaVersion;
        this.releaseStatus         = releaseStatus;
        this.operatingSystem       = operatingSystem;
        this.architecture          = architecture;
        this.libCType              = libCType;
        this.packageType           = packageType;
        this.javafxBundled         = javafxBundled;
        this.directlyDownloadable  = directlyDownloadable;
        this.filename              = filename;
        this.freeToUseInProduction = freeToUseInProduction;
        this.tckTested             = tckTested;
        this.tckCertUri            = tckCertUri;
        this.aqavitCertified       = aqavitCertified;
        this.aqavitCertUri         = aqavitCertUri;
    }

    public String getId() { return id; }

    public ArchiveType getArchiveType() { return archiveType; }

    public Distribution getDistribution() { return distribution; }

    public MajorVersion getMajorVersion() { return majorVersion; }

    public Semver getJavaVersion() { return javaVersion; }

    public ReleaseStatus getReleaseStatus() { return releaseStatus; }

    public OperatingSystem getOperatingSystem() { return operatingSystem; }

    public LibCType getLibCType() { return libCType; }

    public Architecture getArchitecture() { return architecture; }

    public PackageType getPackageType() { return packageType; }

    public boolean isJavaFXBundled() { return javafxBundled; }

    public boolean isDirectlyDownloadable() { return directlyDownloadable; }

    public String getFilename() { return filename; }

    public boolean isFreeToUseInProduction() { return freeToUseInProduction; }

    public Verification getTckTested() { return tckTested; }

    public String getTckCertUri() { return tckCertUri; }

    public Verification getAqavitCertified() { return aqavitCertified; }

    public String getAqavitCertUri() { return aqavitCertUri; }

    @Override public String toString() {
        return new StringBuilder().append(CURLY_BRACKET_OPEN)
                                  .append(QUOTES).append(Pkg.FIELD_ID).append(QUOTES).append(COLON).append(QUOTES).append(getId()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_DISTRIBUTION).append(QUOTES).append(COLON).append(QUOTES).append(distribution.getName()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_JAVA_VERSION).append(QUOTES).append(COLON).append(QUOTES).append(javaVersion.toString()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_ARCHITECTURE).append(QUOTES).append(COLON).append(QUOTES).append(architecture.name()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_OPERATING_SYSTEM).append(QUOTES).append(COLON).append(QUOTES).append(operatingSystem.name()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_PACKAGE_TYPE).append(QUOTES).append(COLON).append(QUOTES).append(packageType.name()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_RELEASE_STATUS).append(QUOTES).append(COLON).append(QUOTES).append(releaseStatus.name()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_ARCHIVE_TYPE).append(QUOTES).append(COLON).append(QUOTES).append(archiveType.getUiString()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_JAVAFX_BUNDLED).append(QUOTES).append(COLON).append(javafxBundled).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_FILENAME).append(QUOTES).append(COLON).append(QUOTES).append(filename).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_FREE_USE_IN_PROD).append(QUOTES).append(COLON).append(freeToUseInProduction).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_TCK_TESTED).append(QUOTES).append(COLON).append(QUOTES).append(tckTested.getApiString()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_TCK_CERT_URI).append(QUOTES).append(COLON).append(QUOTES).append(tckCertUri).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_AQAVIT_CERTIFIED).append(QUOTES).append(COLON).append(QUOTES).append(aqavitCertified.getApiString()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append(Pkg.FIELD_AQAVIT_CERT_URI).append(QUOTES).append(COLON).append(QUOTES).append(aqavitCertUri).append(QUOTES)
                                  .append(CURLY_BRACKET_CLOSE)
                                  .toString();
    }
}
