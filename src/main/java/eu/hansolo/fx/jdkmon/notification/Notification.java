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

import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.OperatingSystem;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.event.WeakEventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.stream.IntStream;


public class Notification {
    public static final Image  INFO_ICON                    = new Image(Notifier.class.getResourceAsStream("info.png"));
    public static final Image  WARNING_ICON                 = new Image(Notifier.class.getResourceAsStream("warning.png"));
    public static final Image  SUCCESS_ICON                 = new Image(Notifier.class.getResourceAsStream("success.png"));
    public static final Image  ERROR_ICON                   = new Image(Notifier.class.getResourceAsStream("error.png"));
    public static final long   DEFAULT_POPUP_LIFETIME       = 5000;
    public static final long   DEFAULT_POPUP_ANIMATION_TIME = 500;
    public static final double DEFAULT_POPUP_PADDING        = 10;
    public static final double DEFAULT_POPUP_SPACING        = 5;
    public final        String title;
    public final        String message;
    public final        Image  image;


    // ******************** Constructors **************************************
    public Notification(final String title, final String message) {
        this(title, message, null);
    }
    public Notification(final String message, final Image image) {
        this("", message, image);
    }
    public Notification(final String title, final String message, final Image image) {
        this.title   = title;
        this.message = message;
        this.image   = image;
    }

    
    // ******************** Inner Classes *************************************
    public enum Notifier {
        INSTANCE;

        private static final double                ICON_WIDTH      = 18;
        private static final double                ICON_HEIGHT     = 18;
        private static       OperatingSystem       operatingSystem = DiscoClient.getOperatingSystem();
        private static       double                width           = OperatingSystem.WINDOWS == operatingSystem ? 332 : 345;
        private static       double                height          = OperatingSystem.WINDOWS == operatingSystem ? 92 : 75;
        private static       double                offsetX         = OperatingSystem.WINDOWS == operatingSystem ? 0 : 10;
        private static       double                offsetY         = OperatingSystem.WINDOWS == operatingSystem ? 72 : 36;
        private static       double                spacingY        = 5;
        private static       Pos                   popupLocation   = Pos.TOP_RIGHT;
        private static       Stage                 stageRef        = null;
        private              Duration              popupLifetime;
        private              Duration              popupAnimationTime;
        private              Stage                 stage;
        private              Scene                 scene;
        private              ObservableList<Popup> popups;


        // ******************** Constructor ***************************************
        Notifier() {
            init();
            Platform.runLater(() -> initGraphics());
        }


        // ******************** Initialization ************************************
        private void init() {
            popupLifetime      = Duration.millis(DEFAULT_POPUP_LIFETIME);
            popupAnimationTime = Duration.millis(DEFAULT_POPUP_ANIMATION_TIME);
            popups             = FXCollections.observableArrayList();
        }

        private void initGraphics() {
            scene = new Scene(new Region());
            scene.setFill(null);
            scene.getStylesheets().add(getClass().getResource("notification.css").toExternalForm());

            stage = new Stage();
            stage.setMinWidth(0);
            stage.setMinHeight(0);
            stage.setMaxWidth(0);
            stage.setMaxHeight(0);
            stage.setWidth(0);
            stage.setHeight(0);
            stage.initModality(Modality.NONE);

            stage.setResizable(false);
            stage.initStyle(StageStyle.TRANSPARENT);
            //stage.initStyle(StageStyle.UTILITY);
            stage.setScene(scene);
            stage.setAlwaysOnTop(true);
        }


        // ******************** Methods *******************************************
        /**
         * @param stageRef  The Notification will be positioned relative to the given Stage.<br>
         * 					 If null then the Notification will be positioned relative to the primary Screen.
         * @param popupLocation  The default is TOP_RIGHT of primary Screen.
         */
        public static void setPopupLocation(final Stage stageRef, final Pos popupLocation) {
            if (null != stageRef) {
                INSTANCE.stage.initOwner(stageRef);
                Notifier.stageRef = stageRef;
            }
            Notifier.popupLocation = popupLocation;
        }

