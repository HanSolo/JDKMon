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


public enum TermOfSupport {
    STS("short term stable", "sts"),
    MTS("mid term stable", "mts"),
    LTS("long term stable", "lts"),
    NONE("-", ""),
    NOT_FOUND("", "");

    private final String uiString;
    private final String apiString;


    TermOfSupport(final String uiString, final String apiString) {
        this.uiString = uiString;
        this.apiString = apiString;
    }


    public String getUiString() { return uiString; }

    public String getApiString() { return apiString; }

    public TermOfSupport getDefault() { return TermOfSupport.NONE; }

    public TermOfSupport getNotFound() { return TermOfSupport.NOT_FOUND; }

    public TermOfSupport[] getAll() { return values(); }

    public static TermOfSupport fromText(final String text) {
        if (null == text) { return NOT_FOUND; }
        switch(text) {
            case "long_term_stable":
            case "LongTermStable":
            case "lts":
            case "LTS":
            case "Lts":
                return LTS;
            case "mid_term_stable":
            case "MidTermStable":
            case "mts":
            case "MTS":
            case "Mts":
                return MTS;
            case "short_term_stable":
            case "ShortTermStable":
            case "sts":
            case "STS":
            case "Sts":
                return STS;
            default: return NOT_FOUND;

        }
    }

    public static List<TermOfSupport> getAsList() { return Arrays.asList(values()); }
}
