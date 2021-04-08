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

import javafx.scene.text.Font;


public class Fonts {
    private static final String SF_PRO_NAME;
    private static final String SF_PRO_TEXT_LIGHT_NAME;
    private static final String SF_PRO_TEXT_REGULAR_NAME;
    private static final String SF_PRO_TEXT_MEDIUM_NAME;
    private static final String SF_PRO_TEXT_BOLD_NAME;
    private static final String SEGOE_UI_NAME;
    private static final String SEGOE_UI_LIGHT_NAME;
    private static final String SEGOE_UI_SEMI_BOLD_NAME;
    private static final String SEGOE_UI_BOLD_NAME;

    private static String sfProName;
    private static String sfProTextLightName;
    private static String sfProTextRegularName;
    private static String sfProTextMediumName;
    private static String sfProTextBoldName;
    private static String segoeUiName;
    private static String segoeUiLightName;
    private static String segoeUiSemiBoldName;
    private static String segoeUiBoldName;


    static {
        try {
            sfProName            = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/jdkmon/tools/SF-Pro.otf"), 10).getName();
            sfProTextLightName   = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/jdkmon/tools/SF-Pro-Text-Light.otf"), 10).getName();
            sfProTextRegularName = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/jdkmon/tools/SF-Pro-Text-Regular.otf"), 10).getName();
            sfProTextMediumName  = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/jdkmon/tools/SF-Pro-Text-Medium.otf"), 10).getName();
            sfProTextBoldName    = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/jdkmon/tools/SF-Pro-Text-Bold.otf"), 10).getName();
            segoeUiName          = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/jdkmon/tools/segoeui.ttf"), 10).getName();
            segoeUiLightName     = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/jdkmon/tools/segoeuil.ttf"), 10).getName();
            segoeUiSemiBoldName  = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/jdkmon/tools/segoeuisb.ttf"), 10).getName();
            segoeUiBoldName      = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/fx/jdkmon/tools/segoeuib.ttf"), 10).getName();
        } catch (Exception exception) { }
        SF_PRO_NAME              = sfProName;
        SF_PRO_TEXT_LIGHT_NAME   = sfProTextLightName;
        SF_PRO_TEXT_REGULAR_NAME = sfProTextRegularName;
        SF_PRO_TEXT_MEDIUM_NAME  = sfProTextMediumName;
        SF_PRO_TEXT_BOLD_NAME    = sfProTextBoldName;
        SEGOE_UI_NAME            = segoeUiName;
        SEGOE_UI_LIGHT_NAME      = segoeUiLightName;
        SEGOE_UI_SEMI_BOLD_NAME  = segoeUiSemiBoldName;
        SEGOE_UI_BOLD_NAME       = segoeUiBoldName;
    }


    // ******************** Methods *******************************************
    public static Font sfPro(final double size) { return new Font(SF_PRO_NAME, size); }
    public static Font sfProTextLight(final double size) { return new Font(SF_PRO_TEXT_LIGHT_NAME, size); }
    public static Font sfProTextRegular(final double size) { return new Font(SF_PRO_TEXT_REGULAR_NAME, size); }
    public static Font sfProTextMedium(final double size) { return new Font(SF_PRO_TEXT_MEDIUM_NAME, size); }
    public static Font sfProTextBold(final double size) { return new Font(SF_PRO_TEXT_BOLD_NAME, size); }

    public static Font segoeUi(final double size) { return new Font(SEGOE_UI_NAME, size); }
    public static Font segoeUiLight(final double size) { return new Font(SEGOE_UI_LIGHT_NAME, size); }
    public static Font segoeUiSemiBold(final double size) { return new Font(SEGOE_UI_SEMI_BOLD_NAME, size); }
    public static Font segoeUiBold(final double size) { return new Font(SEGOE_UI_BOLD_NAME, size); }
}