        /**
         * Sets the Notification's owner stage so that when the owner
         * stage is closed Notifications will be shut down as well.<br>
         * This is only needed if <code>setPopupLocation</code> is called
         * <u>without</u> a stage reference.
         * @param owner
         */
        public static void setNotificationOwner(final Stage owner) {
            INSTANCE.stage.initOwner(owner);
        }

        /**
         * @param offsetX  The horizontal shift required.
         * <br> The default is 0 px.
         */
        public static void setOffsetX(final double offsetX) {
            Notifier.offsetX = offsetX;
        }

        /**
         * @param offsetY  The vertical shift required.
         * <br> The default is 25 px.
         */
        public static void setOffsetY(final double offsetY) {
            Notifier.offsetY = offsetY;
        }

        /**
         * @param width  The default is 300 px.
         */
        public static void setWidth(final double width) {
            Notifier.width = width;
        }

        /**
         * @param height  The default is 80 px.
         */
        public static void setHeight(final double height) {
            Notifier.height = height;
        }

        /**
         * @param spacingY  The spacing between multiple Notifications.
         * <br> The default is 5 px.
         */
        public static void setSpacingY(final double spacingY) {
            Notifier.spacingY = spacingY;
        }
               
        public void stop() {
            popups.clear();
            stage.close();
        }

        /**
         * Returns the Duration that the notification will stay on screen before it
         * will fade out. The default is 5000 ms
         * @return the Duration the popup notification will stay on screen
         */
        public Duration getPopupLifetime() {
            return popupLifetime;
        }

        /**
         * Defines the Duration that the popup notification will stay on screen before it
         * will fade out. The parameter is limited to values between 2 and 20 seconds.
         * @param popupLifetime
         */
        public void setPopupLifetime(final Duration popupLifetime) {
            this.popupLifetime = Duration.millis(clamp(2000, 20000, popupLifetime.toMillis()));
        }

        /**
         * Returns the Duration that it takes to fade out the notification
         * The parameter is limited to values between 0 and 1000 ms
         * @return the Duration that it takes to fade out the notification
         */
        public Duration getPopupAnimationTime() {
            return popupAnimationTime;
        }

        /**
         * Defines the Duration that it takes to fade out the notification
         * The parameter is limited to values between 0 and 1000 ms
         * Default value is 500 ms
         * @param popupAnimationTime
         */
        public void setPopupAnimationTime(final Duration popupAnimationTime) {
            this.popupAnimationTime = Duration.millis(clamp(0, 1000, popupAnimationTime.toMillis()));
        }

        public void setStyleSheet(final String styleSheet) {
            scene.getStylesheets().add(styleSheet);
        }

        /**
         * Show the given Notification on the screen
         * @param notification
         */
        public void notify(final Notification notification) {
            preOrder();
            showPopup(notification);
        }

        /**
         * Show a Notification with the given parameters on the screen
         * @param title
         * @param message
         * @param image
         */
        public void notify(final String title, final String message, final Image image) {
            notify(new Notification(title, message, image));
        }

        /**
         * Show a Notification with the given title and message and an Info icon
         * @param title
         * @param message
         */
        public void notifyInfo(final String title, final String message) {
            notify(new Notification(title, message, Notification.INFO_ICON));
        }

        /**
         * Show a Notification with the given title and message and a Warning icon
         * @param title
         * @param message
         */
        public void notifyWarning(final String title, final String message) {
            notify(new Notification(title, message, Notification.WARNING_ICON));
        }

        /**
         * Show a Notification with the given title and message and a Checkmark icon
         * @param title
         * @param message
         */
        public void notifySuccess(final String title, final String message) {
            notify(new Notification(title, message, Notification.SUCCESS_ICON));
        }

        /**
         * Show a Notification with the given title and message and an Error icon
         * @param title
         * @param message
         */
        public void notifyError(final String title, final String message) {
            notify(new Notification(title, message, Notification.ERROR_ICON));
        }

        /**
         * Returns true if the popup parent stage is always on top
         * @return true if the popup parent stage is always on top
         */
        public boolean isAlwaysOnTop() { return stage.isAlwaysOnTop(); }

