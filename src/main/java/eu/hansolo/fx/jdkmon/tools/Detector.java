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

import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class Detector {
    public enum OperatingSystem { WINDOWS, MACOS, LINUX, SOLARIS, NONE }
    public enum MacOSSystemColor {
        BLUE(Color.rgb(0, 122, 255), Color.rgb(10, 132, 255)),
        BROWN(Color.rgb(162, 132, 94), Color.rgb(172, 142, 104)),
        GRAY(Color.rgb(142, 142, 147), Color.rgb(152, 152, 157)),
        GREEN(Color.rgb(40, 205, 65), Color.rgb(50, 215, 75)),
        INIDIGO(Color.rgb(88, 86, 214), Color.rgb(94, 92, 230)),
        ORANGE(Color.rgb(255, 149, 0), Color.rgb(255, 159, 0)),
        PINK(Color.rgb(255, 45, 85), Color.rgb(255, 55, 95)),
        PURPLE(Color.rgb(175, 82, 222), Color.rgb(191, 90, 242)),
        RED(Color.rgb(255, 59, 48), Color.rgb(255, 69, 58)),
        TEAL(Color.rgb(85, 190, 240), Color.rgb(90, 200, 245)),
        YELLOW(Color.rgb(255, 204, 0), Color.rgb(255, 214, 10));

        final Color   colorAqua;
        final Color   colorDark;


        MacOSSystemColor(final Color colorAqua, final Color colorDark) {
            this.colorAqua = colorAqua;
            this.colorDark = colorDark;
        }

        public Color getColorAqua() { return colorAqua; }

        public Color getColorDark() { return colorDark; }

        public boolean isGivenColor(final Color color) {
            return (colorAqua.equals(color) || colorDark.equals(color));
        }

        public static final List<MacOSSystemColor> getAsList() { return Arrays.asList(values()); }
    }
    public enum MacOSAccentColor {
        MULTI_COLOR(null, MacOSSystemColor.BLUE.colorAqua, Color.web("#b3d7ff"), Color.web("#7daaf0"), MacOSSystemColor.BLUE.colorDark, Color.web("#3f638b"), Color.web("#296e99")),
        GRAPHITE(-1, MacOSSystemColor.GRAY.colorAqua, Color.web("#e0e0e0"), Color.web("#c4c2c4"), MacOSSystemColor.GRAY.colorDark, Color.web("#696665"), Color.web("#7d7b7a")),
        RED(0, MacOSSystemColor.RED.colorAqua, Color.web("#f5c3c5"), Color.web("#df878b"), MacOSSystemColor.RED.colorDark, Color.web("#8b5758"), Color.web("#99585a")),
        ORANGE(1, MacOSSystemColor.ORANGE.colorAqua, Color.web("#fcd9bb"), Color.web("#ecae7d"), MacOSSystemColor.ORANGE.colorDark, Color.web("#886547"), Color.web("#9a7336")),
        YELLOW(2, MacOSSystemColor.YELLOW.colorAqua, Color.web("#feeebe"), Color.web("#f1d283"), MacOSSystemColor.YELLOW.colorDark, Color.web("#8b7a40"), Color.web("#9b982b")),
        GREEN(3, MacOSSystemColor.GREEN.colorAqua, Color.web("#d0eac7"), Color.web("#9dcb8f"), MacOSSystemColor.GREEN.colorDark, Color.web("#5c7653"), Color.web("#629450")),
        BLUE(4, MacOSSystemColor.BLUE.colorAqua, Color.web("#b3d7ff"), Color.web("#7daaf0"), MacOSSystemColor.BLUE.colorDark, Color.web("#3f638b"), Color.web("#296e99")),
        PURPLE(5, MacOSSystemColor.PURPLE.colorAqua, Color.web("#dfc5df"), Color.web("#b98ab8"), MacOSSystemColor.PURPLE.colorDark, Color.web("#6f566f"), Color.web("#895687")),
        PINK(6, MacOSSystemColor.PINK.colorAqua, Color.web("#fccae2"), Color.web("#eb93bc"), MacOSSystemColor.PINK.colorDark, Color.web("#87566d"), Color.web("#995582"));

        final Integer key;
        final Color   colorAqua;
        final Color   colorAquaHighlight;
        final Color   colorAquaFocus;
        final Color   colorDark;
        final Color   colorDarkHighlight;
        final Color   colorDarkFocus;


        MacOSAccentColor(final Integer key,
                         final Color colorAqua, final Color colorAquaHighlight, final Color colorAquaFocus,
                         final Color colorDark, final Color colorDarkHighlight, final Color colorDarkFocus) {
            this.key                = key;
            this.colorAqua          = colorAqua;
            this.colorAquaHighlight = colorAquaHighlight;
            this.colorAquaFocus     = colorAquaFocus;
            this.colorDark          = colorDark;
            this.colorDarkHighlight = colorDarkHighlight;
            this.colorDarkFocus     = colorDarkFocus;
        }

        public Integer getKey() { return key; }

        public Color getColorAqua() { return colorAqua; }

        public Color getColorAquaHighlight() { return colorAquaHighlight; }

        public Color getColorAquaFocus() { return colorAquaFocus; }

        public Color getColorDark() { return colorDark; }

        public Color getColorDarkHighlight() { return colorDarkHighlight; }

        public Color getColorDarkFocus() { return colorDarkFocus; }

        public boolean isGivenColor(final Color color) {
            return (colorAqua.equals(color) || colorDark.equals(color));
        }

        public static final List<MacOSAccentColor> getAsList() { return Arrays.asList(values()); }
    }

    private static final String REGQUERY_UTIL  = "reg query ";
    private static final String REGDWORD_TOKEN = "REG_DWORD";
    private static final String DARK_THEME_CMD = REGQUERY_UTIL + "\"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize\"" + " /v AppsUseLightTheme";

    public static final Map<Integer, Color[]> MACOS_ACCENT_COLOR_MAP = Map.of(-1, new Color[] { MacOSSystemColor.GRAY.colorAqua, MacOSSystemColor.GRAY.colorDark },
                                                                              0, new Color[] { MacOSSystemColor.RED.colorAqua, MacOSSystemColor.RED.colorDark },
                                                                              1, new Color[] { MacOSSystemColor.ORANGE.colorAqua, MacOSSystemColor.ORANGE.colorDark },
                                                                              2, new Color[] { MacOSSystemColor.YELLOW.colorAqua, MacOSSystemColor.YELLOW.colorDark },
                                                                              3, new Color[] { MacOSSystemColor.GREEN.colorAqua, MacOSSystemColor.GREEN.colorDark },
                                                                              4, new Color[] { MacOSSystemColor.BLUE.colorAqua, MacOSSystemColor.BLUE.colorDark },
                                                                              5, new Color[] { MacOSSystemColor.PURPLE.colorAqua, MacOSSystemColor.PURPLE.colorDark },
                                                                              6, new Color[] { MacOSSystemColor.PINK.colorAqua, MacOSSystemColor.PINK.colorDark });


    public static final boolean isDarkMode() {
        switch(getOperatingSystem()) {
            case WINDOWS: return isWindowsDarkMode();
            case MACOS  : return isMacOsDarkMode();
            case LINUX  :
            case SOLARIS:
            default     : return false;
        }
    }

    public static final boolean isMacOsDarkMode() {
        try {
            boolean           isDarkMode = false;
            Runtime           runtime = Runtime.getRuntime();
            Process           process = runtime.exec("defaults read -g AppleInterfaceStyle");
            InputStreamReader isr     = new InputStreamReader(process.getInputStream());
            BufferedReader    rdr     = new BufferedReader(isr);
            String            line;
            while((line = rdr.readLine()) != null) {
                if (line.equals("Dark")) { isDarkMode = true; }
            }
            int rc = process.waitFor();  // Wait for the process to complete
            return 0 == rc && isDarkMode;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public static boolean isWindowsDarkMode() {
        try {
            Process      process = Runtime.getRuntime().exec(DARK_THEME_CMD);
            StreamReader reader  = new StreamReader(process.getInputStream());

            reader.start();
            process.waitFor();
            reader.join();

            String result = reader.getResult();
            int p = result.indexOf(REGDWORD_TOKEN);

            if (p == -1) { return false; }

            // 1 == Light Mode, 0 == Dark Mode
            String temp = result.substring(p + REGDWORD_TOKEN.length()).trim();
            return ((Integer.parseInt(temp.substring("0x".length()), 16))) == 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static MacOSAccentColor getMacOSAccentColor() {
        if (OperatingSystem.MACOS != getOperatingSystem()) { return MacOSAccentColor.MULTI_COLOR; }
        final boolean isDarkMode = isMacOsDarkMode();
        try {
            Integer           colorKey    = null;
            Runtime           runtime    = Runtime.getRuntime();
            Process           process    = runtime.exec("defaults read -g AppleAccentColor");
            InputStreamReader isr        = new InputStreamReader(process.getInputStream());
            BufferedReader    rdr        = new BufferedReader(isr);
            String            line;
            while((line = rdr.readLine()) != null) {
                colorKey = Integer.valueOf(line);
            }
            int rc = process.waitFor();  // Wait for the process to complete
            if (0 == rc) {
                Integer key = colorKey;
                return MacOSAccentColor.getAsList().stream().filter(macOSAccentColor -> macOSAccentColor.key == key).findFirst().orElse(MacOSAccentColor.MULTI_COLOR);
            } else {
                return MacOSAccentColor.MULTI_COLOR;
            }
        } catch (IOException | InterruptedException e) {
            return MacOSAccentColor.MULTI_COLOR;
        }
    }
    public static Color getMacOSAccentColorAsColor() {
        if (OperatingSystem.MACOS != getOperatingSystem()) { return MacOSAccentColor.MULTI_COLOR.getColorAqua(); }
        final boolean isDarkMode = isMacOsDarkMode();
        try {
            Integer           colorKey    = null;
            Runtime           runtime    = Runtime.getRuntime();
            Process           process    = runtime.exec("defaults read -g AppleAccentColor");
            InputStreamReader isr        = new InputStreamReader(process.getInputStream());
            BufferedReader    rdr        = new BufferedReader(isr);
            String            line;
            while((line = rdr.readLine()) != null) {
                colorKey = Integer.valueOf(line);
            }
            int rc = process.waitFor();  // Wait for the process to complete
            if (0 == rc) {
                return isDarkMode ? MACOS_ACCENT_COLOR_MAP.get(colorKey)[1] : MACOS_ACCENT_COLOR_MAP.get(colorKey)[0];
            } else {
                return isDarkMode ? MACOS_ACCENT_COLOR_MAP.get(4)[1] : MACOS_ACCENT_COLOR_MAP.get(4)[0];
            }
        } catch (IOException | InterruptedException e) {
            return isDarkMode ? MACOS_ACCENT_COLOR_MAP.get(4)[1] : MACOS_ACCENT_COLOR_MAP.get(4)[0];
        }
    }

    public static final OperatingSystem getOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            return OperatingSystem.WINDOWS;
        } else if (os.indexOf("mac") >= 0) {
            return OperatingSystem.MACOS;
        } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            return OperatingSystem.LINUX;
        } else if (os.indexOf("sunos") >= 0) {
            return OperatingSystem.SOLARIS;
        } else {
            return OperatingSystem.NONE;
        }
    }


    // ******************** Internal Classes **********************************
    static class StreamReader extends Thread {
        private InputStream  is;
        private StringWriter sw;

        StreamReader(InputStream is) {
            this.is = is;
            sw = new StringWriter();
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1)
                    sw.write(c);
            } catch (IOException e) { ; }
        }

        String getResult() { return sw.toString(); }
    }
}
