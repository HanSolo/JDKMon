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

package eu.hansolo.fx.jdkmon.controls;

import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

import java.util.function.Consumer;


@DefaultProperty("children")
public class MacOSWindowButton extends Region {
    public enum Type { CLOSE, MINIMIZE, ZOOM }
    public enum Size {
        NORMAL(12),
        SMALL(9);

        public final double px;

        Size(final double px) {
            this.px = px;
        }
    }

    private static final double               MINIMUM_WIDTH          = Size.SMALL.px;
    private static final double               MINIMUM_HEIGHT         = Size.SMALL.px;
    private static final double               MAXIMUM_WIDTH          = Size.NORMAL.px;
    private static final double               MAXIMUM_HEIGHT         = Size.NORMAL.px;
    private static final PseudoClass          CLOSE_PSEUDO_CLASS     = PseudoClass.getPseudoClass("close");
    private static final PseudoClass          MINIMIZE_PSEUDO_CLASS  = PseudoClass.getPseudoClass("minimize");
    private static final PseudoClass          ZOOM_PSEUDO_CLASS      = PseudoClass.getPseudoClass("zoom");
    private static final PseudoClass          HOVERED_PSEUDO_CLASS   = PseudoClass.getPseudoClass("hovered");
    private static final PseudoClass          PRESSED_PSEUDO_CLASS   = PseudoClass.getPseudoClass("pressed");
    private static final PseudoClass          DARK_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("dark");
    private              Size                 iconSize;
    private              BooleanProperty      darkMode;
    private              BooleanProperty      hovered;
    private static       String               userAgentStyleSheet;
    private              ObjectProperty<Type> type;
    private              double               size;
    private              double               width;
    private              double               height;
    private              Circle               circle;
    private              Region               symbol;
    private              Consumer<MouseEvent> mousePressedConsumer;
    private              Consumer<MouseEvent> mouseReleasedConsumer;


    // ******************** Constructors **************************************
    public MacOSWindowButton() {
        this(Type.CLOSE);
    }
    public MacOSWindowButton(final Type type) {
        this(type, Size.NORMAL);
    }
    public MacOSWindowButton(final Type type, final Size size) {
        this.type     = new ObjectPropertyBase<>(type) {
            @Override protected void invalidated() {
                switch(get()) {
                    case CLOSE    -> {
                        pseudoClassStateChanged(CLOSE_PSEUDO_CLASS, true);
                        pseudoClassStateChanged(MINIMIZE_PSEUDO_CLASS, false);
                        pseudoClassStateChanged(ZOOM_PSEUDO_CLASS, false);
                    }
                    case MINIMIZE -> {
                        pseudoClassStateChanged(CLOSE_PSEUDO_CLASS, false);
                        pseudoClassStateChanged(MINIMIZE_PSEUDO_CLASS, true);
                        pseudoClassStateChanged(ZOOM_PSEUDO_CLASS, false);
                    }
                    case ZOOM     -> {
                        pseudoClassStateChanged(CLOSE_PSEUDO_CLASS, false);
                        pseudoClassStateChanged(MINIMIZE_PSEUDO_CLASS, false);
                        pseudoClassStateChanged(ZOOM_PSEUDO_CLASS, true);
                    }
                }
            }
            @Override public Object getBean() { return MacOSWindowButton.this; }
            @Override public String getName() { return "type"; }
        };
        this.darkMode = new BooleanPropertyBase(false) {
            @Override protected void invalidated() { pseudoClassStateChanged(DARK_MODE_PSEUDO_CLASS, get()); }
            @Override public Object getBean() { return MacOSWindowButton.this; }
            @Override public String getName() { return "darkMode"; }
        };
        this.hovered  = new BooleanPropertyBase() {
            @Override protected void invalidated() { pseudoClassStateChanged(HOVERED_PSEUDO_CLASS, get()); }
            @Override public Object getBean() { return MacOSWindowButton.this; }
            @Override public String getName() { return "hovered"; }
        };
        this.iconSize = size;

        pseudoClassStateChanged(CLOSE_PSEUDO_CLASS,    Type.CLOSE    == type);
        pseudoClassStateChanged(MINIMIZE_PSEUDO_CLASS, Type.MINIMIZE == type);
        pseudoClassStateChanged(ZOOM_PSEUDO_CLASS,     Type.ZOOM     == type);

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 || Double.compare(getWidth(), 0.0) <= 0 ||
            Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(iconSize.px, iconSize.px);
            }
        }

        getStyleClass().add("macos-window-button");

        circle = new Circle();
        circle.getStyleClass().add("circle");
        circle.setStrokeType(StrokeType.INSIDE);

        symbol = new Region();
        symbol.getStyleClass().add(Size.NORMAL == iconSize ? "symbol" : "symbol-small");

        getChildren().setAll(circle, symbol);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, true);
            if (null == mousePressedConsumer) { return; }
            mousePressedConsumer.accept(e);
        });
        addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, false);
            if (null == mouseReleasedConsumer) { return; }
            mouseReleasedConsumer.accept(e);
        });
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT) { return super.computePrefWidth(HEIGHT); }
    @Override protected double computePrefHeight(final double WIDTH) { return super.computePrefHeight(WIDTH); }
    @Override protected double computeMaxWidth(final double HEIGHT) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH) { return MAXIMUM_HEIGHT; }

    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public Type getType() { return type.get(); }
    public void setType(final Type type) { this.type.set(type); }
    public ObjectProperty<Type> typeProperty() { return type; }

    public boolean isDarkMode() { return darkMode.get(); }
    public void setDarkMode(final boolean darkMode) { this.darkMode.set(darkMode); }
    public BooleanProperty darkModeProperty() { return darkMode; }

    public boolean isHovered() { return hovered.get(); }
    public void setHovered(final boolean hovered) { this.hovered.set(hovered); }
    public BooleanProperty hoveredProperty() { return hovered; }

    public void setOnMousePressed(final Consumer<MouseEvent> mousePressedConsumer)   { this.mousePressedConsumer  = mousePressedConsumer; }
    public void setOnMouseReleased(final Consumer<MouseEvent> mouseReleasedConsumer) { this.mouseReleasedConsumer = mouseReleasedConsumer; }


    // ******************** Layout ********************************************
    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;


        if (width > 0 && height > 0) {
            setMaxSize(size, size);
            setPrefSize(size, size);

            double center = size * 0.5;
            circle.setRadius(center);
            circle.setCenterX(center);
            circle.setCenterY(center);

            symbol.setPrefSize(size, size);
        }
    }

    @Override public String getUserAgentStylesheet() {
        if (null == userAgentStyleSheet) { userAgentStyleSheet = MacOSWindowButton.class.getResource("macos-window-button.css").toExternalForm(); }
        return userAgentStyleSheet;
    }
}