        /**
         * Enables/Disables always on top for the popup parent stage
         * @param alwaysOnTop
         */
        public void setAlwaysOnTop(final boolean alwaysOnTop) { stage.setAlwaysOnTop(alwaysOnTop); }

        /**
         * Makes sure that the given VALUE is within the range of MIN to MAX
         * @param min
         * @param max
         * @param value
         * @return
         */
        private double clamp(final double min, final double max, final double value) {
            if (value < min) return min;
            if (value > max) return max;
            return value;
        }

        /**
         * Reorder the popup Notifications on screen so that the latest Notification will stay on top
         */
        private void preOrder() {
            if (popups.isEmpty()) return;            
            IntStream.range(0, popups.size()).parallel().forEachOrdered(
                i -> {
                    switch (popupLocation) {
                        case TOP_LEFT: case TOP_CENTER: case TOP_RIGHT: 
                            popups.get(i).setY(popups.get(i).getY() + height + spacingY); 
                            break;
                        
                        case BOTTOM_LEFT: case BOTTOM_CENTER: case BOTTOM_RIGHT:                             
                            popups.get(i).setY(popups.get(i).getY() - height - spacingY);                             
                            break;
                        
                        default: 
                            popups.get(i).setY(popups.get(i).getY() - height - spacingY);
                            break;
                    }    
                }
            );
        }

        /**
         * Creates and shows a popup with the data from the given Notification object
         * @param notification
         */
        private void showPopup(final Notification notification) {
            ImageView icon = new ImageView(notification.image);
            icon.setFitWidth(ICON_WIDTH);
            icon.setFitHeight(ICON_HEIGHT);

            Label title = new Label(notification.title, icon);
            title.getStyleClass().add("title");

            double winSpacer = OperatingSystem.WINDOWS == operatingSystem ? 29 : 0;

            TextArea msgArea = new TextArea(notification.message);
            msgArea.setMaxWidth(width - 2 * DEFAULT_POPUP_PADDING - winSpacer);
            msgArea.setTranslateX(winSpacer);
            msgArea.setFocusTraversable(false);
            msgArea.setEditable(false);
            msgArea.getStyleClass().add("msg-area");

            VBox popupLayout = new VBox();
            popupLayout.setSpacing(DEFAULT_POPUP_SPACING);
            popupLayout.setPadding(new Insets(DEFAULT_POPUP_PADDING));
            popupLayout.getChildren().addAll(title, msgArea);

            StackPane popupContent = new StackPane();
            popupContent.setPrefSize(width, height);
            popupContent.getStyleClass().add(OperatingSystem.WINDOWS == operatingSystem ? "notification-win" : "notification-mac");
            popupContent.getChildren().addAll(popupLayout);

            final Popup popup = new Popup();
            popup.setX(getX());
            popup.setY(getY());
            popup.getContent().add(popupContent);
            popup.addEventHandler(MouseEvent.MOUSE_PRESSED, new WeakEventHandler<>(event ->
                fireNotificationEvent(new NotificationEvent(notification, Notifier.this, popup, NotificationEvent.NOTIFICATION_PRESSED))
            ));            
            popups.add(popup);

            // Add a timeline for popup fade out
            KeyValue fadeOutBegin = new KeyValue(popup.opacityProperty(), 1.0);
            KeyValue fadeOutEnd   = new KeyValue(popup.opacityProperty(), 0.0);

            KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
            KeyFrame kfEnd   = new KeyFrame(popupAnimationTime, fadeOutEnd);

            Timeline timeline = new Timeline(kfBegin, kfEnd);
            timeline.setDelay(popupLifetime);
            timeline.setOnFinished(actionEvent -> Platform.runLater(() -> {
                popup.hide();
                popups.remove(popup);
                fireNotificationEvent(new NotificationEvent(notification, Notifier.this, popup, NotificationEvent.HIDE_NOTIFICATION));
            }));

            if (stage.isShowing()) {
                stage.toFront();
            } else {
                stage.show();
            }

            popup.show(stage);
            fireNotificationEvent(new NotificationEvent(notification, Notifier.this, popup, NotificationEvent.SHOW_NOTIFICATION));
            timeline.play();
        }

