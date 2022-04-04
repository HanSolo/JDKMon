/*
 * Copyright (c) 2022 by Gerrit Grunwald
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

package eu.hansolo.fx.jdkmon.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.Insets;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;


public class AttentionIndicator extends Region {
    public static final  Color                   RED              = Color.rgb(171, 41, 59);
    public static final  Color                   ORANGE           = Color.rgb(247, 137, 5);
    private static final double                  PREFERRED_WIDTH  = 18;
    private static final double                  PREFERRED_HEIGHT = 18;
    private static final double                  MINIMUM_WIDTH    = 18;
    private static final double                  MINIMUM_HEIGHT   = 18;
    private static final double                  MAXIMUM_WIDTH    = 18;
    private static final double                  MAXIMUM_HEIGHT   = 18;
    private static final CornerRadii             CORNER_RADII     = new CornerRadii(3);
    private              Color                   _backgroundColor = RED;
    private              Color                   _iconColor       = Color.WHITE;
    private              Tooltip                 _tooltip         = null;
    private              SVGPath                 icon;
    private              ObjectProperty<Color>   backgroundColor;
    private              ObjectProperty<Color>   iconColor;
    private              ObjectProperty<Tooltip> tooltip;


    // ******************** Constructors **************************************
    public AttentionIndicator() {
        initGraphics();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);

        setBackground(new Background(new BackgroundFill(_backgroundColor, CORNER_RADII, Insets.EMPTY)));

        icon = new SVGPath();
        icon.setContent("M12.694,2.832c0.265,-0.473 0.764,-0.766 1.306,-0.766c0.542,0 1.041,0.293 1.306,0.766c2.684,4.793 8.792,15.7 11.436,20.421c0.261,0.467 0.256,1.039 -0.015,1.501c-0.271,0.462 -0.767,0.746 -1.302,0.746c-5.353,0 -17.62,0 -22.915,0c-0.522,-0 -1.005,-0.277 -1.269,-0.727c-0.264,-0.451 -0.27,-1.007 -0.015,-1.463c2.626,-4.689 8.773,-15.665 11.468,-20.478Zm3.223,16.412c0,-0.546 -0.443,-0.988 -0.988,-0.988l-1.986,-0c-0.545,-0 -0.988,0.442 -0.988,0.988l0,1.976c0,0.545 0.443,0.988 0.988,0.988l1.986,-0c0.545,-0 0.988,-0.443 0.988,-0.988l0,-1.976Zm0.117,-9.008c0,-0.555 -0.451,-1.006 -1.006,-1.006l-2.012,0c-0.555,0 -1.006,0.451 -1.006,1.006l0,5.987c0,0.555 0.451,1.006 1.006,1.006l2.012,0c0.555,0 1.006,-0.451 1.006,-1.006l0,-5.987Z");
        icon.setFill(Color.WHITE);
        icon.setScaleX(0.5);
        icon.setScaleY(0.5);
        icon.setLayoutX(-5);
        icon.setLayoutY(-5);

        getChildren().setAll(icon);
    }


    // ******************** Methods *******************************************
    public Color getBackgroundColor() { return null == backgroundColor ? _backgroundColor : backgroundColor.get(); }
    public void setBackgroundColor(final Color backgroundColor) {
        if (null == this.backgroundColor) {
            _backgroundColor = backgroundColor;
            setBackground(new Background(new BackgroundFill(_backgroundColor, CORNER_RADII, Insets.EMPTY)));
        } else {
            this.backgroundColor.set(backgroundColor);
        }
    }
    public ObjectProperty<Color> backgroundColorProperty() {
        if (null == backgroundColor) {
            backgroundColor = new ObjectPropertyBase<>(_backgroundColor) {
                @Override protected void invalidated() { setBackground(new Background(new BackgroundFill(get(), CORNER_RADII, Insets.EMPTY)));}
                @Override public Object getBean() { return AttentionIndicator.this; }
                @Override public String getName() { return "backgroundColor"; }
            };
            _backgroundColor = null;
        }
        return backgroundColor;
    }

    public Color getIconColor() { return null == iconColor ? _iconColor : iconColor.get(); }
    public void setIconColor(final Color iconColor) {
        if (null == this.iconColor) {
            _iconColor = iconColor;
            icon.setFill(_iconColor);
        } else {
            this.iconColor.set(iconColor);
        }
    }
    public ObjectProperty<Color> iconColorProperty() {
        if (null == iconColor) {
            iconColor = new ObjectPropertyBase<>(_iconColor) {
                @Override protected void invalidated() { icon.setFill(get()); }
                @Override public Object getBean() { return AttentionIndicator.this; }
                @Override public String getName() { return "iconColor"; }
            };
            _iconColor = null;
        }
        return iconColor;
    }

    public Tooltip getTooltip() { return null == tooltip ? _tooltip : tooltip.get(); }
    public void setTooltip(final Tooltip tooltip) {
        if (null == this.tooltip) {
            _tooltip = tooltip;
            Tooltip.install(AttentionIndicator.this, _tooltip);
        } else {
            this.tooltip.set(tooltip);
        }
    }
    public ObjectProperty<Tooltip> tooltipProperty() {
        if (null == tooltip) {
            tooltip = new ObjectPropertyBase<>(_tooltip) {
                @Override protected void invalidated() { Tooltip.install(AttentionIndicator.this, get()); }
                @Override public Object getBean() { return AttentionIndicator.this; }
                @Override public String getName() { return "tooltip"; }
            };
            _tooltip = null;
        }
        return tooltip;
    }
}