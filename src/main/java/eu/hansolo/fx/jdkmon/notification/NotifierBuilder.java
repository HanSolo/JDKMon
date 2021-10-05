/*
 * Copyright (c) 2015 by Gerrit Grunwald
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

package eu.hansolo.fx.jdkmon.notification;

import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;


public class NotifierBuilder<B extends NotifierBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected NotifierBuilder() {
    }


    // ******************** Methods *******************************************
    public final static NotifierBuilder create() {
        return new NotifierBuilder();
    }
    
    public final B owner(final Stage owner) {
        properties.put("stage", new SimpleObjectProperty<>(owner));
        return (B)this;
    }

    public final B popupLocation(final Pos location) {
        properties.put("popupLocation", new SimpleObjectProperty<>(location));
        return (B)this;
    }

    public final B width(final double width) {
        properties.put("width", new SimpleDoubleProperty(width));
        return (B) this;
    }

    public final B height(final double height) {
        properties.put("height", new SimpleDoubleProperty(height));
        return (B) this;
    }

    public final B spacingY(final double spacingY) {
        properties.put("spacingY", new SimpleDoubleProperty(spacingY));
        return (B) this;
    }

    public final B popupLifeTime(final Duration popupLifetime) {
        properties.put("popupLifeTime", new SimpleObjectProperty<>(popupLifetime));
        return (B) this;
    }

    public final B popupAnimationTime(final Duration popupAnimationTime) {
        properties.put("popupAnimationTime", new SimpleObjectProperty<>(popupAnimationTime));
        return (B) this;
    }

    public final B styleSheet(final String styleSheet) {
        properties.put("styleSheet", new SimpleStringProperty(styleSheet));
        return (B) this;
    }

    public final Notification.Notifier build() {
        final Notification.Notifier notifier = Notification.Notifier.INSTANCE;
        for (String key : properties.keySet()) {
            if ("owner".equals(key)) {
                notifier.setNotificationOwner(((ObjectProperty<Stage>) properties.get(key)).get());
            } else if ("popupLocation".equals(key)) {
                notifier.setPopupLocation(null, ((ObjectProperty<Pos>) properties.get(key)).get());
            } else if ("width".equals(key)) {
                notifier.setWidth(((DoubleProperty) properties.get(key)).get());
            } else if ("height".equals(key)) {
                notifier.setHeight(((DoubleProperty) properties.get(key)).get());
            } else if ("spacingY".equals(key)) {
                notifier.setSpacingY(((DoubleProperty) properties.get(key)).get());
            } else if ("popupLifeTime".equals(key)) {
                notifier.setPopupLifetime(((ObjectProperty<Duration>) properties.get(key)).get());
            } else if ("popupAnimationTime".equals(key)) {
                notifier.setPopupAnimationTime(((ObjectProperty<Duration>) properties.get(key)).get());
            } else if ("styleSheet".equals(key)) {
                notifier.setStyleSheet(((StringProperty) properties.get(key)).get());
            }
        }
        return notifier;
    }
}
