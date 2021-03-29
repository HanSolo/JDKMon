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

public class Distribution {
    private final String  name;
    private final String  apiString;
    private final String  version;
    private final String  operatingSystem;
    private final String  architecture;
    private final Boolean fxBundled;


    public Distribution(final String name, final String apiString, final String version, final String operatingSystem, final String architecture) {
        this(name, apiString, version, operatingSystem, architecture, null);
    }
    public Distribution(final String name, final String apiString, final String version, final String operatingSystem, final String architecture, final Boolean fxBundled) {
        this.name            = name;
        this.apiString       = apiString;
        this.version         = version;
        this.operatingSystem = operatingSystem;
        this.architecture    = architecture;
        this.fxBundled       = fxBundled;
    }


    public String getName() { return name; }

    public String getApiString() { return apiString; }

    public String getVersion() { return version; }

    public String getOperatingSystem() { return operatingSystem; }

    public String getArchitecture() { return architecture; }

    public Boolean getFxBundled() { return fxBundled; }


    @Override public String toString() {
        return new StringBuilder().append("{")
                                  .append("\"name\":\"").append(getName()).append("\",")
                                  .append("\"api_string\":\"").append(getApiString()).append("\",")
                                  .append("\"version\":\"").append(getVersion()).append("\",")
                                  .append("\"operating_system\":\"").append(getOperatingSystem()).append("\",")
                                  .append("\"architecture\":\"").append(getArchitecture()).append("\",")
                                  .append("\"fx\":\"").append(getFxBundled()).append("\"")
                                  .append("}")
                                  .toString();
    }

    @Override public boolean equals(final Object obj) {
        if (!(obj instanceof Distribution)) { return false; }
        Distribution distribution = (Distribution) obj;
        return (distribution.getApiString().equals(distribution.getApiString()) && distribution.getVersion().equals(getVersion()) && distribution.getFxBundled() == fxBundled);
    }

    @Override public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == name    ? 0 : name.hashCode());
        hash = 31 * hash + (null == version ? 0 : version.hashCode());
        return hash;
    }
}
