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

import java.util.regex.Pattern;


public class Constants {
    public static final String  RELEASES_URI             = "https://github.com/HanSolo/JDKMon/releases/latest";
    public static final long    INITIAL_DELAY_IN_HOURS   = 3;
    public static final long    RESCAN_INTERVAL_IN_HOURS = 3;
    public static final Pattern POSITIVE_INTEGER_PATTERN = Pattern.compile("\\d+");
}
