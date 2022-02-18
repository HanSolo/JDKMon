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

import eu.hansolo.fx.jdkmon.tools.Detector;
import eu.hansolo.fx.jdkmon.tools.Detector.MacosAccentColor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class MacosComboBoxCell<T> extends ListCell<T> {
    public  static final ImageView                        EMPTY_CHECK_MARK  = new ImageView(new Image(MacosComboBoxCell.class.getResourceAsStream("macos-checkmark-empty.png"), 10, 10, true, false));
    public  static final ImageView                        BLACK_CHECK_MARK  = new ImageView(new Image(MacosComboBoxCell.class.getResourceAsStream("macos-checkmark-black.png"), 10, 10, true, false));
    public  static final ImageView                        WHITE_CHECK_MARK  = new ImageView(new Image(MacosComboBoxCell.class.getResourceAsStream("macos-checkmark-white.png"), 10, 10, true, false));
    private static final PseudoClass                      DARK_PSEUDO_CLASS = PseudoClass.getPseudoClass("dark");
    private              BooleanProperty                  dark;
    private              ObjectProperty<MacosAccentColor> accentColor;


    // ******************** Constructors **************************************
    public MacosComboBoxCell() {
        getStyleClass().add("macos-combo-box-cell");
        this.dark        = new BooleanPropertyBase(Detector.isDarkMode()) {
            @Override protected void invalidated() {
                pseudoClassStateChanged(DARK_PSEUDO_CLASS, get());
            }
            @Override public Object getBean() { return MacosComboBoxCell.this; }
            @Override public String getName() { return "dark"; }
        };
        this.accentColor = new ObjectPropertyBase<>(Detector.getMacosAccentColor()) {
            @Override protected void invalidated() { setStyle(isDark() ? new StringBuilder("-selection-color: ").append(get().getDarkStyleClass()).append(";").toString() : new StringBuilder("-selection-color: ").append(get().getDarkStyleClass()).append(";").toString()); }
            @Override public Object getBean() { return MacosComboBoxCell.this; }
            @Override public String getName() { return "accentColor"; }
        };
        setGraphicTextGap(5);
        setContentDisplay(ContentDisplay.LEFT);
    }


    // ******************** Methods *******************************************
    public final boolean isDark() {
        return dark.get();
    }
    public final void setDark(final boolean dark) { this.dark.set(dark); }
    public final BooleanProperty darkProperty() { return dark; }

    public MacosAccentColor getAccentColor() { return accentColor.get(); }
    public void setAccentColor(final MacosAccentColor accentColor) { this.accentColor.set(accentColor); }
    public ObjectProperty<MacosAccentColor> accentColorProperty() { return accentColor; }


    @Override protected void updateItem(final T item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || null == item) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.toString());
            setGraphic(isSelected() ? (isDark() ? WHITE_CHECK_MARK : BLACK_CHECK_MARK) : EMPTY_CHECK_MARK);
        }
    }

    @Override public String toString() {
        return getItem().toString();
    }


    // ******************** Style related *************************************
    //@Override public String getUserAgentStylesheet() { return MacosComboBoxCell.class.getResource("../jdk-mon.css").toExternalForm(); }
}
