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
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class NotificationBuilder<B extends NotificationBuilder<B>> {
    private Map<String, Property> properties = new ConcurrentHashMap<>();


    // ******************** Constructors **************************************
    protected NotificationBuilder() {
    }


    // ******************** Methods *******************************************
    public final static NotificationBuilder create() {
        return new NotificationBuilder();
    }

    public final B title(final String title) {
        properties.put("title", new SimpleStringProperty(title));
        return (B) this;
    }

    public final B message(final String message) {
        properties.put("message", new SimpleStringProperty(message));
        return (B) this;
    }

    public final B image(final Image image) {
        properties.put("image", new SimpleObjectProperty<>(image));
        return (B) this;
    }
    
    public final Notification build() {
        final Notification notification;
        if (properties.keySet().contains("title") && properties.keySet().contains("message") && properties.keySet().contains("image")) {
            notification = new Notification(((StringProperty) properties.get("title")).get(),
                                            ((StringProperty) properties.get("message")).get(),
                                            ((ObjectProperty<Image>) properties.get("image")).get());
        } else if (properties.keySet().contains("title") && properties.keySet().contains("message")) {
            notification = new Notification(((StringProperty) properties.get("title")).get(),
                                            ((StringProperty) properties.get("message")).get());
        } else if (properties.keySet().contains("message") && properties.keySet().contains("image")) {
            notification = new Notification(((StringProperty) properties.get("message")).get(),
                                            ((ObjectProperty<Image>) properties.get("image")).get());
        } else {
            throw new IllegalArgumentException("Wrong or missing parameters.");
        }               
        return notification;
    }
}
