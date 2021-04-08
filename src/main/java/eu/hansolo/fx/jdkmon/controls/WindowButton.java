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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;


public interface WindowButton {
    WindowButtonType getType();
    void setType(final WindowButtonType type);
    ObjectProperty<WindowButtonType> typeProperty();

    boolean isDarkMode();
    void setDarkMode(final boolean darkMode);
    BooleanProperty darkModeProperty();

    boolean isHovered();
    void setHovered(final boolean hovered);
    BooleanProperty hoveredProperty();

    void setOnMousePressed(final Consumer<MouseEvent> mousePressedConsumer);
    void setOnMouseReleased(final Consumer<MouseEvent> mouseReleasedConsumer);
}
