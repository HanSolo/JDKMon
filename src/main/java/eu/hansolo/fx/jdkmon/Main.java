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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.hansolo.fx.jdkmon.controls.MacOSWindowButton;
import eu.hansolo.fx.jdkmon.controls.MacProgress;
import eu.hansolo.fx.jdkmon.controls.WinProgress;
import eu.hansolo.fx.jdkmon.controls.WinWindowButton;
import eu.hansolo.fx.jdkmon.controls.WindowButtonSize;
import eu.hansolo.fx.jdkmon.controls.WindowButtonType;
import eu.hansolo.fx.jdkmon.notification.Notification;
import eu.hansolo.fx.jdkmon.notification.NotificationBuilder;
import eu.hansolo.fx.jdkmon.notification.NotifierBuilder;
import eu.hansolo.fx.jdkmon.tools.ArchitectureCell;
import eu.hansolo.fx.jdkmon.tools.ArchiveTypeCell;
import eu.hansolo.fx.jdkmon.tools.Constants;
import eu.hansolo.fx.jdkmon.tools.Detector;
import eu.hansolo.fx.jdkmon.tools.Detector.MacOSAccentColor;
import eu.hansolo.fx.jdkmon.tools.Detector.OperatingSystem;
import eu.hansolo.fx.jdkmon.tools.Distribution;
import eu.hansolo.fx.jdkmon.tools.DistributionCell;
import eu.hansolo.fx.jdkmon.tools.Finder;
import eu.hansolo.fx.jdkmon.tools.Fonts;
import eu.hansolo.fx.jdkmon.tools.Helper;
import eu.hansolo.fx.jdkmon.tools.LibcTypeCell;
import eu.hansolo.fx.jdkmon.tools.MajorVersionCell;
import eu.hansolo.fx.jdkmon.tools.OperatingSystemCell;
import eu.hansolo.fx.jdkmon.tools.PropertyManager;
import eu.hansolo.fx.jdkmon.tools.ResizeHelper;
import eu.hansolo.fx.jdkmon.tools.UpdateLevelCell;
import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.Architecture;
import io.foojay.api.discoclient.pkg.ArchiveType;
import io.foojay.api.discoclient.pkg.LibCType;
import io.foojay.api.discoclient.pkg.MajorVersion;
import io.foojay.api.discoclient.pkg.Match;
import io.foojay.api.discoclient.pkg.PackageType;
import io.foojay.api.discoclient.pkg.Pkg;
import io.foojay.api.discoclient.pkg.ReleaseStatus;
import io.foojay.api.discoclient.pkg.Scope;
import io.foojay.api.discoclient.pkg.SemVer;
import io.foojay.api.discoclient.pkg.VersionNumber;
import io.foojay.api.discoclient.util.OutputFormat;
import io.foojay.api.discoclient.util.ReadableConsumerByteChannel;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
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
    public static final  VersionNumber                                           VERSION                = PropertyManager.INSTANCE.getVersionNumber();
    private static final PseudoClass                                             DARK_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("dark");
    private final        Image                                                   dukeNotificationIcon   = new Image(Main.class.getResourceAsStream("duke_notification.png"));
    private final        Image                                                   dukeStageIcon          = new Image(Main.class.getResourceAsStream("icon128x128.png"));
    private              io.foojay.api.discoclient.pkg.OperatingSystem           operatingSystem        = Finder.detectOperatingSystem();
    private              Architecture                                            architecture           = Finder.detectArchitecture();
    private              boolean                                                 isWindows              = io.foojay.api.discoclient.pkg.OperatingSystem.WINDOWS == operatingSystem;
    private              String                                                  cssFile;
    private              Notification.Notifier                                   notifier;
    private              BooleanProperty                                         darkMode;
    private              MacOSAccentColor                                        accentColor;
    private              AnchorPane                                              headerPane;
    private              MacOSWindowButton                                       closeMacWindowButton;
    private              WinWindowButton                                         closeWinWindowButton;
    private              Label                                                   windowTitle;
    private              StackPane                                               pane;
    private              BorderPane                                              mainPane;
    private              ScheduledExecutorService                                executor;
    private              boolean                                                 hideMenu;
    private              Stage                                                   stage;
    private              ObservableList<Distribution>                            distros;
    private              Finder                                                  finder;
    private              Label                                                   titleLabel;
    private              Label                                                   searchPathLabel;
    private              MacProgress                                             macProgressIndicator;
    private              WinProgress                                             winProgressIndicator;
    private              VBox                                                    titleBox;
    private              Separator                                               separator;
    private              VBox                                                    distroBox;
    private              VBox                                                    vBox;
    private              List<String>                                            searchPaths;
    private              DirectoryChooser                                        directoryChooser;
    private              ProgressBar                                             progressBar;
    private              DiscoClient                                             discoclient;
    private              BooleanProperty                                         blocked;
    private              AtomicBoolean                                           checkingForUpdates;
    private              boolean                                                 trayIconSupported;
    private              ContextMenu                                             contextMenu;
    private              Worker<Boolean>                                         worker;
    private              Dialog                                                  aboutDialog;
    private              Dialog                                                  downloadJDKDialog;
    private              Timeline                                                timeline;
    private              boolean                                                 isUpdateAvailable;
    private              VersionNumber                                           latestVersion;
    private              Map<String, Popup>                                      popups;
    private              Stage                                                   downloadJDKStage;
    private              AnchorPane                                              downloadJDKHeaderPane;
    private              Label                                                   downloadJDKWindowTitle;
    private              MacOSWindowButton                                       downloadJDKCloseMacWindowButton;
    private              WinWindowButton                                         downloadJDKCloseWinWindowButton;
    private              StackPane                                               downloadJDKPane;
    private              CheckBox                                                downloadJDKBundledWithFXCheckBox;
    private              ComboBox<MajorVersion>                                  downloadJDKMajorVersionComboBox;
    private              ComboBox<SemVer>                                        downloadJDKUpdateLevelComboBox;
    private              ComboBox<io.foojay.api.discoclient.pkg.Distribution>    downloadJDKDistributionComboBox;
    private              ComboBox<io.foojay.api.discoclient.pkg.OperatingSystem> downloadJDKOperatingSystemComboBox;
    private              ComboBox<LibCType>                                      downloadJDKLibcTypeComboBox;
    private              ComboBox<Architecture>                                  downloadJDKArchitectureComboBox;
    private              ComboBox<ArchiveType>                                   downloadJDKArchiveTypeComboBox;
    private              Label                                                   downloadJDKFilenameLabel;
    private              Set<MajorVersion>                                       downloadJDKMaintainedVersions;
    private              List<Pkg>                                               downloadJDKSelectedPkgs;
    private              Pkg                                                     downloadJDKSelectedPkg;
    private              List<Pkg>                                               downloadJDKSelectedPkgsForMajorVersion;
    private              List<io.foojay.api.discoclient.pkg.Distribution>        downloadJDKDistributionsThatSupportFx;
    private              boolean                                                 downloadJDKJavafxBundled;
    private              MajorVersion                                            downloadJDKSelectedMajorVersion;
    private              SemVer                                                  downloadJDKSelectedVersionNumber;
    private              io.foojay.api.discoclient.pkg.Distribution              downloadJDKSelectedDistribution;
    private              io.foojay.api.discoclient.pkg.OperatingSystem           downloadJDKSelectedOperatingSystem;
    private              LibCType                                                downloadJDKSelectedLibcType;
    private              Architecture                                            downloadJDKSelectedArchitecture;
    private              ArchiveType                                             downloadJDKSelectedArchiveType;
    private              Set<io.foojay.api.discoclient.pkg.OperatingSystem>      downloadJDKOperatingSystems;
    private              Set<Architecture>                                       downloadJDKArchitectures;
    private              Set<LibCType>                                           downloadJDKLibcTypes;
    private              Set<ArchiveType>                                        downloadJDKArchiveTypes;
    private              ProgressBar                                             downloadJDKProgressBar;
    private              Button                                                  downloadJDKCancelButton;
    private              Button                                                  downloadJDKDownloadButton;


    @Override public void init() {
        isUpdateAvailable = false;
        latestVersion     = VERSION;
        popups            = new HashMap<>();

        checkForLatestVersion();

        switch (operatingSystem) {
            case WINDOWS -> cssFile = "jdk-mon-win.css";
            case LINUX   -> cssFile = "jdk-mon-linux.css";
            default      -> cssFile = "jdk-mon.css";
        }

        notifier = NotifierBuilder.create()
                                  .popupLocation(OperatingSystem.MACOS == Detector.getOperatingSystem() ? Pos.TOP_RIGHT : Pos.BOTTOM_RIGHT)
                                  .popupLifeTime(Duration.millis(5000))
                                  .build();

        pane        = new StackPane();
        downloadJDKPane = new StackPane();

        darkMode = new BooleanPropertyBase(false) {
            @Override protected void invalidated() {
                pane.pseudoClassStateChanged(DARK_MODE_PSEUDO_CLASS, get());
                downloadJDKPane.pseudoClassStateChanged(DARK_MODE_PSEUDO_CLASS, get());
            }
            @Override public Object getBean() { return Main.this; }
            @Override public String getName() { return "darkMode"; }
        };
        darkMode.set(Detector.isDarkMode());

        if (io.foojay.api.discoclient.pkg.OperatingSystem.LINUX == operatingSystem) {
            if (PropertyManager.INSTANCE.hasKey(PropertyManager.DARK_MODE)) {
                darkMode.set(PropertyManager.INSTANCE.getBoolean(PropertyManager.DARK_MODE));
            } else {
                PropertyManager.INSTANCE.set(PropertyManager.DARK_MODE, "FALSE");
                PropertyManager.INSTANCE.storeProperties();
            }
        }

        closeMacWindowButton = new MacOSWindowButton(WindowButtonType.CLOSE, WindowButtonSize.SMALL);
        closeMacWindowButton.setDarkMode(darkMode.get());

        closeWinWindowButton = new WinWindowButton(WindowButtonType.CLOSE, WindowButtonSize.SMALL);
        closeWinWindowButton.setDarkMode(darkMode.get());

        windowTitle = new Label("JDK Mon");
        if (isWindows) {
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
        if (isWindows) {
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
        executor.scheduleAtFixedRate(() -> rescan(), Constants.INITIAL_DELAY_IN_HOURS, Constants.RESCAN_INTERVAL_IN_HOURS, TimeUnit.HOURS);

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
        titleLabel.setFont(isWindows ? Fonts.segoeUi(12) : Fonts.sfProTextBold(12));

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        HBox titleProgressBox;
        if (isWindows) {
            winProgressIndicator = new WinProgress();
            winProgressIndicator.setDarkMode(darkMode.get());
            winProgressIndicator.setVisible(false);
            titleProgressBox = new HBox(titleLabel, titleSpacer, winProgressIndicator);
        } else {
            macProgressIndicator = new MacProgress();
            macProgressIndicator.setDarkMode(darkMode.get());
            macProgressIndicator.setVisible(false);
            titleProgressBox = new HBox(titleLabel, titleSpacer, macProgressIndicator);
        }

        searchPathLabel = new Label(searchPaths.stream().collect(Collectors.joining(",")));
        searchPathLabel.getStyleClass().add("small-label");

        titleBox = new VBox(5, titleProgressBox, searchPathLabel);

        List<HBox> distroEntries = new ArrayList<>();

        try {
            finder.getAvailableUpdates(distros).entrySet().forEach(entry -> distroEntries.add(getDistroEntry(entry.getKey(), entry.getValue())));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
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
            if (isWindows) {
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
            if (isWindows) {
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

        timeline = new Timeline();


        downloadJDKMaintainedVersions          = new LinkedHashSet<>();
        downloadJDKSelectedPkgs                = new LinkedList<>();
        downloadJDKSelectedPkg                 = null;
        downloadJDKSelectedPkgsForMajorVersion = new LinkedList<>();
        downloadJDKJavafxBundled               = false;
        downloadJDKOperatingSystems            = new TreeSet<>();
        downloadJDKLibcTypes                   = new TreeSet<>();
        downloadJDKArchitectures               = new TreeSet<>();
        downloadJDKArchiveTypes                = new TreeSet<>();
        downloadJDKDistributionsThatSupportFx  = List.of(DiscoClient.getDistributionFromText("zulu"), DiscoClient.getDistributionFromText("liberica"), DiscoClient.getDistributionFromText("corretto"));
    }

    private void registerListeners() {
        headerPane.setOnMousePressed(press -> headerPane.setOnMouseDragged(drag -> {
            stage.setX(drag.getScreenX() - press.getSceneX());
            stage.setY(drag.getScreenY() - press.getSceneY());
        }));

        if (isWindows) {
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

        aboutDialog.getDialogPane().setOnMousePressed(e -> ((Stage) aboutDialog.getDialogPane().getScene().getWindow()).close());
        aboutDialog.setOnShowing(e -> {
            Stage    aboutStage = ((Stage) aboutDialog.getDialogPane().getScene().getWindow());
            KeyFrame key        = new KeyFrame(Duration.millis(7000), new KeyValue(aboutDialog.getDialogPane().opacityProperty(), 1));
            timeline.getKeyFrames().setAll(key);
            timeline.setOnFinished((ae) -> {
                aboutDialog.hide();
                aboutDialog.close();
                aboutStage.close();
            });
            timeline.play();
        });


        if (isWindows) {
            downloadJDKCloseWinWindowButton.setOnMouseReleased((Consumer<MouseEvent>) e -> {
                if (downloadJDKDialog.isShowing()) {
                    downloadJDKDialog.setResult(Boolean.TRUE);
                    downloadJDKDialog.close();
                }
            });
            downloadJDKCloseWinWindowButton.setOnMouseEntered(e -> downloadJDKCloseWinWindowButton.setHovered(true));
            downloadJDKCloseWinWindowButton.setOnMouseExited(e -> downloadJDKCloseWinWindowButton.setHovered(false));
        } else {
            downloadJDKCloseMacWindowButton.setOnMouseReleased((Consumer<MouseEvent>) e -> {
                if (downloadJDKDialog.isShowing()) {
                    downloadJDKDialog.setResult(Boolean.TRUE);
                    downloadJDKDialog.close();
                }
            });
            downloadJDKCloseMacWindowButton.setOnMouseEntered(e -> downloadJDKCloseMacWindowButton.setHovered(true));
            downloadJDKCloseMacWindowButton.setOnMouseExited(e -> downloadJDKCloseMacWindowButton.setHovered(false));
        }

        downloadJDKBundledWithFXCheckBox.selectedProperty().addListener((o, ov, nv) -> {
            downloadJDKJavafxBundled = nv;

            boolean include_build = downloadJDKSelectedMajorVersion.isEarlyAccessOnly();
            List<io.foojay.api.discoclient.pkg.Distribution> distrosForSelection = downloadJDKSelectedPkgsForMajorVersion.stream()
                                                                                                                     .filter(pkg -> downloadJDKJavafxBundled == pkg.isJavaFXBundled())
                                                                                                                     .filter(pkg -> pkg.getJavaVersion().getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED,true, include_build).equals(
                                                                                                                         downloadJDKSelectedVersionNumber.getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build)))
                                                                                                                     .map(pkg -> pkg.getDistribution())
                                                                                                                     .distinct()
                                                                                                                     .sorted(Comparator.comparing(io.foojay.api.discoclient.pkg.Distribution::getName).reversed())
                                                                                                                     .collect(Collectors.toList());
            Platform.runLater(() -> {
                downloadJDKDistributionComboBox.getItems().setAll(distrosForSelection);
                if (downloadJDKDistributionComboBox.getItems().size() > 0) {
                    downloadJDKDistributionComboBox.getSelectionModel().select(0);
                } else {
                    downloadJDKOperatingSystemComboBox.getItems().clear();
                    downloadJDKLibcTypeComboBox.getItems().clear();
                    downloadJDKArchitectureComboBox.getItems().clear();
                    downloadJDKArchiveTypeComboBox.getItems().clear();
                    downloadJDKFilenameLabel.setText("-");
                }
            });
        });

        downloadJDKMajorVersionComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (null == nv) { return; }
            selectMajorVersion();
        });

        downloadJDKUpdateLevelComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (null == downloadJDKMajorVersionComboBox.getSelectionModel().getSelectedItem()) { return; }
            if (null == nv) { return; }
            selectVersionNumber();
        });

        downloadJDKDistributionComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (null == downloadJDKMajorVersionComboBox.getSelectionModel().getSelectedItem()) { return; }
            if (null == downloadJDKUpdateLevelComboBox.getSelectionModel().getSelectedItem())  { return; }
            if (null == downloadJDKDistributionComboBox.getSelectionModel().getSelectedItem()) { return; }
            selectDistribution();
        });

        downloadJDKOperatingSystemComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (null == downloadJDKMajorVersionComboBox.getSelectionModel().getSelectedItem())    { return; }
            if (null == downloadJDKUpdateLevelComboBox.getSelectionModel().getSelectedItem())     { return; }
            if (null == downloadJDKDistributionComboBox.getSelectionModel().getSelectedItem())    { return; }
            if (null == downloadJDKOperatingSystemComboBox.getSelectionModel().getSelectedItem()) { return; }
            if (downloadJDKSelectedPkgs.isEmpty()) { return; }
            selectOperatingSystem();
        });

        downloadJDKLibcTypeComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (null == downloadJDKMajorVersionComboBox.getSelectionModel().getSelectedItem())    { return; }
            if (null == downloadJDKUpdateLevelComboBox.getSelectionModel().getSelectedItem())     { return; }
            if (null == downloadJDKDistributionComboBox.getSelectionModel().getSelectedItem())    { return; }
            if (null == downloadJDKOperatingSystemComboBox.getSelectionModel().getSelectedItem()) { return; }
            if (null == downloadJDKLibcTypeComboBox.getSelectionModel().getSelectedItem())        { return; }
            if (downloadJDKSelectedPkgs.isEmpty()) { return; }
            selectLibcType();
        });

        downloadJDKArchitectureComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (null == downloadJDKMajorVersionComboBox.getSelectionModel().getSelectedItem())    { return; }
            if (null == downloadJDKUpdateLevelComboBox.getSelectionModel().getSelectedItem())     { return; }
            if (null == downloadJDKDistributionComboBox.getSelectionModel().getSelectedItem())    { return; }
            if (null == downloadJDKOperatingSystemComboBox.getSelectionModel().getSelectedItem()) { return; }
            if (null == downloadJDKLibcTypeComboBox.getSelectionModel().getSelectedItem())        { return; }
            if (null == downloadJDKArchitectureComboBox.getSelectionModel().getSelectedItem())    { return; }
            if (downloadJDKSelectedPkgs.isEmpty()) { return; }
            selectArchitecture();
        });

        downloadJDKArchiveTypeComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (null == nv) { return; }
            selectArchiveType();
        });

        downloadJDKCancelButton.setOnAction(e -> {
            downloadJDKDialog.setResult(Boolean.TRUE);
            downloadJDKDialog.close();
        });

        downloadJDKDownloadButton.setOnAction(e -> {
            if (null == downloadJDKSelectedPkg) { return; }
            downloadPkgDownloadJDK(downloadJDKSelectedPkg);
        });

        downloadJDKStage.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                if (darkMode.get()) {
                    if (isWindows) {
                        downloadJDKHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        downloadJDKWindowTitle.setTextFill(Color.web("#969696"));
                    } else {
                        downloadJDKHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        downloadJDKWindowTitle.setTextFill(Color.web("#dddddd"));
                    }
                } else {
                    if (isWindows) {
                        downloadJDKHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        downloadJDKWindowTitle.setTextFill(Color.web("#000000"));
                    } else {
                        downloadJDKHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#edefef"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        downloadJDKWindowTitle.setTextFill(Color.web("#000000"));
                    }
                }
                downloadJDKCloseMacWindowButton.setDisable(false);
                downloadJDKCloseWinWindowButton.setDisable(false);
            } else {
                if (darkMode.get()) {
                    if (isWindows) {
                        downloadJDKHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        downloadJDKWindowTitle.setTextFill(Color.web("#969696"));
                    } else {
                        downloadJDKHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#282927"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        downloadJDKWindowTitle.setTextFill(Color.web("#696a68"));
                    }
                } else {
                    if (isWindows) {
                        downloadJDKCloseWinWindowButton.setStyle("-fx-fill: #969696;");
                    } else {
                        downloadJDKHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#e5e7e7"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        downloadJDKWindowTitle.setTextFill(Color.web("#a9a6a6"));
                        downloadJDKCloseMacWindowButton.setStyle("-fx-fill: #ceccca;");
                    }
                }
                downloadJDKCloseMacWindowButton.setDisable(true);
                downloadJDKCloseWinWindowButton.setDisable(true);
            }
        });
    }

    private void initOnFXApplicationThread() {
        aboutDialog       = createAboutDialog();
        downloadJDKDialog = createDownloadJDKDialog();

        registerListeners();

        discoclient.getMaintainedMajorVersionsAsync(true, true).thenAccept(uv -> {
            downloadJDKMaintainedVersions.addAll(uv);
            downloadJDKMajorVersionComboBox.getItems().setAll(downloadJDKMaintainedVersions);
            if (downloadJDKMaintainedVersions.size() > 0) {
                downloadJDKMajorVersionComboBox.getSelectionModel().select(0);
            }
        });
    }


    @Override public void start(final Stage stage) {
        initOnFXApplicationThread();

        this.stage = stage;
        this.stage.setMinWidth(402);
        this.stage.setMinHeight(272);
        this.trayIconSupported = FXTrayIcon.isSupported();

        if (trayIconSupported) {
            FXTrayIcon trayIcon = new FXTrayIcon(stage, getClass().getResource("duke.png"));
            trayIcon.setTrayIconTooltip("JDKMon " + VERSION);
            trayIcon.addExitItem(false);
            trayIcon.setApplicationTitle("JDKMon " + VERSION);

            MenuItem aboutItem = new MenuItem("About");
            aboutItem.setOnAction(e -> {
                if (aboutDialog.isShowing()) { return; }
                aboutDialog.show();
            });
            trayIcon.addMenuItem(aboutItem);

            MenuItem rescanItem = new MenuItem("Rescan");
            rescanItem.setOnAction(e -> rescan());
            trayIcon.addMenuItem(rescanItem);

            trayIcon.addSeparator();

            MenuItem searchPathItem = new MenuItem("Add search path");
            searchPathItem.setOnAction(e -> selectSearchPath());
            trayIcon.addMenuItem(searchPathItem);

            MenuItem defaultSearchPathItem = new MenuItem("Default search path");
            defaultSearchPathItem.setOnAction(e -> resetToDefaultSearchPath());
            trayIcon.addMenuItem(defaultSearchPathItem);

            trayIcon.addSeparator();

            CheckMenuItem rememberDownloadFolderItem = new CheckMenuItem();
            rememberDownloadFolderItem.setVisible(true);
            rememberDownloadFolderItem.setSelected(PropertyManager.INSTANCE.getBoolean(PropertyManager.REMEMBER_DOWNLOAD_FOLDER));
            rememberDownloadFolderItem.setText(rememberDownloadFolderItem.isSelected() ? "Remember download folder" : "Don't remember download folder");
            rememberDownloadFolderItem.setOnAction(e -> rememberDownloadFolderItem.setSelected(!rememberDownloadFolderItem.isSelected()));
            rememberDownloadFolderItem.selectedProperty().addListener(o -> {
                PropertyManager.INSTANCE.set(PropertyManager.REMEMBER_DOWNLOAD_FOLDER, rememberDownloadFolderItem.isSelected() ? "TRUE" : "FALSE");
                PropertyManager.INSTANCE.storeProperties();
                trayIcon.removeMenuItem(6);
                rememberDownloadFolderItem.setText(rememberDownloadFolderItem.isSelected() ? "Remember download folder" : "Don't remember download folder");
                trayIcon.insertMenuItem(rememberDownloadFolderItem, 6);
            });
            trayIcon.addMenuItem(rememberDownloadFolderItem);

            trayIcon.addSeparator();

            MenuItem downloadJDKItem = new MenuItem("Download a JDK");
            downloadJDKItem.setOnAction(e -> {
               if (downloadJDKDialog.isShowing()) { return; }
               downloadJDKDialog.show();
            });
            trayIcon.addMenuItem(downloadJDKItem);

            trayIcon.addSeparator();

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.setOnAction(e -> stop());
            trayIcon.addMenuItem(exitItem);

            trayIcon.show();
        } else {
            MenuBar menuBar = new MenuBar();
            menuBar.setPrefWidth(100);
            menuBar.setUseSystemMenuBar(true);
            //menuBar.useSystemMenuBarProperty().set(true);
            menuBar.setTranslateX(16);

            Menu menu = new Menu("JDK Mon");
            menu.setText("Menu");
            menu.setOnShowing(e -> hideMenu = false);
            menu.setOnHidden(e -> {
                if (!hideMenu) {
                    menu.show();
                }
            });

            CustomMenuItem aboutItem = new CustomMenuItem();
            Label mainLabel = new Label("About");
            mainLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            mainLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            aboutItem.setContent(mainLabel);
            aboutItem.setHideOnClick(false);
            aboutItem.setOnAction(e -> {
                if (aboutDialog.isShowing()) { return; }
                aboutDialog.show();
            });
            menu.getItems().add(aboutItem);

            CustomMenuItem rescanItem = new CustomMenuItem();
            Label rescanLabel = new Label("Rescan");
            rescanLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            rescanLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            rescanItem.setContent(rescanLabel);
            rescanItem.setHideOnClick(false);
            rescanItem.setOnAction(e -> rescan());
            menu.getItems().add(rescanItem);

            menu.getItems().add(new SeparatorMenuItem());

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

            menu.getItems().add(new SeparatorMenuItem());

            CheckMenuItem rememberDownloadFolderItem = new CheckMenuItem();
            rememberDownloadFolderItem.setVisible(true);
            rememberDownloadFolderItem.setSelected(PropertyManager.INSTANCE.getBoolean(PropertyManager.REMEMBER_DOWNLOAD_FOLDER));
            rememberDownloadFolderItem.setText("Remember download folder");
            rememberDownloadFolderItem.selectedProperty().addListener(o -> {
                PropertyManager.INSTANCE.set(PropertyManager.REMEMBER_DOWNLOAD_FOLDER, rememberDownloadFolderItem.isSelected() ? "TRUE" : "FALSE");
                PropertyManager.INSTANCE.storeProperties();
            });
            menu.getItems().add(rememberDownloadFolderItem);

            menu.getItems().add(new SeparatorMenuItem());

            CustomMenuItem downloadJDKItem = new CustomMenuItem();
            Label downloadJDKItemLabel = new Label("Download a JDK");
            downloadJDKItemLabel.setTooltip(new Tooltip("Download a JDK"));
            downloadJDKItemLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            downloadJDKItemLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            downloadJDKItem.setContent(downloadJDKItemLabel);
            downloadJDKItem.setHideOnClick(false);
            downloadJDKItem.setOnAction( e -> {
                if (downloadJDKDialog.isShowing()) { return; }
                downloadJDKDialog.show();
            });
            menu.getItems().add(downloadJDKItem);

            menu.getItems().add(new SeparatorMenuItem());

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

        Scene scene;
        if (io.foojay.api.discoclient.pkg.OperatingSystem.LINUX == operatingSystem && (Architecture.AARCH64 == architecture || Architecture.ARM64 == architecture)) {
            scene = new Scene(mainPane);
        } else {
            StackPane glassPane = new StackPane(mainPane);
            glassPane.setPadding(new Insets(10));
            glassPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            glassPane.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.75), 10.0, 0.0, 0.0, 5));
            scene = new Scene(glassPane);
        }

        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(Main.class.getResource(cssFile).toExternalForm());

        stage.setTitle("JDK Mon");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.show();
        stage.getIcons().add(dukeStageIcon);
        stage.centerOnScreen();
        stage.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                if (darkMode.get()) {
                    if (isWindows) {
                        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        windowTitle.setTextFill(Color.web("#969696"));
                    } else {
                        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        windowTitle.setTextFill(Color.web("#dddddd"));
                    }
                } else {
                    if (isWindows) {
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
                    if (isWindows) {
                        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        windowTitle.setTextFill(Color.web("#969696"));
                    } else {
                        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#282927"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        windowTitle.setTextFill(Color.web("#696a68"));
                    }
                } else {
                    if (isWindows) {
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
            if (isWindows) {
                winProgressIndicator.setVisible(true);
                winProgressIndicator.setIndeterminate(true);
            } else {
                macProgressIndicator.setVisible(true);
                macProgressIndicator.setIndeterminate(true);
            }
            Set<Distribution> distrosFound = finder.getDistributions(searchPaths);
            distros.setAll(distrosFound);
            SwingUtilities.invokeLater(() -> checkForUpdates());
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
            if (isWindows) {
                winProgressIndicator.setVisible(false);
                winProgressIndicator.setIndeterminate(false);
            } else {
                macProgressIndicator.setVisible(false);
                macProgressIndicator.setIndeterminate(false);
            }
            if (updatesAvailable.get()) {
                Notification notification = NotificationBuilder.create().title("New updates available").message(msgBuilder.toString()).image(dukeNotificationIcon).build();
                notifier.notify(notification);
            }
        });

        checkingForUpdates.set(false);
    }

    private HBox getDistroEntry(final Distribution distribution, final List<Pkg> pkgs) {
        final boolean isDistributionInUse = distribution.isInUse();

        Label distroLabel = new Label(new StringBuilder(distribution.getName()).append(distribution.getFxBundled() ? " (FX)" : "").append("  ").append(distribution.getVersion()).append(isDistributionInUse ? "*" : "").toString());
        distroLabel.setMinWidth(180);
        distroLabel.setAlignment(Pos.CENTER_LEFT);
        distroLabel.setMaxWidth(Double.MAX_VALUE);

        distroLabel.setTooltip(new Tooltip(isDistributionInUse ? "(Currently in use) " + distribution.getLocation() : distribution.getLocation()));
        distroLabel.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown())
                openDistribution(distribution);
        });

        HBox hBox = new HBox(5, distroLabel);
        hBox.setMinWidth(360);

        if (pkgs.isEmpty()) { return hBox; }

        Optional<Pkg> optionalZulu = pkgs.parallelStream()
                                         .sorted(Comparator.comparing(Pkg::getDistributionName).reversed())
                                         .filter(pkg -> pkg.getDistribution().getApiString().equals("zulu"))
                                         .findFirst();

        Optional<Pkg> optionalOpenJDK = pkgs.parallelStream()
                                            .sorted(Comparator.comparing(Pkg::getDistributionName).reversed())
                                            .filter(pkg -> pkg.getDistribution().getApiString().equals("oracle_open_jdk"))
                                            .findFirst();

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

        if (isWindows) {
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
        popupTitle.setFont(isWindows ? Fonts.segoeUiSemiBold(12) : Fonts.sfProTextMedium(12));
        popupTitle.setTextFill(darkMode.get() ? Color.web("#dddddd") : Color.web("#000000"));
        popupTitle.setMouseTransparent(true);
        popupTitle.setAlignment(Pos.CENTER);

        AnchorPane.setTopAnchor(closePopupMacOSButton, 5d);
        AnchorPane.setLeftAnchor(closePopupMacOSButton, 5d);
        AnchorPane.setTopAnchor(closePopupWinButton, 0d);
        AnchorPane.setRightAnchor(closePopupWinButton, 0d);
        AnchorPane.setTopAnchor(popupTitle, 0d);
        AnchorPane.setRightAnchor(popupTitle, 0d);
        AnchorPane.setBottomAnchor(popupTitle, 0d);
        AnchorPane.setLeftAnchor(popupTitle, 0d);

        AnchorPane popupHeader = new AnchorPane();
        popupHeader.getStyleClass().add("header");
        if (isWindows) {
            popupHeader.setMinHeight(31);
            popupHeader.setMaxHeight(31);
            popupHeader.setPrefHeight(31);
        } else {
            popupHeader.setMinHeight(21);
            popupHeader.setMaxHeight(21);
            popupHeader.setPrefHeight(21);
        }
        popupHeader.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.1), 1, 0.0, 0, 1));
        if (isWindows) {
            popupHeader.getChildren().addAll(closePopupWinButton, popupTitle);
        } else {
            popupHeader.getChildren().addAll(closePopupMacOSButton, popupTitle);
        }

        Label popupMsg;
        if (optionalZulu.isPresent()) {
            popupMsg = new Label(optionalZulu.get().getDistribution().getUiString() + " " + optionalZulu.get().getJavaVersion().toString(true) + " available");
        } else if (optionalOpenJDK.isPresent()) {
            popupMsg = new Label(optionalOpenJDK.get().getDistribution().getUiString() + " " + optionalOpenJDK.get().getJavaVersion().toString(true) + " available");
        } else {
            popupMsg = new Label(firstPkg.getDistribution().getUiString() + " " + firstPkg.getJavaVersion().toString(true) + " available");
        }
        popupMsg.setTextFill(darkMode.get() ? Color.web("#dddddd") : Color.web("#292929"));
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
            if (isWindows) {
                popupHeader.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(0, 0, 0, 0, false), Insets.EMPTY)));
                popupContent.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(0, 0, 0, 0, false), Insets.EMPTY)));
                popupPane.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(0), Insets.EMPTY)));
                popupPane.setBorder(new Border(new BorderStroke(Color.web("#515352"), BorderStrokeStyle.SOLID, new CornerRadii(0, 0, 0, 0, false), new BorderWidths(1))));
            } else {
                popupHeader.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                popupContent.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(0, 0, 10, 10, false), Insets.EMPTY)));
                popupPane.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(10), Insets.EMPTY)));
                popupPane.setBorder(new Border(new BorderStroke(Color.web("#515352"), BorderStrokeStyle.SOLID, new CornerRadii(10, 10, 10, 10, false), new BorderWidths(1))));
            }
        } else {
            if (isWindows) {
                popupHeader.setBackground(new Background(new BackgroundFill(Color.web("#efedec"), new CornerRadii(0, 0, 0, 0, false), Insets.EMPTY)));
                popupContent.setBackground(new Background(new BackgroundFill(Color.web("#e3e5e5"), new CornerRadii(0, 0, 0, 0, false), Insets.EMPTY)));
                popupPane.setBackground(new Background(new BackgroundFill(Color.web("#ecebe9"), new CornerRadii(0), Insets.EMPTY)));
                popupPane.setBorder(new Border(new BorderStroke(Color.web("#f6f4f4"), BorderStrokeStyle.SOLID, new CornerRadii(0, 0, 0, 0, false), new BorderWidths(1))));
            } else {
                popupHeader.setBackground(new Background(new BackgroundFill(Color.web("#efedec"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                popupContent.setBackground(new Background(new BackgroundFill(Color.web("#e3e5e5"), new CornerRadii(0, 0, 10, 10, false), Insets.EMPTY)));
                popupPane.setBackground(new Background(new BackgroundFill(Color.web("#ecebe9"), new CornerRadii(10), Insets.EMPTY)));
                popupPane.setBorder(new Border(new BorderStroke(Color.web("#f6f4f4"), BorderStrokeStyle.SOLID, new CornerRadii(10, 10, 10, 10, false), new BorderWidths(1))));
            }
        }

        popup.getContent().add(popupPane);
        popups.put(distroLabel.getText(), popup);
        // ********************************************************************
        final String distributionApiString = distribution.getApiString();

        if (distributionApiString.equals(nameToCheck)) {
            Label versionLabel = new Label(firstPkg.getJavaVersion().toString(true));
            versionLabel.setMinWidth(56);
            hBox.getChildren().add(versionLabel);
        } else {
            // Only show newer update for installed version from another distribution if not AOJ_OpenJ9, Semeru, Semeru Certified and Zulu Prime
            if (!distributionApiString.equals("zulu_prime") && !distributionApiString.equals("aoj_openj9") && !distributionApiString.equals("semeru") && !distributionApiString.equals("semeru_certified")) {
                // There is a newer update for the installed version from another distribution
                Region infoIcon = new Region();
                infoIcon.getStyleClass().add("icon");
                infoIcon.setId("info");
                infoIcon.setOnMousePressed(e -> {
                    popups.values().forEach(p -> p.hide());
                    if (null != popup) {
                        popup.setX(e.getScreenX() + 10);
                        popup.setY(e.getScreenY() + 10);
                        popup.show(stage);
                    }
                });
                hBox.getChildren().add(infoIcon);
            } else {
                hBox.getChildren().remove(arrowLabel);
            }
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
                    archiveTypeLabel.setTooltip(new Tooltip("Go to download page"));
                    archiveTypeLabel.setTextFill(Color.WHITE);
                    archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacOSAccentColor.GRAPHITE.getColorDark() : MacOSAccentColor.GRAPHITE.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
                }
                archiveTypeLabel.disableProperty().bind(blocked);
                if (pkg.isDirectlyDownloadable()) {
                    archiveTypeLabel.setOnMouseClicked(e -> { if (!blocked.get()) { downloadPkg(pkg); }});
                } else {
                    archiveTypeLabel.setOnMouseClicked(e -> { if (!blocked.get()) {
                        if (Desktop.isDesktopSupported()) {
                            final String downloadSiteUri = discoclient.getPkgDownloadSiteUri(pkg.getId());
                            try {
                                Desktop.getDesktop().browse(new URI(downloadSiteUri));
                            } catch (IOException | URISyntaxException ex) {
                                ex.printStackTrace();
                            }
                        }
                    } });
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

    private void openDistribution(Distribution distribution) {
        try {
            Desktop.getDesktop().open(new File(distribution.getLocation()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Dialog createAboutDialog() {
        final boolean isDarkMode = darkMode.get();

        Dialog aboutDialog = new Dialog();
        aboutDialog.setTitle("JDKMon");
        aboutDialog.initStyle(StageStyle.TRANSPARENT);
        aboutDialog.initModality(Modality.WINDOW_MODAL);

        Stage aboutDialogStage = (Stage) aboutDialog.getDialogPane().getScene().getWindow();
        aboutDialogStage.getIcons().add(dukeStageIcon);
        aboutDialogStage.getScene().setFill(Color.TRANSPARENT);
        aboutDialogStage.getScene().getStylesheets().add(Main.class.getResource(cssFile).toExternalForm());

        ImageView aboutImage = new ImageView(dukeStageIcon);
        aboutImage.setFitWidth(128);
        aboutImage.setFitHeight(128);

        Label nameLabel = new Label("JDKMon");
        nameLabel.setFont(isWindows ? Fonts.segoeUi(36) : Fonts.sfPro(36));

        Label versionLabel = new Label(VERSION.toString(OutputFormat.REDUCED_COMPRESSED, true, false));
        versionLabel.setFont(isWindows ? Fonts.segoeUi(14) : Fonts.sfPro(14));

        Label infrastructureLabel = new Label("(" + operatingSystem.getUiString() + ", " + architecture.getUiString() + ")");
        infrastructureLabel.setFont(isWindows ? Fonts.segoeUi(12) : Fonts.sfPro(12));

        Node updateNode;
        if (isUpdateAvailable) {
            Hyperlink updateLink = new Hyperlink();
            updateLink.setFont(isWindows ? Fonts.segoeUi(12) : Fonts.sfPro(12));
            updateLink.setText("New Version (" + latestVersion.toString(OutputFormat.REDUCED_COMPRESSED, true, false) + ") available");
            updateLink.setOnAction(e -> {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(Constants.RELEASES_URI));
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            if (Detector.OperatingSystem.MACOS == Detector.getOperatingSystem()) {
                if (isDarkMode) {
                    updateLink.setTextFill(accentColor.getColorDark());
                } else {
                    updateLink.setTextFill(accentColor.getColorAqua());
                }
            } else {
                updateLink.setTextFill(accentColor.getColorAqua());
            }

            updateNode = updateLink;
        } else {
            Label updateLabel = new Label("Latest version installed");
            updateLabel.setFont(isWindows ? Fonts.segoeUi(12) : Fonts.sfPro(12));

            if (isDarkMode) {
                updateLabel.setTextFill(Color.web("#dddddd"));
            } else {
                updateLabel.setTextFill(Color.web("#2a2a2a"));
            }

            updateNode = updateLabel;
        }

        Label descriptionLabel = new Label("JDKMon, your friendly JDK updater helps you keeping track of your installed OpenJDK distributions.");
        if (isWindows) {
            descriptionLabel.setFont(Fonts.segoeUi(11));
        } else if (io.foojay.api.discoclient.pkg.OperatingSystem.MACOS == operatingSystem) {
            descriptionLabel.setFont(Fonts.sfPro(11));
        } else if (io.foojay.api.discoclient.pkg.OperatingSystem.LINUX == operatingSystem) {
            descriptionLabel.setFont(Fonts.sfPro(11));
        }
        descriptionLabel.setTextAlignment(TextAlignment.LEFT);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setAlignment(Pos.TOP_LEFT);

        VBox aboutTextBox = new VBox(10, nameLabel, versionLabel, infrastructureLabel, updateNode, descriptionLabel);

        HBox aboutBox = new HBox(20, aboutImage, aboutTextBox);
        aboutBox.setAlignment(Pos.CENTER);
        aboutBox.setPadding(new Insets(20, 20, 10, 20));
        aboutBox.setMinSize(420, 200);
        aboutBox.setMaxSize(420, 200);
        aboutBox.setPrefSize(420, 200);


        if (io.foojay.api.discoclient.pkg.OperatingSystem.LINUX == operatingSystem && (Architecture.AARCH64 == architecture || Architecture.ARM64 == architecture)) {
            aboutDialog.getDialogPane().setContent(new StackPane(aboutBox));
        } else {
            StackPane glassPane = new StackPane(aboutBox);
            glassPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            glassPane.setMinSize(440, 220);
            glassPane.setMaxSize(440, 220);
            glassPane.setPrefSize(440, 220);
            glassPane.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.35), 10.0, 0.0, 0.0, 5));
            aboutDialog.getDialogPane().setContent(glassPane);
        }

        aboutDialog.getDialogPane().setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        // Adjustments related to dark/light mode
        if (isDarkMode) {
            if (isWindows) {
                aboutBox.setBackground(new Background(new BackgroundFill(Color.web("#000000"), CornerRadii.EMPTY, Insets.EMPTY)));
                nameLabel.setTextFill(Color.web("#dddddd"));
                versionLabel.setTextFill(Color.web("#dddddd"));
                infrastructureLabel.setTextFill(Color.web("#dddddd"));
                descriptionLabel.setTextFill(Color.web("#dddddd"));
            } else {
                aboutBox.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 10, 10, false), Insets.EMPTY)));
                nameLabel.setTextFill(Color.web("#dddddd"));
                versionLabel.setTextFill(Color.web("#dddddd"));
                infrastructureLabel.setTextFill(Color.web("#dddddd"));
                descriptionLabel.setTextFill(Color.web("#dddddd"));
            }
        } else {
            if (isWindows) {
                aboutBox.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), CornerRadii.EMPTY, Insets.EMPTY)));
                nameLabel.setTextFill(Color.web("#2a2a2a"));
                versionLabel.setTextFill(Color.web("#2a2a2a"));
                infrastructureLabel.setTextFill(Color.web("#2a2a2a"));
                descriptionLabel.setTextFill(Color.web("#2a2a2a"));
            } else {
                aboutBox.setBackground(new Background(new BackgroundFill(Color.web("#efedec"), new CornerRadii(10, 10, 10, 10, false), Insets.EMPTY)));
                nameLabel.setTextFill(Color.web("#2a2a2a"));
                versionLabel.setTextFill(Color.web("#2a2a2a"));
                infrastructureLabel.setTextFill(Color.web("#2a2a2a"));
                descriptionLabel.setTextFill(Color.web("#2a2a2a"));
            }
        }

        return aboutDialog;
    }

    private void downloadPkg(final Pkg pkg) {
        if (null == pkg) { return; }
        directoryChooser.setTitle("Choose folder for download");

        final File downloadFolder;
        if (PropertyManager.INSTANCE.getBoolean(PropertyManager.REMEMBER_DOWNLOAD_FOLDER)) {
            if (PropertyManager.INSTANCE.getString(PropertyManager.DOWNLOAD_FOLDER).isEmpty()) {
                directoryChooser.setTitle("Choose folder for download");
                downloadFolder = directoryChooser.showDialog(stage);
            } else {
                File folder = new File(PropertyManager.INSTANCE.getString(PropertyManager.DOWNLOAD_FOLDER));
                if (folder.isDirectory()) {
                    downloadFolder = folder;
                } else {
                    downloadFolder = directoryChooser.showDialog(stage);
                }
            }
        } else {
            downloadFolder = directoryChooser.showDialog(stage);
        }
        PropertyManager.INSTANCE.set(PropertyManager.DOWNLOAD_FOLDER, downloadFolder.getAbsolutePath());
        PropertyManager.INSTANCE.storeProperties();

        if (null != downloadFolder) {
            final String directDownloadUri = discoclient.getPkgDirectDownloadUri(pkg.getId());
            if (null == directDownloadUri) {
                new Alert(AlertType.ERROR, "Problem downloading the package, please try again.", ButtonType.CLOSE).show();
                return;
            }
            final String target = downloadFolder.getAbsolutePath() + File.separator + pkg.getFileName();
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

            if (PropertyManager.INSTANCE.getBoolean(PropertyManager.REMEMBER_DOWNLOAD_FOLDER)) {
                new Thread((Runnable) worker).start();
            } else {
                Alert info = new Alert(AlertType.INFORMATION);
                info.setTitle("JDKMon");
                info.setHeaderText("JDKMon Download Info");
                info.setContentText("Download will be started and update will be saved to " + downloadFolder);
                info.setOnCloseRequest(e -> new Thread((Runnable) worker).start());
                info.show();
            }
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

    private void checkForLatestVersion() {
        Helper.checkForJDKMonUpdateAsync().thenAccept(response -> {
            if (null == response || null == response.body() || response.body().isEmpty()) {
                isUpdateAvailable = false;
            } else {
                final Gson       gson       = new Gson();
                final JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
                if (jsonObject.has("tag_name")) {
                    latestVersion     = VersionNumber.fromText(jsonObject.get("tag_name").getAsString());
                    isUpdateAvailable = latestVersion.compareTo(Main.VERSION) > 0;
                }
            }
        });
    }

    // Download a JDK related
    private Dialog createDownloadJDKDialog() {
        final boolean isDarkMode = darkMode.get();

        Dialog downloadJDKDialog = new Dialog();
        downloadJDKDialog.initStyle(StageStyle.TRANSPARENT);
        downloadJDKDialog.initModality(Modality.WINDOW_MODAL);

        downloadJDKStage = (Stage) downloadJDKDialog.getDialogPane().getScene().getWindow();
        downloadJDKStage.setAlwaysOnTop(true);
        downloadJDKStage.getIcons().add(dukeStageIcon);
        downloadJDKStage.getScene().setFill(Color.TRANSPARENT);
        downloadJDKStage.getScene().getStylesheets().add(Main.class.getResource(cssFile).toExternalForm());

        downloadJDKBundledWithFXCheckBox = new CheckBox("Bundled with FX");
        downloadJDKBundledWithFXCheckBox.setFont(isWindows ? Fonts.segoeUi(13) : Fonts.sfPro(13));
        downloadJDKBundledWithFXCheckBox.setTextFill(isWindows ? Color.web("#dddddd") : Color.web("#2a2a2a"));
        HBox findlJDKFxBox = new HBox(downloadJDKBundledWithFXCheckBox);


        Label downloadJDKMajorVersionLabel = new Label("Major version");
        Region findlJDKMajorVersionSpacer = new Region();
        HBox.setHgrow(findlJDKMajorVersionSpacer, Priority.ALWAYS);
        downloadJDKMajorVersionComboBox = new ComboBox<>();
        downloadJDKMajorVersionComboBox.setCellFactory(majorVersionListView -> new MajorVersionCell());
        downloadJDKMajorVersionComboBox.setMinWidth(150);
        downloadJDKMajorVersionComboBox.setMaxWidth(150);
        downloadJDKMajorVersionComboBox.setPrefWidth(150);
        HBox downloadJDKMajorVersionBox = new HBox(5, downloadJDKMajorVersionLabel, findlJDKMajorVersionSpacer, downloadJDKMajorVersionComboBox);
        downloadJDKMajorVersionBox.setAlignment(Pos.CENTER);

        Label downloadJDKUpdateLevelLabel = new Label("Update level");
        Region downloadJDKUpdateLevelSpacer = new Region();
        HBox.setHgrow(downloadJDKUpdateLevelSpacer, Priority.ALWAYS);
        downloadJDKUpdateLevelComboBox = new ComboBox<>();
        downloadJDKUpdateLevelComboBox.setCellFactory(updateLevelListView -> new UpdateLevelCell());
        downloadJDKUpdateLevelComboBox.setMinWidth(150);
        downloadJDKUpdateLevelComboBox.setMaxWidth(150);
        downloadJDKUpdateLevelComboBox.setPrefWidth(150);
        HBox downloadJDKUpdateLevelBox = new HBox(5, downloadJDKUpdateLevelLabel, downloadJDKUpdateLevelSpacer, downloadJDKUpdateLevelComboBox);
        downloadJDKUpdateLevelBox.setAlignment(Pos.CENTER);

        Label downloadJDKDistributionLabel = new Label("Distribution");
        Region downloadJDKDistributionSpacer = new Region();
        HBox.setHgrow(downloadJDKDistributionSpacer, Priority.ALWAYS);
        downloadJDKDistributionComboBox = new ComboBox<>();
        downloadJDKDistributionComboBox.setCellFactory(distributionListView -> new DistributionCell());
        downloadJDKDistributionComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(final io.foojay.api.discoclient.pkg.Distribution distribution) {
                return null == distribution ? null : distribution.getUiString();
            }
            @Override public io.foojay.api.discoclient.pkg.Distribution fromString(final String text) {
                return DiscoClient.getDistributionFromText(text);
            }
        });
        downloadJDKDistributionComboBox.setMinWidth(150);
        downloadJDKDistributionComboBox.setMaxWidth(150);
        downloadJDKDistributionComboBox.setPrefWidth(150);
        HBox downloadJDKDistributionBox = new HBox(5, downloadJDKDistributionLabel, downloadJDKDistributionSpacer, downloadJDKDistributionComboBox);
        downloadJDKDistributionBox.setAlignment(Pos.CENTER);

        Label downloadJDKOperatingSystemLabel = new Label("Operating system");
        Region findlJDKOperatingSystemSpacer = new Region();
        HBox.setHgrow(findlJDKOperatingSystemSpacer, Priority.ALWAYS);
        downloadJDKOperatingSystemComboBox = new ComboBox<>();
        downloadJDKOperatingSystemComboBox.setCellFactory(operatingSystemListView -> new OperatingSystemCell());
        downloadJDKOperatingSystemComboBox.setMinWidth(150);
        downloadJDKOperatingSystemComboBox.setMaxWidth(150);
        downloadJDKOperatingSystemComboBox.setPrefWidth(150);
        HBox downloadJDKOperatingSystemBox = new HBox(5, downloadJDKOperatingSystemLabel, findlJDKOperatingSystemSpacer, downloadJDKOperatingSystemComboBox);
        downloadJDKOperatingSystemBox.setAlignment(Pos.CENTER);

        Label downloadJDKLibcTypeLabel = new Label("Libc type");
        Region downloadJDKLibcTypeSpacer = new Region();
        HBox.setHgrow(downloadJDKLibcTypeSpacer, Priority.ALWAYS);
        downloadJDKLibcTypeComboBox = new ComboBox<>();
        downloadJDKLibcTypeComboBox.setCellFactory(libcTypeListView -> new LibcTypeCell());
        downloadJDKLibcTypeComboBox.setMinWidth(150);
        downloadJDKLibcTypeComboBox.setMaxWidth(150);
        downloadJDKLibcTypeComboBox.setPrefWidth(150);
        HBox downloadJDKLibcTypeBox = new HBox(5, downloadJDKLibcTypeLabel, downloadJDKLibcTypeSpacer, downloadJDKLibcTypeComboBox);
        downloadJDKLibcTypeBox.setAlignment(Pos.CENTER);

        Label downloadJDKArchitectureLabel = new Label("Architecture");
        Region downloadJDKArchitectureSpacer = new Region();
        HBox.setHgrow(downloadJDKArchitectureSpacer, Priority.ALWAYS);
        downloadJDKArchitectureComboBox = new ComboBox<>();
        downloadJDKArchitectureComboBox.setCellFactory(architectureListView -> new ArchitectureCell());
        downloadJDKArchitectureComboBox.setMinWidth(150);
        downloadJDKArchitectureComboBox.setMaxWidth(150);
        downloadJDKArchitectureComboBox.setPrefWidth(150);
        HBox downloadJDKArchitectureBox = new HBox(5, downloadJDKArchitectureLabel, downloadJDKArchitectureSpacer, downloadJDKArchitectureComboBox);
        downloadJDKArchitectureBox.setAlignment(Pos.CENTER);

        Label downloadJDKArchiveTypeLabel = new Label("Archive type");
        Region downloadJDKArchiveTypeSpacer = new Region();
        HBox.setHgrow(downloadJDKArchiveTypeSpacer, Priority.ALWAYS);
        downloadJDKArchiveTypeComboBox = new ComboBox<>();
        downloadJDKArchiveTypeComboBox.setCellFactory(archiveTypeListView -> new ArchiveTypeCell());
        downloadJDKArchiveTypeComboBox.setMinWidth(150);
        downloadJDKArchiveTypeComboBox.setMaxWidth(150);
        downloadJDKArchiveTypeComboBox.setPrefWidth(150);
        HBox downloadJDKArchiveTypeBox = new HBox(5, downloadJDKArchiveTypeLabel, downloadJDKArchiveTypeSpacer, downloadJDKArchiveTypeComboBox);
        downloadJDKArchiveTypeBox.setAlignment(Pos.CENTER);

        downloadJDKFilenameLabel = new Label("-");
        downloadJDKFilenameLabel.getStyleClass().add("small-label");
        HBox.setMargin(downloadJDKFilenameLabel, new Insets(10, 0, 10, 0));
        HBox.setHgrow(downloadJDKFilenameLabel, Priority.ALWAYS);
        HBox downloadJDKFilenameBox = new HBox(downloadJDKFilenameLabel);
        downloadJDKFilenameBox.setAlignment(Pos.CENTER);

        downloadJDKCancelButton = new Button("Cancel");

        Region downloadJDKButtonSpacer   = new Region();
        HBox.setMargin(downloadJDKButtonSpacer, new Insets(10, 0, 10, 0));
        HBox.setHgrow(downloadJDKButtonSpacer, Priority.ALWAYS);

        downloadJDKDownloadButton = new Button("Download");
        downloadJDKDownloadButton.setDisable(true);

        HBox downloadJDKButtonBox = new HBox(5, downloadJDKCancelButton, downloadJDKButtonSpacer, downloadJDKDownloadButton);


        downloadJDKCloseMacWindowButton = new MacOSWindowButton(WindowButtonType.CLOSE, WindowButtonSize.SMALL);
        downloadJDKCloseMacWindowButton.setDarkMode(isDarkMode);

        downloadJDKCloseWinWindowButton = new WinWindowButton(WindowButtonType.CLOSE, WindowButtonSize.SMALL);
        downloadJDKCloseWinWindowButton.setDarkMode(isDarkMode);

        downloadJDKWindowTitle = new Label("Download a JDK");
        if (isWindows) {
            downloadJDKWindowTitle.setFont(Fonts.segoeUi(9));
            downloadJDKWindowTitle.setTextFill(isDarkMode ? Color.web("#969696") : Color.web("#000000"));
            downloadJDKWindowTitle.setAlignment(Pos.CENTER_LEFT);
            downloadJDKWindowTitle.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(darkMode.get() ? "duke.png" : "duke_blk.png"), 12, 12, true, false)));
            downloadJDKWindowTitle.setGraphicTextGap(10);

            AnchorPane.setTopAnchor(downloadJDKCloseWinWindowButton, 0d);
            AnchorPane.setRightAnchor(downloadJDKCloseWinWindowButton, 0d);
            AnchorPane.setTopAnchor(downloadJDKWindowTitle, 0d);
            AnchorPane.setRightAnchor(downloadJDKWindowTitle, 0d);
            AnchorPane.setBottomAnchor(downloadJDKWindowTitle, 0d);
            AnchorPane.setLeftAnchor(downloadJDKWindowTitle, 10d);
        } else {
            downloadJDKWindowTitle.setFont(Fonts.sfProTextMedium(12));
            downloadJDKWindowTitle.setTextFill(isDarkMode ? Color.web("#dddddd") : Color.web("#000000"));
            downloadJDKWindowTitle.setAlignment(Pos.CENTER);

            AnchorPane.setTopAnchor(downloadJDKCloseMacWindowButton, 5d);
            AnchorPane.setLeftAnchor(downloadJDKCloseMacWindowButton, 5d);
            AnchorPane.setTopAnchor(downloadJDKWindowTitle, 0d);
            AnchorPane.setRightAnchor(downloadJDKWindowTitle, 0d);
            AnchorPane.setBottomAnchor(downloadJDKWindowTitle, 0d);
            AnchorPane.setLeftAnchor(downloadJDKWindowTitle, 0d);
        }
        downloadJDKWindowTitle.setMouseTransparent(true);

        downloadJDKHeaderPane = new AnchorPane();
        downloadJDKHeaderPane.getStyleClass().add("header");
        downloadJDKHeaderPane.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.1), 1, 0.0, 0, 1));
        if (isWindows) {
            downloadJDKHeaderPane.setMinHeight(31);
            downloadJDKHeaderPane.setMaxHeight(31);
            downloadJDKHeaderPane.setPrefHeight(31);
            downloadJDKHeaderPane.getChildren().addAll(downloadJDKCloseWinWindowButton, downloadJDKWindowTitle);
        } else {
            downloadJDKHeaderPane.setMinHeight(21);
            downloadJDKHeaderPane.setMaxHeight(21);
            downloadJDKHeaderPane.setPrefHeight(21);
            downloadJDKHeaderPane.getChildren().addAll(downloadJDKCloseMacWindowButton, downloadJDKWindowTitle);
        }

        downloadJDKProgressBar = new ProgressBar(0);
        downloadJDKProgressBar.setVisible(false);
        VBox.setMargin(downloadJDKProgressBar, new Insets(0, 0, 5, 0));

        VBox downloadJDKVBox = new VBox(10, findlJDKFxBox, downloadJDKMajorVersionBox, downloadJDKUpdateLevelBox, downloadJDKDistributionBox, downloadJDKOperatingSystemBox,
                                        downloadJDKLibcTypeBox, downloadJDKArchitectureBox, downloadJDKArchiveTypeBox, downloadJDKFilenameBox, downloadJDKProgressBar, downloadJDKButtonBox);
        downloadJDKVBox.setAlignment(Pos.CENTER);

        downloadJDKPane.getChildren().add(downloadJDKVBox);
        downloadJDKPane.getStyleClass().add("jdk-mon");
        downloadJDKPane.setPadding(new Insets(10));

        BorderPane downloadJDKMainPane = new BorderPane();
        downloadJDKMainPane.setTop(downloadJDKHeaderPane);
        downloadJDKMainPane.setCenter(downloadJDKPane);

        downloadJDKProgressBar.prefWidthProperty().bind(downloadJDKMainPane.widthProperty());

        if (io.foojay.api.discoclient.pkg.OperatingSystem.LINUX == operatingSystem && (Architecture.AARCH64 == architecture ||Architecture.ARM64 == architecture)) {
            downloadJDKMainPane.setOnMousePressed(press -> downloadJDKMainPane.setOnMouseDragged(drag -> {
                downloadJDKStage.setX(drag.getScreenX() - press.getSceneX());
                downloadJDKStage.setY(drag.getScreenY() - press.getSceneY());
            }));
            downloadJDKDialog.getDialogPane().setContent(new StackPane(downloadJDKMainPane));
        } else {
            StackPane downloadJDKGlassPane = new StackPane(downloadJDKMainPane);
            downloadJDKGlassPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            downloadJDKGlassPane.setMinSize(310, isWindows ? 480 : 390);
            downloadJDKGlassPane.setMaxSize(310, isWindows ? 480 : 390);
            downloadJDKGlassPane.setPrefSize(310, isWindows ? 480 : 390);
            downloadJDKGlassPane.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.35), 10.0, 0.0, 0.0, 5));
            downloadJDKGlassPane.setOnMousePressed(press -> downloadJDKGlassPane.setOnMouseDragged(drag -> {
                downloadJDKStage.setX(drag.getScreenX() - press.getSceneX());
                downloadJDKStage.setY(drag.getScreenY() - press.getSceneY());
            }));
            downloadJDKDialog.getDialogPane().setContent(downloadJDKGlassPane);
        }

        downloadJDKDialog.getDialogPane().setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        // Adjustments related to dark/light mode
        if (Detector.OperatingSystem.MACOS == Detector.getOperatingSystem()) {
            if (isDarkMode) {
                downloadJDKPane.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorDark()));
                contextMenu.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorDark()));
            } else {
                downloadJDKPane.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorAqua()));
                contextMenu.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorAqua()));
            }
        }
        if (isDarkMode) {
            if (isWindows) {
                downloadJDKWindowTitle.setTextFill(Color.web("#969696"));
                downloadJDKHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                downloadJDKHeaderPane.setBorder(new Border(new BorderStroke(Color.web("#f2f2f2"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0.5, 0))));
                downloadJDKPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), CornerRadii.EMPTY, Insets.EMPTY)));
                downloadJDKMainPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), CornerRadii.EMPTY, Insets.EMPTY)));
                downloadJDKMainPane.setBorder(new Border(new BorderStroke(Color.web("#333333"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 1, 1, 1))));
                downloadJDKBundledWithFXCheckBox.setTextFill(Color.web("#292929"));
                downloadJDKMajorVersionLabel.setTextFill(Color.web("#292929"));
                downloadJDKUpdateLevelLabel.setTextFill(Color.web("#292929"));
                downloadJDKDistributionLabel.setTextFill(Color.web("#292929"));
                downloadJDKOperatingSystemLabel.setTextFill(Color.web("#292929"));
                downloadJDKLibcTypeLabel.setTextFill(Color.web("#292929"));
                downloadJDKArchitectureLabel.setTextFill(Color.web("#292929"));
                downloadJDKArchiveTypeLabel.setTextFill(Color.web("#292929"));
                downloadJDKFilenameLabel.setTextFill(Color.web("#292929"));
            } else {
                downloadJDKWindowTitle.setTextFill(Color.web("#dddddd"));
                downloadJDKHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                downloadJDKPane.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(0, 0, 10, 10, false), Insets.EMPTY)));
                downloadJDKMainPane.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(10), Insets.EMPTY)));
                downloadJDKMainPane.setBorder(new Border(new BorderStroke(Color.web("#515352"), BorderStrokeStyle.SOLID, new CornerRadii(10, 10, 10, 10, false), new BorderWidths(1))));
                downloadJDKBundledWithFXCheckBox.setTextFill(Color.web("#dddddd"));
                downloadJDKMajorVersionLabel.setTextFill(Color.web("#dddddd"));
                downloadJDKUpdateLevelLabel.setTextFill(Color.web("#dddddd"));
                downloadJDKDistributionLabel.setTextFill(Color.web("#dddddd"));
                downloadJDKOperatingSystemLabel.setTextFill(Color.web("#dddddd"));
                downloadJDKLibcTypeLabel.setTextFill(Color.web("#dddddd"));
                downloadJDKArchitectureLabel.setTextFill(Color.web("#dddddd"));
                downloadJDKArchiveTypeLabel.setTextFill(Color.web("#dddddd"));
                downloadJDKFilenameLabel.setTextFill(Color.web("#dddddd"));
            }
        } else {
            if (isWindows) {
                downloadJDKWindowTitle.setTextFill(Color.web("#000000"));
                downloadJDKHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                downloadJDKHeaderPane.setBorder(new Border(new BorderStroke(Color.web("#f2f2f2"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0.5, 0))));
                downloadJDKPane.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), CornerRadii.EMPTY, Insets.EMPTY)));
                downloadJDKMainPane.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), CornerRadii.EMPTY, Insets.EMPTY)));
                downloadJDKMainPane.setBorder(new Border(new BorderStroke(Color.web("#f2f2f2"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 1, 1, 1))));
                downloadJDKBundledWithFXCheckBox.setTextFill(Color.web("#2a2a2a"));
                downloadJDKMajorVersionLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKUpdateLevelLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKDistributionLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKOperatingSystemLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKLibcTypeLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKArchitectureLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKArchiveTypeLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKFilenameLabel.setTextFill(Color.web("#2a2a2a"));
            } else {
                downloadJDKWindowTitle.setTextFill(Color.web("#000000"));
                downloadJDKHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#edefef"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                downloadJDKPane.setBackground(new Background(new BackgroundFill(Color.web("#ecebe9"), new CornerRadii(0, 0, 10, 10, false), Insets.EMPTY)));
                downloadJDKMainPane.setBackground(new Background(new BackgroundFill(Color.web("#ecebe9"), new CornerRadii(10), Insets.EMPTY)));
                downloadJDKMainPane.setBorder(new Border(new BorderStroke(Color.web("#f6f4f4"), BorderStrokeStyle.SOLID, new CornerRadii(10, 10, 10, 10, false), new BorderWidths(1))));
                downloadJDKBundledWithFXCheckBox.setTextFill(Color.web("#2a2a2a"));
                downloadJDKMajorVersionLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKUpdateLevelLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKDistributionLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKOperatingSystemLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKLibcTypeLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKArchitectureLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKArchiveTypeLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKFilenameLabel.setTextFill(Color.web("#2a2a2a"));
            }
        }

        return downloadJDKDialog;
    }

    private void selectMajorVersion() {
        downloadJDKSelectedMajorVersion = downloadJDKMajorVersionComboBox.getSelectionModel().getSelectedItem();
        final boolean include_build = downloadJDKSelectedMajorVersion.isEarlyAccessOnly();
        downloadJDKBundledWithFXCheckBox.setDisable(true);
        if (downloadJDKBundledWithFXCheckBox.isSelected()) {
            downloadJDKUpdateLevelComboBox.getItems().clear();
            downloadJDKDistributionComboBox.getItems().clear();
            downloadJDKOperatingSystemComboBox.getItems().clear();
            downloadJDKLibcTypeComboBox.getItems().clear();
            downloadJDKArchitectureComboBox.getItems().clear();
            downloadJDKArchiveTypeComboBox.getItems().clear();
        }
        discoclient.getPkgsForFeatureVersionAsync(downloadJDKDistributionsThatSupportFx, downloadJDKSelectedMajorVersion.getAsInt(), include_build ? List.of(ReleaseStatus.EA) : List.of(ReleaseStatus.GA),
                                                  true, List.of(Scope.PUBLIC, Scope.DIRECTLY_DOWNLOADABLE, Scope.BUILD_OF_OPEN_JDK), Match.ANY)
                   .thenAccept(pkgs -> {
                       downloadJDKSelectedPkgsForMajorVersion.clear();
                       downloadJDKSelectedPkgsForMajorVersion.addAll(pkgs);
                       Platform.runLater(() -> downloadJDKBundledWithFXCheckBox.setDisable(false));
                       if (downloadJDKBundledWithFXCheckBox.isSelected()) {
                           List<SemVer> versionList = downloadJDKSelectedMajorVersion.getVersions()
                                                                                 .stream()
                                                                                 .filter(semVer -> downloadJDKSelectedMajorVersion.isEarlyAccessOnly() ? (semVer.getReleaseStatus() == ReleaseStatus.EA) : (semVer.getReleaseStatus() == ReleaseStatus.GA))
                                                                                 .sorted(Comparator.comparing(SemVer::getVersionNumber).reversed())
                                                                                 .map(semVer -> semVer.getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build))
                                                                                 .distinct()
                                                                                 .map(versionString -> SemVer.fromText(versionString).getSemVer1())
                                                                                 .collect(Collectors.toList());
                           Platform.runLater(() -> {
                               downloadJDKUpdateLevelComboBox.getItems().setAll(versionList);
                               if (versionList.size() > 0) {
                                   downloadJDKUpdateLevelComboBox.getSelectionModel().clearSelection();
                                   downloadJDKUpdateLevelComboBox.getSelectionModel().select(0);
                               }
                           });

                       }
                   });

        if (!downloadJDKBundledWithFXCheckBox.isSelected()) {
            List<SemVer> versionList = downloadJDKSelectedMajorVersion.getVersions()
                                                                  .stream()
                                                                  .filter(semVer -> downloadJDKSelectedMajorVersion.isEarlyAccessOnly() ? (semVer.getReleaseStatus() == ReleaseStatus.EA) : (semVer.getReleaseStatus() == ReleaseStatus.GA))
                                                                  .sorted(Comparator.comparing(SemVer::getVersionNumber).reversed())
                                                                  .map(semVer -> semVer.getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build))
                                                                  .distinct().map(versionString -> SemVer.fromText(versionString).getSemVer1())
                                                                  .collect(Collectors.toList());
            Platform.runLater(() -> {
                downloadJDKUpdateLevelComboBox.getItems().setAll(versionList);
                if (versionList.size() > 0) {
                    downloadJDKUpdateLevelComboBox.getSelectionModel().select(0);
                }
            });
        }
    }

    private void selectVersionNumber() {
        downloadJDKSelectedVersionNumber = downloadJDKUpdateLevelComboBox.getSelectionModel().getSelectedItem();

        List<io.foojay.api.discoclient.pkg.Distribution> distrosForSelection;
        if (downloadJDKJavafxBundled) {
            boolean include_build = downloadJDKSelectedMajorVersion.isEarlyAccessOnly();
            distrosForSelection = downloadJDKSelectedPkgsForMajorVersion.stream()
                                                                    .filter(pkg -> downloadJDKJavafxBundled == pkg.isJavaFXBundled())
                                                                    .filter(pkg -> pkg.getJavaVersion().getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED,true,include_build).equals(
                                                                        downloadJDKSelectedVersionNumber.getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build)))
                                                                    .map(pkg -> pkg.getDistribution())
                                                                    .distinct()
                                                                    .sorted(Comparator.comparing(io.foojay.api.discoclient.pkg.Distribution::getName).reversed())
                                                                    .collect(Collectors.toList());
        } else {
            distrosForSelection = discoclient.getDistributionsForSemVerAsync(downloadJDKSelectedVersionNumber)
                                             .thenApply(distros -> distros.stream()
                                                                          .filter(distro -> distro.getScopes().contains(Scope.DIRECTLY_DOWNLOADABLE))
                                                                          .sorted(Comparator.comparing(io.foojay.api.discoclient.pkg.Distribution::getName).reversed()))
                                             .join()
                                             .collect(Collectors.toList());
        }
        Platform.runLater(() -> {
            downloadJDKDistributionComboBox.getItems().setAll(distrosForSelection);
            if (downloadJDKDistributionComboBox.getItems().size() > 0) {
                if (downloadJDKDistributionComboBox.getItems().get(0).getApiString().equals("zulu_prime") && downloadJDKDistributionComboBox.getItems().size() > 1) {
                    downloadJDKDistributionComboBox.getSelectionModel().select(1);
                } else {
                    downloadJDKDistributionComboBox.getSelectionModel().select(0);
                }
            } else {
                downloadJDKOperatingSystemComboBox.getItems().clear();
                downloadJDKLibcTypeComboBox.getItems().clear();
                downloadJDKArchitectureComboBox.getItems().clear();
                downloadJDKArchiveTypeComboBox.getItems().clear();
            }
        });
    }

    private void selectDistribution() {
        downloadJDKSelectedDistribution = downloadJDKDistributionComboBox.getSelectionModel().getSelectedItem();
        if (null == downloadJDKSelectedDistribution) { return; }

        downloadJDKSelectedPkgs.clear();
        downloadJDKOperatingSystems.clear();
        downloadJDKArchitectures.clear();
        downloadJDKLibcTypes.clear();
        downloadJDKArchiveTypes.clear();

        downloadJDKSelectedPkgs.addAll(discoclient.getPkgs(List.of(downloadJDKSelectedDistribution), VersionNumber.fromText((downloadJDKSelectedVersionNumber).toString(true)), null, null, null, null, null, null, PackageType.JDK,
                                                       null, true, null, null, List.of(Scope.PUBLIC, Scope.DIRECTLY_DOWNLOADABLE, Scope.BUILD_OF_OPEN_JDK), Match.ANY));

        downloadJDKSelectedPkgs.forEach(pkg -> {
            downloadJDKOperatingSystems.add(pkg.getOperatingSystem());
            downloadJDKArchitectures.add(pkg.getArchitecture());
            downloadJDKLibcTypes.add(pkg.getLibCType());
            downloadJDKArchiveTypes.add(pkg.getArchiveType());
        });
        Platform.runLater(() -> {
            downloadJDKOperatingSystemComboBox.getItems().setAll(downloadJDKOperatingSystems);
            int selectIndex = -1;
            for (int i = 0; i < downloadJDKOperatingSystemComboBox.getItems().size() ; i++) {
                if (downloadJDKOperatingSystemComboBox.getItems().get(i) == operatingSystem) {
                    selectIndex = i;
                    break;
                }
            }
            if (-1 == selectIndex) {
                if (downloadJDKOperatingSystems.size() > 0) {
                    downloadJDKOperatingSystemComboBox.getSelectionModel().select(0);
                }
            } else {
                downloadJDKOperatingSystemComboBox.getSelectionModel().select(selectIndex);
            }
        });
    }

    private void selectOperatingSystem() {
        downloadJDKSelectedOperatingSystem = downloadJDKOperatingSystemComboBox.getSelectionModel().getSelectedItem();
        List<Pkg> selection = downloadJDKSelectedPkgs.stream()
                                                 .filter(pkg -> pkg.isJavaFXBundled() == downloadJDKJavafxBundled)
                                                 .filter(pkg -> downloadJDKSelectedDistribution.getApiString().equals(pkg.getDistribution().getApiString()))
                                                 .filter(pkg -> downloadJDKSelectedOperatingSystem == pkg.getOperatingSystem())
                                                 .collect(Collectors.toList());
        downloadJDKLibcTypes = selection.stream().map(pkg -> pkg.getLibCType()).collect(Collectors.toSet());
        Platform.runLater(() -> {
            downloadJDKLibcTypeComboBox.getItems().setAll(downloadJDKLibcTypes);
            if (downloadJDKLibcTypes.size() > 0) {
                downloadJDKLibcTypeComboBox.getSelectionModel().select(0);
            }
        });
    }

    private void selectLibcType() {
        downloadJDKSelectedLibcType = downloadJDKLibcTypeComboBox.getSelectionModel().getSelectedItem();
        List<Pkg> selection = downloadJDKSelectedPkgs.stream()
                                                 .filter(pkg -> pkg.isJavaFXBundled() == downloadJDKJavafxBundled)
                                                 .filter(pkg -> downloadJDKSelectedDistribution.getApiString().equals(pkg.getDistribution().getApiString()))
                                                 .filter(pkg -> downloadJDKSelectedOperatingSystem == pkg.getOperatingSystem())
                                                 .filter(pkg -> downloadJDKSelectedLibcType == pkg.getLibCType())
                                                 .collect(Collectors.toList());
        downloadJDKArchitectures = selection.stream().map(pkg -> pkg.getArchitecture()).collect(Collectors.toSet());
        Platform.runLater(() -> {
            downloadJDKArchitectureComboBox.getItems().clear();
            downloadJDKArchitectures.forEach(architecture -> downloadJDKArchitectureComboBox.getItems().add(architecture));
            downloadJDKArchitectureComboBox.getItems().setAll(downloadJDKArchitectures);
            if (downloadJDKArchitectures.size() > 0) {
                downloadJDKArchitectureComboBox.getSelectionModel().select(0);
            }
        });
    }

    private void selectArchitecture() {
        downloadJDKSelectedArchitecture = downloadJDKArchitectureComboBox.getSelectionModel().getSelectedItem();
        List<Pkg> selection = downloadJDKSelectedPkgs.stream()
                                                 .filter(pkg -> pkg.isJavaFXBundled() == downloadJDKJavafxBundled)
                                                 .filter(pkg -> downloadJDKSelectedDistribution.getApiString().equals(pkg.getDistribution().getApiString()))
                                                 .filter(pkg -> downloadJDKSelectedOperatingSystem == pkg.getOperatingSystem())
                                                 .filter(pkg -> downloadJDKSelectedLibcType == pkg.getLibCType())
                                                 .filter(pkg -> downloadJDKSelectedArchitecture == pkg.getArchitecture())
                                                 .collect(Collectors.toList());
        downloadJDKArchiveTypes = selection.stream().map(pkg -> pkg.getArchiveType()).collect(Collectors.toSet());
        Platform.runLater(() -> {
            downloadJDKArchiveTypeComboBox.getItems().setAll(downloadJDKArchiveTypes);
            if (downloadJDKArchiveTypes.size() > 0) {
                downloadJDKArchiveTypeComboBox.getSelectionModel().select(0);
            }
            selectArchiveType();
        });
    }

    private void selectArchiveType() {
        downloadJDKSelectedArchiveType = downloadJDKArchiveTypeComboBox.getSelectionModel().getSelectedItem();
        update();
    }

    private void update() {
        List<Pkg> selection = downloadJDKSelectedPkgs.stream()
                                                 .filter(pkg -> pkg.isJavaFXBundled() == downloadJDKJavafxBundled)
                                                 .filter(pkg -> downloadJDKSelectedDistribution.getApiString().equals(pkg.getDistribution().getApiString()))
                                                 .filter(pkg -> downloadJDKSelectedOperatingSystem == pkg.getOperatingSystem())
                                                 .filter(pkg -> downloadJDKSelectedLibcType == pkg.getLibCType())
                                                 .filter(pkg -> downloadJDKSelectedArchitecture == pkg.getArchitecture())
                                                 .filter(pkg -> downloadJDKSelectedArchiveType == pkg.getArchiveType())
                                                 .collect(Collectors.toList());
        if (selection.size() > 0) {
            downloadJDKSelectedPkg = selection.get(0);
            Platform.runLater(() -> {
                downloadJDKFilenameLabel.setText(null == downloadJDKSelectedPkg ? "-" : downloadJDKSelectedPkg.getFileName());
                downloadJDKDownloadButton.setDisable(false);
            });
        } else {
            downloadJDKSelectedPkg = null;
            Platform.runLater(() -> {
                downloadJDKFilenameLabel.setText("-");
                downloadJDKDownloadButton.setDisable(true);
            });
        }
    }

    private void downloadPkgDownloadJDK(final Pkg pkg) {
        if (null == pkg) { return; }
        directoryChooser.setTitle("Choose folder for download");

        final File downloadFolder;
        if (PropertyManager.INSTANCE.getBoolean(PropertyManager.REMEMBER_DOWNLOAD_FOLDER)) {
            if (PropertyManager.INSTANCE.getString(PropertyManager.DOWNLOAD_FOLDER).isEmpty()) {
                directoryChooser.setTitle("Choose folder for download");
                downloadFolder = directoryChooser.showDialog(stage);
            } else {
                File folder = new File(PropertyManager.INSTANCE.getString(PropertyManager.DOWNLOAD_FOLDER));
                if (folder.isDirectory()) {
                    downloadFolder = folder;
                } else {
                    downloadFolder = directoryChooser.showDialog(stage);
                }
            }
        } else {
            downloadFolder = directoryChooser.showDialog(stage);
        }
        PropertyManager.INSTANCE.set(PropertyManager.DOWNLOAD_FOLDER, downloadFolder.getAbsolutePath());
        PropertyManager.INSTANCE.storeProperties();

        if (null != downloadFolder) {
            final String directDownloadUri = discoclient.getPkgDirectDownloadUri(pkg.getId());
            if (null == directDownloadUri) {
                new Alert(AlertType.ERROR, "Problem downloading the package, please try again.", ButtonType.CLOSE).show();
                return;
            }
            final String target = downloadFolder.getAbsolutePath() + File.separator + pkg.getFileName();
            Worker<Boolean> worker = createWorker(directDownloadUri, target);
            worker.stateProperty().addListener((o, ov, nv) -> {
                if (nv.equals(State.READY)) {
                } else if (nv.equals(State.RUNNING)) {
                    blocked.set(true);
                    downloadJDKProgressBar.setVisible(true);
                    downloadJDKDownloadButton.setDisable(true);
                    downloadJDKCancelButton.setDisable(true);
                } else if (nv.equals(State.CANCELLED)) {
                    final File file = new File(target);
                    if (file.exists()) { file.delete(); }
                    blocked.set(false);
                    downloadJDKProgressBar.setProgress(0);
                    downloadJDKProgressBar.setVisible(false);
                    downloadJDKDownloadButton.setDisable(false);
                    downloadJDKCancelButton.setDisable(false);
                } else if (nv.equals(State.FAILED)) {
                    final File file = new File(target);
                    if (file.exists()) { file.delete(); }
                    blocked.set(false);
                    downloadJDKProgressBar.setProgress(0);
                    downloadJDKProgressBar.setVisible(false);
                    downloadJDKDownloadButton.setDisable(false);
                    downloadJDKCancelButton.setDisable(false);
                } else if (nv.equals(State.SUCCEEDED)) {
                    blocked.set(false);
                    downloadJDKProgressBar.setProgress(0);
                    downloadJDKProgressBar.setVisible(false);
                    downloadJDKDownloadButton.setDisable(false);
                    downloadJDKCancelButton.setDisable(false);
                    downloadJDKDialog.setResult(Boolean.TRUE);
                    downloadJDKDialog.close();
                } else if (nv.equals(State.SCHEDULED)) {
                    blocked.set(true);
                    downloadJDKProgressBar.setVisible(true);
                    downloadJDKDownloadButton.setDisable(true);
                    downloadJDKCancelButton.setDisable(false);
                }
            });
            worker.progressProperty().addListener((o, ov, nv) -> downloadJDKProgressBar.setProgress(nv.doubleValue() * 100.0));

            if (PropertyManager.INSTANCE.getBoolean(PropertyManager.REMEMBER_DOWNLOAD_FOLDER)) {
                new Thread((Runnable) worker).start();
            } else {
                Alert info = new Alert(AlertType.INFORMATION);
                info.setTitle("JDKMon");
                info.setHeaderText("JDKMon Download Info");
                info.setContentText("Download will be started and update will be saved to " + downloadFolder);
                info.setOnCloseRequest(e -> new Thread((Runnable) worker).start());
                info.show();
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