        private double getX() {
            if (null == stageRef) return calcX( 0.0, Screen.getPrimary().getBounds().getWidth() );

            return calcX(stageRef.getX(), stageRef.getWidth());
        }
        private double getY() {
            if (null == stageRef) return calcY( 0.0, Screen.getPrimary().getBounds().getHeight() );            
            return calcY(stageRef.getY(), stageRef.getHeight());
        }

        private double calcX(final double left, final double totalWidth) {
            switch (popupLocation) {
                case TOP_LEFT  : case CENTER_LEFT : case BOTTOM_LEFT  : return left + offsetX;
                case TOP_CENTER: case CENTER      : case BOTTOM_CENTER: return left + (totalWidth - width) * 0.5 - offsetX;
                case TOP_RIGHT : case CENTER_RIGHT: case BOTTOM_RIGHT : return left + totalWidth - width - offsetX;
                default: return 0.0;
            }
        }
        private double calcY(final double top, final double totalHeight ) {
            switch (popupLocation) {
                case TOP_LEFT   : case TOP_CENTER   : case TOP_RIGHT   : return top + offsetY;
                case CENTER_LEFT: case CENTER       : case CENTER_RIGHT: return top + (totalHeight- height)/2 - offsetY;
                case BOTTOM_LEFT: case BOTTOM_CENTER: case BOTTOM_RIGHT: return top + totalHeight - height - offsetY;
                default: return 0.0;
            }
        }
        
        
        // ******************** Event handling ********************************
        public final ObjectProperty<EventHandler<NotificationEvent>> onNotificationPressedProperty() { return onNotificationPressed; }
        public final void setOnNotificationPressed(EventHandler<NotificationEvent> value) { onNotificationPressedProperty().set(value); }
        public final EventHandler<NotificationEvent> getOnNotificationPressed() { return onNotificationPressedProperty().get(); }
        private ObjectProperty<EventHandler<NotificationEvent>> onNotificationPressed = new ObjectPropertyBase<EventHandler<NotificationEvent>>() {
            @Override public Object getBean() { return this; }
            @Override public String getName() { return "onNotificationPressed";}
        };

        public final ObjectProperty<EventHandler<NotificationEvent>> onShowNotificationProperty() { return onShowNotification; }
        public final void setOnShowNotification(EventHandler<NotificationEvent> value) { onShowNotificationProperty().set(value); }
        public final EventHandler<NotificationEvent> getOnShowNotification() { return onShowNotificationProperty().get(); }
        private ObjectProperty<EventHandler<NotificationEvent>> onShowNotification = new ObjectPropertyBase<EventHandler<NotificationEvent>>() {
            @Override public Object getBean() { return this; }
            @Override public String getName() { return "onShowNotification";}
        };

        public final ObjectProperty<EventHandler<NotificationEvent>> onHideNotificationProperty() { return onHideNotification; }
        public final void setOnHideNotification(EventHandler<NotificationEvent> value) { onHideNotificationProperty().set(value); }
        public final EventHandler<NotificationEvent> getOnHideNotification() { return onHideNotificationProperty().get(); }
        private ObjectProperty<EventHandler<NotificationEvent>> onHideNotification = new ObjectPropertyBase<EventHandler<NotificationEvent>>() {
            @Override public Object getBean() { return this; }
            @Override public String getName() { return "onHideNotification";}
        };


        public void fireNotificationEvent(final NotificationEvent event) {
            final EventType TYPE = event.getEventType();
            final EventHandler<NotificationEvent> handler;
            if (NotificationEvent.NOTIFICATION_PRESSED == TYPE) {
                handler = getOnNotificationPressed();
            } else if (NotificationEvent.SHOW_NOTIFICATION == TYPE) {
                handler = getOnShowNotification();
            } else if (NotificationEvent.HIDE_NOTIFICATION == TYPE) {
                handler = getOnHideNotification();
            } else {
                handler = null;
            }
            if (null == handler) return;
            handler.handle(event);
        }

    }
}
