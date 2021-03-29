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

import java.util.Arrays;
import java.util.List;


public enum ReleaseStatus {
    GA("General Access", "ga",""),
    EA("Early Access", "ea", "-ea"),
    NONE("-", "", ""),
    NOT_FOUND("", "", "");

    private final String uiString;
    private final String apiString;
    private final String preReleaseId;


    ReleaseStatus(final String uiString, final String apiString, final String preReleaseId) {
        this.uiString     = uiString;
        this.apiString    = apiString;
        this.preReleaseId = preReleaseId;
    }


    public String getUiString() { return uiString; }

    public String getApiString() { return apiString; }

    public ReleaseStatus getDefault() { return ReleaseStatus.NONE; }

    public ReleaseStatus getNotFound() { return ReleaseStatus.NOT_FOUND; }

    public ReleaseStatus[] getAll() { return values(); }

    public static ReleaseStatus fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch (text) {
            case "-ea":
            case "-EA":
            case "_ea":
            case "_EA":
            case "ea":
            case "EA":
            case "ea_":
            case "EA_":
                return EA;
            case "-ga":
            case "-GA":
            case "_ga":
            case "_GA":
            case "ga":
            case "GA":
            case "ga_":
            case "GA_":
                return GA;
            default:
                return NOT_FOUND;
        }
    }

    public String getPreReleaseId() { return preReleaseId; }

    public static List<ReleaseStatus> getAsList() { return Arrays.asList(values()); }
}
