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

package eu.hansolo.fx.jdkmon;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import eu.hansolo.fx.jdkmon.controls.MacOSWindowButton;
import eu.hansolo.fx.jdkmon.controls.WinWindowButton;
import eu.hansolo.fx.jdkmon.controls.WindowButtonSize;
import eu.hansolo.fx.jdkmon.controls.WindowButtonType;
import eu.hansolo.fx.jdkmon.notification.Notification;
import eu.hansolo.fx.jdkmon.notification.NotificationBuilder;
import eu.hansolo.fx.jdkmon.notification.NotifierBuilder;
import eu.hansolo.fx.jdkmon.tools.Detector;
import eu.hansolo.fx.jdkmon.tools.Detector.MacOSAccentColor;
import eu.hansolo.fx.jdkmon.tools.Detector.OperatingSystem;
import eu.hansolo.fx.jdkmon.tools.Distribution;
import eu.hansolo.fx.jdkmon.tools.Finder;
import eu.hansolo.fx.jdkmon.tools.Fonts;
import eu.hansolo.fx.jdkmon.tools.Helper;
import eu.hansolo.fx.jdkmon.tools.PropertyManager;
import eu.hansolo.fx.jdkmon.tools.ResizeHelper;
import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.ArchiveType;
import io.foojay.api.discoclient.pkg.Pkg;
import io.foojay.api.discoclient.util.OutputFormat;
import io.foojay.api.discoclient.util.ReadableConsumerByteChannel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * User: hansolo
 * Date: 24.03.21
 * Time: 15:35
 */
public class Main extends Application {
    private static final long                                          INITIAL_DELAY_IN_HOURS   = 3;
    private static final long                                          RESCAN_INTERVAL_IN_HOURS = 3;
    private static final PseudoClass                                   DARK_MODE_PSEUDO_CLASS   = PseudoClass.getPseudoClass("dark");
    private final        Image                                         dukeNotificationIcon     = new Image(Main.class.getResourceAsStream("duke_notification.png"));
    private final        Image                                         dukeStageIcon            = new Image(Main.class.getResourceAsStream("icon128x128.png"));
    private              io.foojay.api.discoclient.pkg.OperatingSystem operatingSystem          = DiscoClient.getOperatingSystem();
    private              String                                        cssFile;
    private              Notification.Notifier                         notifier;
    private              BooleanProperty                               darkMode;
    private              MacOSAccentColor                              accentColor;
    private              AnchorPane                                    headerPane;
    private              MacOSWindowButton                             closeMacWindowButton;
    private              WinWindowButton                               closeWinWindowButton;
    private              Label                                         windowTitle;
    private              StackPane                                     pane;
    private              BorderPane                                    mainPane;
    private              ScheduledExecutorService                      executor;
    private              boolean                                       hideMenu;
    private              Stage                                         stage;
    private              ObservableList<Distribution>                  distros;
    private              Finder                                        finder;
    private              Label                                         titleLabel;
    private              Label                                         searchPathLabel;
    private              VBox                                          titleBox;
    private              Separator                                     separator;
    private              VBox                                          distroBox;
    private              VBox                                          vBox;
    private              List<String>                                  searchPaths;
    private              DirectoryChooser                              directoryChooser;
    private              ProgressBar                                   progressBar;
    private              DiscoClient                                   discoclient;
    private              BooleanProperty                               blocked;
    private              AtomicBoolean                                 checkingForUpdates;
    private              boolean                                       trayIconSupported;
    private              ContextMenu                                   contextMenu;
    private              Worker<Boolean>                               worker;


    @Override public void init() {
        switch (operatingSystem) {
            case WINDOWS -> cssFile = "jdk-mon-win.css";
            case LINUX   -> cssFile = "jdk-mon-linux.css";
            default      -> cssFile = "jdk-mon.css";
        }

        notifier = NotifierBuilder.create()
                                  .popupLocation(OperatingSystem.MACOS == Detector.getOperatingSystem() ? Pos.TOP_RIGHT : Pos.BOTTOM_RIGHT)
                                  .popupLifeTime(Duration.millis(5000))
                                  .build();

        pane     = new StackPane();
        darkMode = new BooleanPropertyBase(false) {
            @Override protected void invalidated() { pane.pseudoClassStateChanged(DARK_MODE_PSEUDO_CLASS, get()); }
            @Override public Object getBean() { return Main.this; }
            @Override public String getName() { return "darkMode"; }
        };
        darkMode.set(Detector.isDarkMode());

        closeMacWindowButton = new MacOSWindowButton(WindowButtonType.CLOSE, WindowButtonSize.SMALL);
        closeMacWindowButton.setDarkMode(darkMode.get());

        closeWinWindowButton = new WinWindowButton(WindowButtonType.CLOSE, WindowButtonSize.SMALL);
        closeWinWindowButton.setDarkMode(darkMode.get());

        windowTitle = new Label("JDK Mon");
        if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
            windowTitle.setFont(Fonts.segoeUi(9));
            windowTitle.setTextFill(darkMode.get() ? Color.web("#969696") : Color.web("#000000"));
            windowTitle.setAlignment(Pos.CENTER_LEFT);
            windowTitle.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(darkMode.get() ? "duke.png" : "duke_blk.png"), 12, 12, true, false)));
            windowTitle.setGraphicTextGap(10);

            AnchorPane.setTopAnchor(closeWinWindowButton, 0d);
            AnchorPane.setRightAnchor(closeWinWindowButton, 0d);
            AnchorPane.setTopAnchor(windowTitle, 0d);
            AnchorPane.setRightAnchor(windowTitle, 0d);
            AnchorPane.setBottomAnchor(windowTitle, 0d);
            AnchorPane.setLeftAnchor(windowTitle, 10d);
        } else {
            windowTitle.setFont(Fonts.sfProTextMedium(12));
            windowTitle.setTextFill(darkMode.get() ? Color.web("#dddddd") : Color.web("#000000"));
            windowTitle.setAlignment(Pos.CENTER);

            AnchorPane.setTopAnchor(closeMacWindowButton, 5d);
            AnchorPane.setLeftAnchor(closeMacWindowButton, 5d);
            AnchorPane.setTopAnchor(windowTitle, 0d);
            AnchorPane.setRightAnchor(windowTitle, 0d);
            AnchorPane.setBottomAnchor(windowTitle, 0d);
            AnchorPane.setLeftAnchor(windowTitle, 0d);
        }
        windowTitle.setMouseTransparent(true);

        headerPane = new AnchorPane();
        headerPane.getStyleClass().add("header");
        headerPane.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.1), 1, 0.0, 0, 1));
        if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
            headerPane.setMinHeight(31);
            headerPane.setMaxHeight(31);
            headerPane.setPrefHeight(31);
            headerPane.getChildren().addAll(closeWinWindowButton, windowTitle);
        } else {
            headerPane.setMinHeight(21);
            headerPane.setMaxHeight(21);
            headerPane.setPrefHeight(21);
            headerPane.getChildren().addAll(closeMacWindowButton, windowTitle);
        }

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> rescan(), INITIAL_DELAY_IN_HOURS, RESCAN_INTERVAL_IN_HOURS, TimeUnit.HOURS);

        discoclient        = new DiscoClient("JDKMon");
        blocked            = new SimpleBooleanProperty(false);
        checkingForUpdates = new AtomicBoolean(false);
        searchPaths        = new ArrayList<>(Arrays.asList(PropertyManager.INSTANCE.getString(PropertyManager.SEARCH_PATH).split(",")));
        
        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose search path");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        distros = FXCollections.observableArrayList();
        finder  = new Finder(discoclient);
        distros.setAll(finder.getDistributions(searchPaths));

        titleLabel = new Label("Distributions found in");
        titleLabel.setFont(io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem ? Fonts.segoeUi(12) : Fonts.sfProTextBold(12));

        searchPathLabel = new Label(searchPaths.stream().collect(Collectors.joining(",")));
        searchPathLabel.getStyleClass().add("small-label");

        titleBox = new VBox(5, titleLabel, searchPathLabel);

        List<HBox> distroEntries = new ArrayList<>();
        finder.getAvailableUpdates(distros).entrySet().forEach(entry -> distroEntries.add(getDistroEntry(entry.getKey(), entry.getValue())));
        distroBox = new VBox(10);
        distroBox.getChildren().setAll(distroEntries);

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);

        separator = new Separator(Orientation.HORIZONTAL);
        VBox.setMargin(separator, new Insets(5, 0, 5, 0));

        vBox = new VBox(5, titleBox, separator, distroBox, progressBar);

        pane.getChildren().add(vBox);
        pane.getStyleClass().add("jdk-mon");
        pane.setPadding(new Insets(10));

        mainPane = new BorderPane();
        mainPane.setTop(headerPane);
        mainPane.setCenter(pane);

        // Context menu
        MenuItem contextRescan = new MenuItem("Rescan");
        contextRescan.getStyleClass().add("context-menu-item");
        contextRescan.setOnAction(e -> rescan());

        MenuItem contextAddSearchPath = new MenuItem("Add search path");
        contextAddSearchPath.getStyleClass().add("context-menu-item");
        contextAddSearchPath.setOnAction(e -> selectSearchPath());

        MenuItem contextDefaultSearchPath = new MenuItem("Default search path");
        contextDefaultSearchPath.getStyleClass().add("context-menu-item");
        contextDefaultSearchPath.setOnAction(e -> resetToDefaultSearchPath());

        contextMenu = new ContextMenu(contextRescan, contextAddSearchPath, contextDefaultSearchPath);
        contextMenu.getStyleClass().add("jdk-mon");
        contextMenu.setAutoHide(true);

        // Adjustments related to accent color
        if (Detector.OperatingSystem.MACOS == Detector.getOperatingSystem()) {
            accentColor = Detector.getMacOSAccentColor();
            if (darkMode.get()) {
                pane.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorDark()));
                contextMenu.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorDark()));
            } else {
                pane.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorAqua()));
                contextMenu.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorAqua()));
            }
        } else {
            accentColor = MacOSAccentColor.MULTI_COLOR;
        }

        // Adjustments related to dark/light mode
        if (darkMode.get()) {
            if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
                headerPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), CornerRadii.EMPTY, Insets.EMPTY)));
                headerPane.setBorder(new Border(new BorderStroke(Color.web("#f2f2f2"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0.5, 0))));
                pane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), CornerRadii.EMPTY, Insets.EMPTY)));
                mainPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), CornerRadii.EMPTY, Insets.EMPTY)));
                mainPane.setBorder(new Border(new BorderStroke(Color.web("#333333"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 1, 1, 1))));
            } else {
                headerPane.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                pane.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(0, 0, 10, 10, false), Insets.EMPTY)));
                mainPane.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(10), Insets.EMPTY)));
                mainPane.setBorder(new Border(new BorderStroke(Color.web("#515352"), BorderStrokeStyle.SOLID, new CornerRadii(10, 10, 10, 10, false), new BorderWidths(1))));
            }
        } else {
            if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
                headerPane.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), CornerRadii.EMPTY, Insets.EMPTY)));
                headerPane.setBorder(new Border(new BorderStroke(Color.web("#f2f2f2"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0.5, 0))));
                pane.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), CornerRadii.EMPTY, Insets.EMPTY)));
                mainPane.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), CornerRadii.EMPTY, Insets.EMPTY)));
                mainPane.setBorder(new Border(new BorderStroke(Color.web("#f2f2f2"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 1, 1, 1))));
            } else {
                headerPane.setBackground(new Background(new BackgroundFill(Color.web("#efedec"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                pane.setBackground(new Background(new BackgroundFill(Color.web("#ecebe9"), new CornerRadii(0, 0, 10, 10, false), Insets.EMPTY)));
                mainPane.setBackground(new Background(new BackgroundFill(Color.web("#ecebe9"), new CornerRadii(10), Insets.EMPTY)));
                mainPane.setBorder(new Border(new BorderStroke(Color.web("#f6f4f4"), BorderStrokeStyle.SOLID, new CornerRadii(10, 10, 10, 10, false), new BorderWidths(1))));
            }
        }

        registerListeners();
    }

    private void registerListeners() {
        headerPane.setOnMousePressed(press -> headerPane.setOnMouseDragged(drag -> {
            stage.setX(drag.getScreenX() - press.getSceneX());
            stage.setY(drag.getScreenY() - press.getSceneY());
        }));

        if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
            closeWinWindowButton.setOnMouseReleased((Consumer<MouseEvent>) e -> {
                if (stage.isShowing()) {
                    if (trayIconSupported) {
                        stage.hide();
                    } else {
                        stage.setMaximized(false);
                    }
                } else {
                    if (trayIconSupported) {
                        stage.show();
                    } else {
                        stage.setWidth(330);
                        stage.setHeight(242);
                        stage.centerOnScreen();
                    }
                }
            });
            closeWinWindowButton.setOnMouseEntered(e -> closeWinWindowButton.setHovered(true));
            closeWinWindowButton.setOnMouseExited(e -> closeWinWindowButton.setHovered(false));
        } else {
            closeMacWindowButton.setOnMouseReleased((Consumer<MouseEvent>) e -> {
                if (stage.isShowing()) {
                    if (trayIconSupported) {
                        stage.hide();
                    } else {
                        stage.setIconified(true);
                    }
                } else {
                    if (trayIconSupported) {
                        stage.show();
                    } else {
                        stage.setWidth(330);
                        stage.setHeight(242);
                        stage.centerOnScreen();
                    }
                }
            });
            closeMacWindowButton.setOnMouseEntered(e -> closeMacWindowButton.setHovered(true));
            closeMacWindowButton.setOnMouseExited(e -> closeMacWindowButton.setHovered(false));
        }

        mainPane.setOnContextMenuRequested(e -> contextMenu.show(mainPane, e.getScreenX(), e.getScreenY()));
        mainPane.setOnMousePressed(e -> contextMenu.hide());

        progressBar.prefWidthProperty().bind(mainPane.widthProperty());
    }


    @Override public void start(final Stage stage) {
        this.stage             = stage;
        this.trayIconSupported = FXTrayIcon.isSupported();

        if (trayIconSupported) {
            FXTrayIcon trayIcon = new FXTrayIcon(stage, getClass().getResource("duke.png"));
            trayIcon.setTrayIconTooltip("JDK Mon");
            trayIcon.addExitItem(false);

            MenuItem rescanItem = new MenuItem("Rescan");
            rescanItem.setOnAction(e -> rescan());
            trayIcon.addMenuItem(rescanItem);

            MenuItem searchPathItem = new MenuItem("Add search path");
            searchPathItem.setOnAction(e -> selectSearchPath());
            trayIcon.addMenuItem(searchPathItem);

            MenuItem defaultSearchPathItem = new MenuItem("Default search path");
            defaultSearchPathItem.setOnAction(e -> resetToDefaultSearchPath());
            trayIcon.addMenuItem(defaultSearchPathItem);

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.setOnAction(e -> stop());
            trayIcon.addMenuItem(exitItem);

            trayIcon.show();
        } else {
            MenuBar menuBar = new MenuBar();
            menuBar.useSystemMenuBarProperty().set(true);
            menuBar.setTranslateX(16);

            Menu menu = new Menu("JDK Mon");
            menu.setOnShowing(e -> hideMenu = false);
            menu.setOnHidden(e -> {
                if (!hideMenu) {
                    menu.show();
                }
            });

            CustomMenuItem mainItem = new CustomMenuItem();
            Label mainLabel = new Label("JDK Mon");
            mainLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            mainLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            mainItem.setContent(mainLabel);
            mainItem.setHideOnClick(false);
            mainItem.setOnAction(e -> {
                stage.setWidth(330);
                stage.setHeight(242);
                stage.centerOnScreen();
            });
            menu.getItems().add(mainItem);

            CustomMenuItem rescanItem = new CustomMenuItem();
            Label rescanLabel = new Label("Rescan");
            rescanLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            rescanLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            rescanItem.setContent(rescanLabel);
            rescanItem.setHideOnClick(false);
            rescanItem.setOnAction(e -> rescan());
            menu.getItems().add(rescanItem);

            CustomMenuItem searchPathItem = new CustomMenuItem();
            Label searchPathLabel = new Label("Add search path");
            searchPathLabel.setTooltip(new Tooltip("Add another folder that should be scanned for JDK's"));
            searchPathLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            searchPathLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            searchPathItem.setContent(searchPathLabel);
            searchPathItem.setHideOnClick(false);
            searchPathItem.setOnAction( e -> selectSearchPath());
            menu.getItems().add(searchPathItem);

            CustomMenuItem defaultSearchPathItem = new CustomMenuItem();
            Label defaultSearchPathLabel = new Label("Default search path");
            defaultSearchPathLabel.setTooltip(new Tooltip("Reset search paths to default"));
            defaultSearchPathLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            defaultSearchPathLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            defaultSearchPathItem.setContent(defaultSearchPathLabel);
            defaultSearchPathItem.setHideOnClick(false);
            defaultSearchPathItem.setOnAction( e -> resetToDefaultSearchPath());
            menu.getItems().add(defaultSearchPathItem);

            CustomMenuItem exitItem = new CustomMenuItem();
            Label exitLabel = new Label("Exit");
            exitLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            exitLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            exitItem.setContent(exitLabel);
            exitItem.setHideOnClick(false);
            exitItem.setOnAction(e -> stop());
            menu.getItems().add(exitItem);

            menuBar.getMenus().add(menu);

            mainPane.getChildren().add(menuBar);
        }

        Scene scene = new Scene(mainPane);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(Main.class.getResource(cssFile).toExternalForm());

        stage.setTitle("JDK Mon");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
        stage.getIcons().add(dukeStageIcon);
        stage.centerOnScreen();
        stage.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                if (darkMode.get()) {
                    if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
                        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        windowTitle.setTextFill(Color.web("#969696"));
                    } else {
                        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        windowTitle.setTextFill(Color.web("#dddddd"));
                    }
                } else {
                    if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
                        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        windowTitle.setTextFill(Color.web("#000000"));
                    } else {
                        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#edefef"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        windowTitle.setTextFill(Color.web("#000000"));
                    }
                }
                closeMacWindowButton.setDisable(false);
                closeWinWindowButton.setDisable(false);
            } else {
                if (darkMode.get()) {
                    if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
                        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        windowTitle.setTextFill(Color.web("#969696"));
                    } else {
                        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#282927"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        windowTitle.setTextFill(Color.web("#696a68"));
                    }
                } else {
                    if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
                        closeWinWindowButton.setStyle("-fx-fill: #969696;");
                    } else {
                        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#e5e7e7"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        windowTitle.setTextFill(Color.web("#a9a6a6"));
                        closeMacWindowButton.setStyle("-fx-fill: #ceccca;");
                    }
                }
                closeMacWindowButton.setDisable(true);
                closeWinWindowButton.setDisable(true);
            }
        });

        progressBar.prefWidthProperty().bind(stage.widthProperty());

        ResizeHelper.addResizeListener(stage);
    }

    @Override public void stop() {
        executor.shutdownNow();
        Platform.exit();
        System.exit(0);
    }


    private void rescan() {
        Platform.runLater(() -> {
            if (checkingForUpdates.get()) { return; }
            Set<Distribution> distrosFound = finder.getDistributions(searchPaths);
            distros.setAll(distrosFound);
            checkForUpdates();
        });
    }

    private void checkForUpdates() {
        checkingForUpdates.set(true);
        AtomicBoolean updatesAvailable = new AtomicBoolean(false);
        StringBuilder msgBuilder       = new StringBuilder();
        List<Node>    distroEntries    = new ArrayList<>();

        finder.getAvailableUpdates(distros).entrySet().forEach(entry -> {
            HBox distroEntry = getDistroEntry(entry.getKey(), entry.getValue());
            distroEntries.add(distroEntry);
            if (distroEntry.getChildren().size() > 1 && distroEntry.getChildren().get(2) instanceof Label) {
                msgBuilder.append(entry.getKey().getName()).append(" ").append(((Label)distroEntry.getChildren().get(2)).getText()).append("\n");
                updatesAvailable.set(true);
            }
        });
        Platform.runLater(() -> {
            int numberOfDistros = distroBox.getChildren().size();
            distroBox.getChildren().setAll(distroEntries);
            double delta = (distroEntries.size() - numberOfDistros) * 28;
            stage.setHeight(stage.getHeight() + delta);
            mainPane.layout();
        });

        if (updatesAvailable.get()) {
            Notification notification = NotificationBuilder.create().title("New updates available").message(msgBuilder.toString()).image(dukeNotificationIcon).build();
            notifier.notify(notification);
        }

        checkingForUpdates.set(false);
    }

    private HBox getDistroEntry(final Distribution distribution, final List<Pkg> pkgs) {
        Label distroLabel = new Label(new StringBuilder(distribution.getName()).append(distribution.getFxBundled() ? " (FX)" : "").append("  ").append(distribution.getVersion()).toString());
        distroLabel.setMinWidth(180);
        distroLabel.setAlignment(Pos.CENTER_LEFT);
        distroLabel.setMaxWidth(Double.MAX_VALUE);

        HBox hBox = new HBox(5, distroLabel);
        hBox.setMinWidth(360);

        if (pkgs.isEmpty()) { return hBox; }

        Optional<Pkg> optionalZulu = pkgs.parallelStream().sorted(Comparator.comparing(Pkg::getDistributionName).reversed()).findFirst();

        Pkg     firstPkg         = pkgs.get(0);
        String  nameToCheck      = firstPkg.getDistribution().getApiString();
        Boolean fxBundledToCheck = firstPkg.isJavaFXBundled();
        String  versionToCheck   = firstPkg.getJavaVersion().getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, false);
        for (Distribution distro : distros) {
            if (distro.getApiString().equals(nameToCheck) && distro.getVersion().equals(versionToCheck) && distro.getFxBundled() == fxBundledToCheck) {
                return hBox;
            }
        }

        Label  arrowLabel   = new Label(" -> ");
        hBox.getChildren().add(arrowLabel);


        // ******************** Create popup **********************************
        Popup popup = new Popup();


        WinWindowButton   closePopupWinButton   = new WinWindowButton(WindowButtonType.CLOSE, WindowButtonSize.SMALL);
        MacOSWindowButton closePopupMacOSButton = new MacOSWindowButton(WindowButtonType.CLOSE, WindowButtonSize.SMALL);

        if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
            closePopupWinButton.setDarkMode(darkMode.get());
            closePopupWinButton.setOnMouseReleased((Consumer<MouseEvent>) e -> popup.hide());
            closePopupWinButton.setOnMouseEntered(e -> closePopupWinButton.setHovered(true));
            closePopupWinButton.setOnMouseExited(e -> closePopupWinButton.setHovered(false));
        } else {
            closePopupMacOSButton.setDarkMode(darkMode.get());
            closePopupMacOSButton.setOnMouseReleased((Consumer<MouseEvent>) e -> popup.hide());
            closePopupMacOSButton.setOnMouseEntered(e -> closePopupMacOSButton.setHovered(true));
            closePopupMacOSButton.setOnMouseExited(e -> closePopupMacOSButton.setHovered(false));
        }
        Label popupTitle = new Label("Alternative distribution");
        popupTitle.setFont(io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem ? Fonts.segoeUiSemiBold(12) : Fonts.sfProTextMedium(12));
        popupTitle.setTextFill(darkMode.get() ? Color.web("#dddddd") : Color.web("#000000"));
        popupTitle.setMouseTransparent(true);
        popupTitle.setAlignment(Pos.CENTER);

        AnchorPane.setTopAnchor(closePopupMacOSButton, 5d);
        AnchorPane.setLeftAnchor(closePopupMacOSButton, 5d);
        AnchorPane.setTopAnchor(closePopupWinButton, 5d);
        AnchorPane.setLeftAnchor(closePopupWinButton, 5d);
        AnchorPane.setTopAnchor(popupTitle, 0d);
        AnchorPane.setRightAnchor(popupTitle, 0d);
        AnchorPane.setBottomAnchor(popupTitle, 0d);
        AnchorPane.setLeftAnchor(popupTitle, 0d);

        AnchorPane popupHeader = new AnchorPane();
        popupHeader.getStyleClass().add("header");
        popupHeader.setMinHeight(21);
        popupHeader.setMaxHeight(21);
        popupHeader.setPrefHeight(21);
        popupHeader.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.1), 1, 0.0, 0, 1));
        if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
            popupHeader.getChildren().addAll(closePopupWinButton, popupTitle);
        } else {
            popupHeader.getChildren().addAll(closePopupMacOSButton, popupTitle);
        }

        Label popupMsg;
        if (optionalZulu.isPresent()) {
            popupMsg = new Label(optionalZulu.get().getDistribution().getUiString() + " " + optionalZulu.get().getJavaVersion().toString(true) + " available");
        } else {
            popupMsg = new Label(firstPkg.getDistribution().getUiString() + " " + firstPkg.getJavaVersion().toString(true) + " available");
        }
        popupMsg.setTextFill(darkMode.get() ? Color.web("#dddddd") : Color.web("#868687"));
        popupMsg.getStyleClass().add("msg-label");

        HBox otherDistroPkgsBox = new HBox(5);

        VBox popupContent = new VBox(5, popupMsg, otherDistroPkgsBox);
        popupContent.setPadding(new Insets(10));

        BorderPane popupPane = new BorderPane();
        popupPane.getStyleClass().add("popup");
        popupPane.setTop(popupHeader);
        popupPane.setCenter(popupContent);

        // Adjustments related to dark/light mode
        if (darkMode.get()) {
            if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
                // TODO: Set popup windows style
            } else {
                popupHeader.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                popupContent.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(0, 0, 10, 10, false), Insets.EMPTY)));
                popupPane.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(10), Insets.EMPTY)));
                popupPane.setBorder(new Border(new BorderStroke(Color.web("#515352"), BorderStrokeStyle.SOLID, new CornerRadii(10, 10, 10, 10, false), new BorderWidths(1))));
            }
        } else {
            if (io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem) {
                // TODO: Set popup windows style
            } else {
                popupHeader.setBackground(new Background(new BackgroundFill(Color.web("#efedec"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                popupContent.setBackground(new Background(new BackgroundFill(Color.web("#e3e5e5"), new CornerRadii(0, 0, 10, 10, false), Insets.EMPTY)));
                popupPane.setBackground(new Background(new BackgroundFill(Color.web("#ecebe9"), new CornerRadii(10), Insets.EMPTY)));
                popupPane.setBorder(new Border(new BorderStroke(Color.web("#f6f4f4"), BorderStrokeStyle.SOLID, new CornerRadii(10, 10, 10, 10, false), new BorderWidths(1))));
            }
        }

        popup.getContent().add(popupPane);
        // ********************************************************************

        if (distribution.getApiString().equals(nameToCheck)) {
            Label versionLabel = new Label(firstPkg.getJavaVersion().toString(true));
            versionLabel.setMinWidth(56);
            hBox.getChildren().add(versionLabel);
        } else {
            // There is a newer update for the currently installed version from another distribution
            Region infoIcon = new Region();
            infoIcon.getStyleClass().add("icon");
            infoIcon.setId("info");
            infoIcon.setOnMousePressed(e -> {
                if (null != popup) {
                    popup.setX(e.getScreenX() + 10);
                    popup.setY(e.getScreenY() + 10);
                    popup.show(stage);
                }
            });
            hBox.getChildren().add(infoIcon);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hBox.getChildren().add(spacer);

        Collections.sort(pkgs, Comparator.comparing(Pkg::getArchiveType));
        pkgs.forEach(pkg -> {
                ArchiveType archiveType      = pkg.getArchiveType();
                Label       archiveTypeLabel = new Label(archiveType.getUiString());
                archiveTypeLabel.getStyleClass().add("tag-label");
                if (pkg.isDirectlyDownloadable()) {
                    archiveTypeLabel.setTooltip(new Tooltip("Download " + pkg.getFileName()));
                    switch (archiveType) {
                        case APK, BIN, CAB, EXE, MSI, ZIP -> archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacOSAccentColor.GREEN.getColorDark() : MacOSAccentColor.GREEN.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
                        case DEB, TAR, TAR_GZ, TAR_Z, RPM -> archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacOSAccentColor.ORANGE.getColorDark() : MacOSAccentColor.ORANGE.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
                        case PKG, DMG -> archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacOSAccentColor.YELLOW.getColorDark() : MacOSAccentColor.YELLOW.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
                    }
                } else {
                    archiveTypeLabel.setTooltip(new Tooltip("No direct download, please check website"));
                    archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacOSAccentColor.GRAPHITE.getColorDark() : MacOSAccentColor.GRAPHITE.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
                }
                archiveTypeLabel.disableProperty().bind(blocked);
                if (pkg.isDirectlyDownloadable()) {
                    archiveTypeLabel.setOnMouseClicked(e -> { if (!blocked.get()) { downloadPkg(pkg); }});
                }

                if (pkg.getDistribution().getApiString().equals(distribution.getApiString())) {
                    hBox.getChildren().add(archiveTypeLabel);
                } else {
                    // Add tags to popup
                    if (optionalZulu.isPresent() && pkg.equals(optionalZulu.get())) {
                        otherDistroPkgsBox.getChildren().add(archiveTypeLabel);
                    } else if (pkg.equals(firstPkg)) {
                        otherDistroPkgsBox.getChildren().add(archiveTypeLabel);
                    }
                }
        });
        return hBox;
    }

    private void downloadPkg(final Pkg pkg) {
        if (null == pkg) { return; }
        directoryChooser.setTitle("Choose folder for download");
        final File targetFolder = directoryChooser.showDialog(stage);
        if (null != targetFolder) {
            final String directDownloadUri = discoclient.getPkgDirectDownloadUri(pkg.getId());
            if (null == directDownloadUri) {
                new Alert(AlertType.ERROR, "Problem downloading the package, please try again.", ButtonType.CLOSE).show();
                return;
            }
            final String target = targetFolder.getAbsolutePath() + File.separator + pkg.getFileName();
            worker = createWorker(directDownloadUri, target);
            worker.stateProperty().addListener((o, ov, nv) -> {
                if (nv.equals(State.READY)) {
                } else if (nv.equals(State.RUNNING)) {
                    blocked.set(true);
                    progressBar.setVisible(true);
                } else if (nv.equals(State.CANCELLED)) {
                    final File file = new File(target);
                    if (file.exists()) { file.delete(); }
                    blocked.set(false);
                    progressBar.setProgress(0);
                    progressBar.setVisible(false);
                } else if (nv.equals(State.FAILED)) {
                    final File file = new File(target);
                    if (file.exists()) { file.delete(); }
                    blocked.set(false);
                    progressBar.setProgress(0);
                    progressBar.setVisible(false);
                } else if (nv.equals(State.SUCCEEDED)) {
                    blocked.set(false);
                    progressBar.setProgress(0);
                    progressBar.setVisible(false);
                } else if (nv.equals(State.SCHEDULED)) {
                    blocked.set(true);
                    progressBar.setVisible(true);
                }
            });
            worker.progressProperty().addListener((o, ov, nv) -> progressBar.setProgress(nv.doubleValue() * 100.0));
            Alert info = new Alert(AlertType.INFORMATION);
            info.setTitle("JDKMon");
            info.setHeaderText("JDKMon Download Info");
            info.setContentText("Download will be started and update will be saved to " + targetFolder);
            info.setOnCloseRequest(e -> new Thread((Runnable) worker).start());
            info.show();
        }
    }

    private Worker<Boolean> createWorker(final String uri, final String filename) {
        return new Task<>() {
            @Override protected Boolean call() {
                updateProgress(0, 100);
                try {
                    final URLConnection         connection = new URL(uri).openConnection();
                    final int                   fileSize   = connection.getContentLength();
                    ReadableByteChannel         rbc        = Channels.newChannel(connection.getInputStream());
                    ReadableConsumerByteChannel rcbc       = new ReadableConsumerByteChannel(rbc, (b) -> updateProgress((double) b / (double) fileSize, 100));
                    FileOutputStream            fos        = new FileOutputStream(filename);
                    fos.getChannel().transferFrom(rcbc, 0, Long.MAX_VALUE);
                    fos.close();
                    rcbc.close();
                    rbc.close();
                } catch (IOException ex) {
                    return Boolean.FALSE;
                }
                updateProgress(0, 100);
                return Boolean.TRUE;
            }
        };
    }

    private void selectSearchPath() {
        stage.show();
        boolean searchPathExists;
        if (searchPaths.isEmpty()) {
            searchPathExists = false;
        } else {
            searchPathExists = new File(searchPaths.get(0)).exists();
        }
        directoryChooser.setTitle("Add search path");
        directoryChooser.setInitialDirectory(searchPathExists ? new File(searchPaths.get(0)) : new File(System.getProperty("user.home")));
        final File selectedFolder = directoryChooser.showDialog(stage);
        if (null != selectedFolder) {
            String searchPath = selectedFolder.getAbsolutePath() + File.separator;
            if (searchPaths.contains(searchPath)) { return; }
            searchPaths.add(searchPath);
            PropertyManager.INSTANCE.set(PropertyManager.SEARCH_PATH, searchPaths.stream().collect(Collectors.joining(",")));
            PropertyManager.INSTANCE.storeProperties();
            searchPathLabel.setText(searchPaths.stream().collect(Collectors.joining(", ")));
            //setupFileWatcher();
            rescan();
        }
    }

    private void resetToDefaultSearchPath() {
        stage.show();
        PropertyManager.INSTANCE.resetProperties();
        distros.clear();
        searchPaths.clear();
        searchPaths.addAll(Arrays.asList(PropertyManager.INSTANCE.getString(PropertyManager.SEARCH_PATH).split(",")));
        searchPathLabel.setText(searchPaths.stream().collect(Collectors.joining(", ")));
        rescan();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
