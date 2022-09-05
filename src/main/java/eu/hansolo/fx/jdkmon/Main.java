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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.hansolo.cvescanner.CveScanner;
import eu.hansolo.fx.jdkmon.controls.AttentionIndicator;
import eu.hansolo.fx.jdkmon.controls.MacProgress;
import eu.hansolo.fx.jdkmon.controls.MacosWindowButton;
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
import eu.hansolo.fx.jdkmon.tools.Detector.MacosAccentColor;
import eu.hansolo.fx.jdkmon.tools.DistributionCell;
import eu.hansolo.fx.jdkmon.tools.Distro;
import eu.hansolo.fx.jdkmon.tools.Finder;
import eu.hansolo.fx.jdkmon.tools.Fonts;
import eu.hansolo.fx.jdkmon.tools.Helper;
import eu.hansolo.fx.jdkmon.tools.MacosArchitectureCell;
import eu.hansolo.fx.jdkmon.tools.MacosArchiveTypeCell;
import eu.hansolo.fx.jdkmon.tools.MacosDistributionCell;
import eu.hansolo.fx.jdkmon.tools.MacosMajorVersionCell;
import eu.hansolo.fx.jdkmon.tools.MacosOperatingSystemCell;
import eu.hansolo.fx.jdkmon.tools.MacosUpdateLevelCell;
import eu.hansolo.fx.jdkmon.tools.MajorVersionCell;
import eu.hansolo.fx.jdkmon.tools.MinimizedPkg;
import eu.hansolo.fx.jdkmon.tools.OperatingSystemCell;
import eu.hansolo.fx.jdkmon.tools.PropertyManager;
import eu.hansolo.fx.jdkmon.tools.Records.CVE;
import eu.hansolo.fx.jdkmon.tools.Records.SysInfo;
import eu.hansolo.fx.jdkmon.tools.ResizeHelper;
import eu.hansolo.fx.jdkmon.tools.UpdateLevelCell;
import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.ArchiveType;
import eu.hansolo.jdktools.OperatingMode;
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.PackageType;
import eu.hansolo.jdktools.Severity;
import eu.hansolo.jdktools.Verification;
import eu.hansolo.jdktools.scopes.BuildScope;
import eu.hansolo.jdktools.util.OutputFormat;
import eu.hansolo.jdktools.versioning.Semver;
import eu.hansolo.jdktools.versioning.VersionNumber;
import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.MajorVersion;
import io.foojay.api.discoclient.pkg.Pkg;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static eu.hansolo.jdktools.ReleaseStatus.EA;
import static eu.hansolo.jdktools.ReleaseStatus.GA;


/**
 * User: hansolo
 * Date: 24.03.21
 * Time: 15:35
 */
public class Main extends Application {
    public  static final VersionNumber             VERSION                = PropertyManager.INSTANCE.getVersionNumber();
    private static final PseudoClass               DARK_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("dark");
    private final        Image                     dukeNotificationIcon   = new Image(Main.class.getResourceAsStream("duke_notification.png"));
    private final        Image                     dukeStageIcon          = new Image(Main.class.getResourceAsStream("icon128x128.png"));
    private              OperatingSystem           operatingSystem        = Finder.detectOperatingSystem();
    private              Architecture              architecture           = Finder.detectArchitecture();
    private              SysInfo                   sysInfo                = Finder.getOperaringSystemArchitectureOperatingMode();
    private              boolean                   isWindows              = OperatingSystem.WINDOWS == operatingSystem;
    private              CveScanner                cveScanner             = new CveScanner();
    private              List<CVE>                 cves                   = new CopyOnWriteArrayList<>();
    private              String                    cssFile;
    private              Notification.Notifier     notifier;
    private              BooleanProperty           darkMode;
    private              MacosAccentColor          accentColor;
    private              AnchorPane                headerPane;
    private              MacosWindowButton         closeMacWindowButton;
    private              WinWindowButton           closeWinWindowButton;
    private              Label                     windowTitle;
    private              StackPane                 pane;
    private              BorderPane                mainPane;
    private              ScheduledExecutorService  executor;
    private              boolean                   hideMenu;
    private              Stage                     stage;
    private              ObservableList<Distro>    distros;
    private              Finder                    finder;
    private              Label                     titleLabel;
    private              Label                     searchPathLabel;
    private              MacProgress               macProgressIndicator;
    private              WinProgress               winProgressIndicator;
    private              VBox                      titleBox;
    private              Separator                 separator;
    private              VBox                      distroBox;
    private              VBox                      vBox;
    private              List<String>              searchPaths;
    private              List<String>              javafxSearchPaths;
    private              DirectoryChooser          directoryChooser;
    private              ProgressBar               progressBar;
    private              DiscoClient               discoclient;
    private              BooleanProperty           blocked;
    private              AtomicBoolean             checkingForUpdates;
    private              boolean                   trayIconSupported;
    private              ContextMenu               contextMenu;
    private              Worker<Boolean>           worker;
    private              Dialog                    aboutDialog;
    private              Dialog                    downloadJDKDialog;
    private              Dialog                    cveDialog;
    private              ObservableList<Hyperlink> cveLinks;
    private              Stage                     cveStage;
    private              AnchorPane                cveHeaderPane;
    private              Label                     cveWindowTitle;
    private              MacosWindowButton         cveCloseMacWindowButton;
    private              WinWindowButton           cveCloseWinWindowButton;
    private              StackPane                 cvePane;
    private              VBox                      cveBox;
    private              Button                    cveCloseButton;
    private              Timeline                  timeline;
    private              boolean                   isUpdateAvailable;
    private              VersionNumber             latestVersion;
    private              Map<String, Popup>        popups;
    private              Stage                     downloadJDKStage;
    private              AnchorPane                downloadJDKHeaderPane;
    private              Label                     downloadJDKWindowTitle;
    private              MacosWindowButton         downloadJDKCloseMacWindowButton;
    private              WinWindowButton           downloadJDKCloseWinWindowButton;
    private              StackPane                 downloadJDKPane;
    private              CheckBox                  downloadJDKBundledWithFXCheckBox;
    private              Label                     downloadAutoExtractLabel;
    private              ComboBox<MajorVersion>    downloadJDKMajorVersionComboBox;
    private              ComboBox<Semver>          downloadJDKUpdateLevelComboBox;
    private              ComboBox<Distribution>    downloadJDKDistributionComboBox;
    private              ComboBox<OperatingSystem> downloadJDKOperatingSystemComboBox;
    private              ComboBox<Architecture>    downloadJDKArchitectureComboBox;
    private              ComboBox<ArchiveType>     downloadJDKArchiveTypeComboBox;
    private              Label                     downloadJDKFilenameLabel;
    private              Label                     alreadyDownloadedLabel;
    private              Set<MajorVersion>         downloadJDKMaintainedVersions;
    private              List<MinimizedPkg>        downloadJDKSelectedPkgs;
    private              MinimizedPkg              downloadJDKSelectedPkg;
    private              List<MinimizedPkg>        downloadJDKSelectedPkgsForMajorVersion;
    private              List<MinimizedPkg>        downloadJDKMinimizedPkgs;
    private              boolean                   downloadJDKJavafxBundled;
    private              MajorVersion              downloadJDKSelectedMajorVersion;
    private              Semver                    downloadJDKSelectedVersionNumber;
    private              Distribution              downloadJDKSelectedDistribution;
    private              OperatingSystem           downloadJDKSelectedOperatingSystem;
    private              Architecture              downloadJDKSelectedArchitecture;
    private              ArchiveType               downloadJDKSelectedArchiveType;
    private              Set<OperatingSystem>      downloadJDKOperatingSystems;
    private              Set<Architecture>         downloadJDKArchitectures;
    private              Set<ArchiveType>          downloadJDKArchiveTypes;
    private              ProgressBar               downloadJDKProgressBar;
    private              ImageView                 tckTestedTag;
    private              ImageView                 aqavitTestedTag;
    private              Hyperlink                 tckTestedLink;
    private              Hyperlink                 aqavitTestedLink;
    private              Button                    downloadJDKCancelButton;
    private              Button                    downloadJDKDownloadButton;

    private              AtomicBoolean             online;

    public record SemverUri(Semver semver, String uri) {}


    @Override public void init() {
        isUpdateAvailable = false;
        latestVersion     = VERSION;
        popups            = new HashMap<>();
        online            = new AtomicBoolean(false);

        switch (operatingSystem) {
            case WINDOWS -> cssFile = "jdk-mon-win.css";
            case LINUX   -> cssFile = "jdk-mon-linux.css";
            default      -> cssFile = "jdk-mon.css";
        }

        notifier = NotifierBuilder.create()
                                  .owner(stage)
                                  .popupLocation(OperatingSystem.MACOS == Detector.getOperatingSystem() ? Pos.TOP_RIGHT : Pos.BOTTOM_RIGHT)
                                  .popupLifeTime(Duration.millis(5000))
                                  .build();

        pane            = new StackPane();

        cvePane         = new StackPane();

        downloadJDKPane = new StackPane();

        cveCloseButton  = new Button("Close");
        cveCloseButton.getStyleClass().addAll("jdk-mon", "cve-close-button");

        darkMode = new BooleanPropertyBase(false) {
            @Override protected void invalidated() {
                pane.pseudoClassStateChanged(DARK_MODE_PSEUDO_CLASS, get());
                downloadJDKPane.pseudoClassStateChanged(DARK_MODE_PSEUDO_CLASS, get());
                cveCloseButton.pseudoClassStateChanged(DARK_MODE_PSEUDO_CLASS, get());
            }
            @Override public Object getBean() { return Main.this; }
            @Override public String getName() { return "darkMode"; }
        };
        darkMode.set(Detector.isDarkMode());

        if (OperatingSystem.LINUX == operatingSystem) {
            if (PropertyManager.INSTANCE.hasKey(PropertyManager.DARK_MODE)) {
                darkMode.set(PropertyManager.INSTANCE.getBoolean(PropertyManager.DARK_MODE));
            } else {
                PropertyManager.INSTANCE.set(PropertyManager.DARK_MODE, "FALSE");
                PropertyManager.INSTANCE.storeProperties();
            }
        }

        if (PropertyManager.INSTANCE.hasKey(PropertyManager.FEATURES)) {
            if (!PropertyManager.INSTANCE.getString(PropertyManager.FEATURES).contains("crac")) {
                PropertyManager.INSTANCE.set(PropertyManager.FEATURES, "loom,panama,metropolis,valhalla,lanai,kona_fiber,crac");
                PropertyManager.INSTANCE.storeProperties();
            }
        }

        closeMacWindowButton = new MacosWindowButton(WindowButtonType.CLOSE, WindowButtonSize.NORMAL);
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

            AnchorPane.setTopAnchor(closeMacWindowButton, 7.125d);
            AnchorPane.setLeftAnchor(closeMacWindowButton, 11d);
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
            headerPane.setMinHeight(28.75);
            headerPane.setMaxHeight(28.75);
            headerPane.setPrefHeight(28.75);
            headerPane.getChildren().addAll(closeMacWindowButton, windowTitle);
        }

        // Scheduled jobs
        executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(() -> rescan(), Constants.INITIAL_DELAY_IN_SECONDS, Constants.RESCAN_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(() -> cveScanner.updateCves(), Constants.INITIAL_CVE_DELAY_IN_MINUTES, Constants.CVE_UPDATE_INTERVAL_IN_MINUTES, TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(() -> updateDownloadPkgs(), Constants.INITIAL_PKG_DOWNLOAD_DELAY_IN_MINUTES, Constants.UPDATE_PKGS_INTERVAL_IN_MINUTES, TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(() -> isOnline(), Constants.INITIAL_CHECK_DELAY_IN_SECONDS, Constants.CHECK_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);

        discoclient        = new DiscoClient("JDKMon");
        blocked            = new SimpleBooleanProperty(false);
        checkingForUpdates = new AtomicBoolean(false);
        searchPaths        = new ArrayList<>(Arrays.asList(PropertyManager.INSTANCE.getString(PropertyManager.SEARCH_PATH).split(",")));
        javafxSearchPaths  = new ArrayList<>(Arrays.asList(PropertyManager.INSTANCE.getString(PropertyManager.JAVAFX_SEARCH_PATH).split(",")));

        // If on Linux or Mac add .sdkman/candidates/java folder to search paths if present
        if (operatingSystem == OperatingSystem.LINUX || operatingSystem == OperatingSystem.MACOS) {
            if (Detector.isSDKMANInstalled() && searchPaths.stream().filter(path -> path.equals(Detector.SDKMAN_FOLDER)).findFirst().isEmpty()) {
                searchPaths.add(Detector.SDKMAN_FOLDER);
                PropertyManager.INSTANCE.set(PropertyManager.SEARCH_PATH, searchPaths.stream().collect(Collectors.joining(",")));
                PropertyManager.INSTANCE.storeProperties();
            }
        }

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
            if (online.get()) {
                // Find JDK distribution updates
                finder.getAvailableUpdates(distros).entrySet().forEach(entry -> distroEntries.add(getDistroEntry(entry.getKey(), entry.getValue())));

                // Find JavaFX SDK updates
                finder.checkForJavaFXUpdates(javafxSearchPaths).entrySet().forEach(entry -> distroEntries.add(getJavaFXEntry(entry.getKey(), entry.getValue())));
            }
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

        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        MenuItem contextAddSearchPath = new MenuItem("Add JDK search path");
        contextAddSearchPath.getStyleClass().add("context-menu-item");
        contextAddSearchPath.setOnAction(e -> selectSearchPath(false));

        MenuItem contextDefaultSearchPath = new MenuItem("Default JDK search path");
        contextDefaultSearchPath.getStyleClass().add("context-menu-item");
        contextDefaultSearchPath.setOnAction(e -> resetToDefaultSearchPath(false));

        SeparatorMenuItem separator2 = new SeparatorMenuItem();

        MenuItem contextAddJavaFXSearchPath = new MenuItem("Add JavaFX search path");
        contextAddJavaFXSearchPath.getStyleClass().add("context-menu-item");
        contextAddJavaFXSearchPath.setOnAction(e -> selectSearchPath(true));

        MenuItem contextDefaultJavaFXSearchPath = new MenuItem("Default JavaFX search path");
        contextDefaultJavaFXSearchPath.getStyleClass().add("context-menu-item");
        contextDefaultJavaFXSearchPath.setOnAction(e -> resetToDefaultSearchPath(true));

        contextMenu = new ContextMenu(contextRescan, separator1, contextAddSearchPath, contextDefaultSearchPath, separator2, contextAddJavaFXSearchPath, contextDefaultJavaFXSearchPath);
        contextMenu.getStyleClass().add("jdk-mon");
        contextMenu.setAutoHide(true);

        // Adjustments related to accent color
        if (OperatingSystem.MACOS == Detector.getOperatingSystem()) {
            accentColor = Detector.getMacosAccentColor();
            if (darkMode.get()) {
                pane.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorDark()));
                contextMenu.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorDark()));
            } else {
                pane.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorAqua()));
                contextMenu.setStyle("-selection-color: " + Helper.colorToCss(accentColor.getColorAqua()));
            }
        } else {
            accentColor = MacosAccentColor.MULTI_COLOR;
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

        cveLinks                               = FXCollections.observableArrayList();

        downloadJDKMaintainedVersions          = new LinkedHashSet<>();
        downloadJDKSelectedPkgs                = new LinkedList<>();
        downloadJDKSelectedPkg                 = null;
        downloadJDKSelectedPkgsForMajorVersion = new LinkedList<>();
        downloadJDKJavafxBundled               = false;
        downloadJDKOperatingSystems            = new TreeSet<>();
        downloadJDKArchitectures               = new TreeSet<>();
        downloadJDKArchiveTypes                = new TreeSet<>();
        downloadJDKMinimizedPkgs               = new CopyOnWriteArrayList<>();
        updateDownloadPkgs();
    }

    private void registerListeners() {
        cveScanner.addCveEvtConsumer(e -> {
            switch(e.type()) {
                case UPDATED -> updateCves();
            }
        });

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


        cveStage.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                if (darkMode.get()) {
                    if (isWindows) {
                        cveHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        cveWindowTitle.setTextFill(Color.web("#969696"));
                    } else {
                        cveHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        cveWindowTitle.setTextFill(Color.web("#dddddd"));
                    }
                } else {
                    if (isWindows) {
                        cveHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        cveWindowTitle.setTextFill(Color.web("#000000"));
                    } else {
                        cveHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#edefef"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        cveWindowTitle.setTextFill(Color.web("#000000"));
                    }
                }
                cveCloseMacWindowButton.setDisable(false);
                cveCloseWinWindowButton.setDisable(false);
            } else {
                if (darkMode.get()) {
                    if (isWindows) {
                        cveHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#000000"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        cveWindowTitle.setTextFill(Color.web("#969696"));
                    } else {
                        cveHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#282927"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        cveWindowTitle.setTextFill(Color.web("#696a68"));
                    }
                } else {
                    if (isWindows) {
                        cveCloseWinWindowButton.setStyle("-fx-fill: #969696;");
                    } else {
                        cveHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#e5e7e7"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                        cveWindowTitle.setTextFill(Color.web("#a9a6a6"));
                        cveCloseMacWindowButton.setStyle("-fx-fill: #ceccca;");
                    }
                }
                cveCloseMacWindowButton.setDisable(true);
                cveCloseWinWindowButton.setDisable(true);
            }
        });

        cveCloseButton.setOnAction(e -> {
            if (cveDialog.isShowing()) {
                cveDialog.setResult(Boolean.TRUE);
                cveDialog.close();
            }
        });

        if (isWindows) {
            cveCloseWinWindowButton.setOnMouseReleased((Consumer<MouseEvent>) e -> {
                if (cveDialog.isShowing()) {
                    cveDialog.setResult(Boolean.TRUE);
                    cveDialog.close();
                }
            });
            cveCloseWinWindowButton.setOnMouseEntered(e -> cveCloseWinWindowButton.setHovered(true));
            cveCloseWinWindowButton.setOnMouseExited(e -> cveCloseWinWindowButton.setHovered(false));

            downloadJDKCloseWinWindowButton.setOnMouseReleased((Consumer<MouseEvent>) e -> {
                if (downloadJDKDialog.isShowing()) {
                    downloadJDKDialog.setResult(Boolean.TRUE);
                    downloadJDKDialog.close();
                }
            });
            downloadJDKCloseWinWindowButton.setOnMouseEntered(e -> downloadJDKCloseWinWindowButton.setHovered(true));
            downloadJDKCloseWinWindowButton.setOnMouseExited(e -> downloadJDKCloseWinWindowButton.setHovered(false));
        } else {
            cveCloseMacWindowButton.setOnMouseReleased((Consumer<MouseEvent>) e -> {
                if (cveDialog.isShowing()) {
                    cveDialog.setResult(Boolean.TRUE);
                    cveDialog.close();
                }
            });
            cveCloseMacWindowButton.setOnMouseEntered(e -> cveCloseMacWindowButton.setHovered(true));
            cveCloseMacWindowButton.setOnMouseExited(e -> cveCloseMacWindowButton.setHovered(false));

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
            if (null == downloadJDKSelectedMajorVersion) { return; }
            downloadJDKJavafxBundled = nv;

            boolean include_build = downloadJDKSelectedMajorVersion.isEarlyAccessOnly();

            List<Distribution> distrosForSelection = downloadJDKMinimizedPkgs.stream()
                                                                             .filter(pkg -> pkg.getJavaVersion().getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build).equals(downloadJDKSelectedVersionNumber.getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build)))
                                                                             .map(pkg -> pkg.getDistribution())
                                                                             .distinct()
                                                                             .sorted(Comparator.comparing(io.foojay.api.discoclient.pkg.Distribution::getName).reversed())
                                                                             .collect(Collectors.toList());

            Platform.runLater(() -> {
                downloadJDKDistributionComboBox.getItems().setAll(distrosForSelection);

                downloadJDKDistributionComboBox.getItems().clear();
                downloadJDKOperatingSystemComboBox.getItems().clear();
                downloadJDKArchitectureComboBox.getItems().clear();
                downloadJDKArchiveTypeComboBox.getItems().clear();
                downloadJDKFilenameLabel.setText("-");

                if (downloadJDKUpdateLevelComboBox.getItems().size() > 0) {
                    downloadJDKUpdateLevelComboBox.getSelectionModel().select(0);
                    selectVersionNumber();
                } else if (distrosForSelection.size() > 0) {
                    downloadJDKDistributionComboBox.getSelectionModel().select(0);
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

        downloadJDKArchitectureComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (null == downloadJDKMajorVersionComboBox.getSelectionModel().getSelectedItem())    { return; }
            if (null == downloadJDKUpdateLevelComboBox.getSelectionModel().getSelectedItem())     { return; }
            if (null == downloadJDKDistributionComboBox.getSelectionModel().getSelectedItem())    { return; }
            if (null == downloadJDKOperatingSystemComboBox.getSelectionModel().getSelectedItem()) { return; }
            if (null == downloadJDKArchitectureComboBox.getSelectionModel().getSelectedItem())    { return; }
            if (downloadJDKSelectedPkgs.isEmpty()) { return; }
            selectArchitecture();
        });

        downloadJDKArchiveTypeComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (null == nv) { return; }
            selectArchiveType();

            final boolean downloadAndExtract = PropertyManager.INSTANCE.getBoolean(PropertyManager.AUTO_EXTRACT);
            if (downloadAndExtract && (ArchiveType.TAR_GZ == downloadJDKSelectedPkg.getArchiveType() || ArchiveType.ZIP == downloadJDKSelectedPkg.getArchiveType())) {
                downloadAutoExtractLabel.setVisible(true);
            } else {
                downloadAutoExtractLabel.setVisible(false);
            }
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
        cveDialog         = createCveDialog();

        registerListeners();
        if (online.get()) {
            discoclient.getMaintainedMajorVersionsAsync(true, true).thenAccept(uv -> {
                downloadJDKMaintainedVersions.addAll(uv);
                downloadJDKMajorVersionComboBox.getItems().setAll(downloadJDKMaintainedVersions);
                if (downloadJDKMaintainedVersions.size() > 0) {
                    downloadJDKMajorVersionComboBox.getSelectionModel().select(0);
                }
            });

            updateCves();
        }
    }


    @Override public void start(final Stage stage) {
        initOnFXApplicationThread();

        this.stage = stage;
        this.stage.setMinWidth(402);
        this.stage.setMinHeight(312);
        this.trayIconSupported = FXTrayIcon.isSupported();

        if (trayIconSupported) {
            FXTrayIcon trayIcon;
            if (OperatingSystem.LINUX == operatingSystem && (Architecture.AARCH64 == architecture || Architecture.ARM64 == architecture)) {
                trayIcon = new FXTrayIcon(stage, getClass().getResource("duke_blk.png"), 16, 16);
            } else {
                trayIcon = new FXTrayIcon(stage, getClass().getResource("duke.png"), 16, 16);
            }

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

            MenuItem searchPathItem = new MenuItem("Add JDK search path");
            searchPathItem.setOnAction(e -> selectSearchPath(false));
            trayIcon.addMenuItem(searchPathItem);

            MenuItem defaultSearchPathItem = new MenuItem("Default JDK search path");
            defaultSearchPathItem.setOnAction(e -> resetToDefaultSearchPath(false));
            trayIcon.addMenuItem(defaultSearchPathItem);

            trayIcon.addSeparator();

            MenuItem javafxSearchPathItem = new MenuItem("Add JavaFX search path");
            javafxSearchPathItem.setOnAction(e -> selectSearchPath(true));
            trayIcon.addMenuItem(javafxSearchPathItem);

            MenuItem defaultJavaFXSearchPathItem = new MenuItem("Default JavaFX search path");
            defaultJavaFXSearchPathItem.setOnAction(e -> resetToDefaultSearchPath(true));
            trayIcon.addMenuItem(defaultJavaFXSearchPathItem);

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
            Menu menu = new Menu("JDKMon");
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
            Label searchPathLabel = new Label("Add JDK search path");
            searchPathLabel.setTooltip(new Tooltip("Add another folder that should be scanned for JDK's"));
            searchPathLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            searchPathLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            searchPathItem.setContent(searchPathLabel);
            searchPathItem.setHideOnClick(false);
            searchPathItem.setOnAction( e -> selectSearchPath(false));
            menu.getItems().add(searchPathItem);

            CustomMenuItem defaultSearchPathItem = new CustomMenuItem();
            Label defaultSearchPathLabel = new Label("Default JDK search path");
            defaultSearchPathLabel.setTooltip(new Tooltip("Reset JDK search paths to default"));
            defaultSearchPathLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            defaultSearchPathLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            defaultSearchPathItem.setContent(defaultSearchPathLabel);
            defaultSearchPathItem.setHideOnClick(false);
            defaultSearchPathItem.setOnAction( e -> resetToDefaultSearchPath(false));
            menu.getItems().add(defaultSearchPathItem);

            menu.getItems().add(new SeparatorMenuItem());

            CustomMenuItem javafxSearchPathItem = new CustomMenuItem();
            Label javafxSearchPathLabel = new Label("Add JavaFX search path");
            javafxSearchPathLabel.setTooltip(new Tooltip("Add another folder that should be scanned for JavaFX"));
            javafxSearchPathLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            javafxSearchPathLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            javafxSearchPathItem.setContent(javafxSearchPathLabel);
            javafxSearchPathItem.setHideOnClick(false);
            javafxSearchPathItem.setOnAction( e -> selectSearchPath(true));
            menu.getItems().add(javafxSearchPathItem);

            CustomMenuItem defaultJavaFXSearchPathItem = new CustomMenuItem();
            Label defaultJavaFXSearchPathLabel = new Label("Default JavaFX search path");
            defaultJavaFXSearchPathLabel.setTooltip(new Tooltip("Reset JavaFX search paths to default"));
            defaultJavaFXSearchPathLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hideMenu = false);
            defaultJavaFXSearchPathLabel.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideMenu = true);
            defaultJavaFXSearchPathItem.setContent(defaultJavaFXSearchPathLabel);
            defaultJavaFXSearchPathItem.setHideOnClick(false);
            defaultJavaFXSearchPathItem.setOnAction( e -> resetToDefaultSearchPath(true));
            menu.getItems().add(defaultJavaFXSearchPathItem);

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

            MenuBar menuBar = new MenuBar();
            menuBar.setUseSystemMenuBar(true);
            menuBar.getMenus().add(menu);

            titleBox.getChildren().add(0, menuBar);
        }

        Scene scene;
        if (OperatingSystem.LINUX == operatingSystem && (Architecture.AARCH64 == architecture || Architecture.ARM64 == architecture)) {
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


    private final void isOnline() {
        try {
            if (!online.get()) {
                URL           url        = new URL(Constants.TEST_CONNECTIVITY_URL);
                URLConnection connection = url.openConnection();
                connection.connect();
                online.set(true);
                updateDownloadPkgs();
                rescan();
            }
        } catch (IOException e) {
            online.set(false);
        }
    }

    private void updateCves() {
        List<CveScanner.CVE> cvesFound = cveScanner.getCves();
        if (cvesFound.isEmpty()) { return; }
        cves.clear();
        cvesFound.forEach(cve -> {
            final String   id       = cve.id();
            final double              score            = cve.score();
            final Severity            severity         = Severity.fromText(cve.severity().getApiString());
            final List<VersionNumber> affectedVersions = cve.affectedVersions().stream().map(v -> VersionNumber.fromText(v)).collect(Collectors.toList());
            cves.add(new CVE(id, score, severity, affectedVersions));
        });
        checkForUpdates();
    }

    private void updateDownloadPkgs() {
        downloadJDKPane.setDisable(true);
        Helper.getAsync(Constants.ALL_PKGS_MINIMIZED_URI).thenAccept(response -> {
                if (null == response) { return; }
                final String bodyText = response.body();
                if (null == bodyText || bodyText.isEmpty()) { return; }
                final Gson        gson    = new Gson();
                final JsonElement element = gson.fromJson(response.body(), JsonElement.class);
                if (element instanceof JsonObject) {
                    final List<MinimizedPkg> pkgs       = new ArrayList<>();
                    final JsonObject         jsonObject = element.getAsJsonObject();
                    final JsonArray          jsonArray  = jsonObject.getAsJsonArray("result");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        try {
                            final JsonObject json = jsonArray.get(i).getAsJsonObject();
                            pkgs.add(new MinimizedPkg(json.toString()));
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                    if (!pkgs.isEmpty()) {
                        Platform.runLater(() -> {
                            downloadJDKMinimizedPkgs.clear();
                            downloadJDKMinimizedPkgs.addAll(pkgs);

                            // Major Versions
                            discoclient.getMaintainedMajorVersionsAsync(true, true).thenAccept(uv -> {
                                downloadJDKMaintainedVersions.addAll(uv);
                                downloadJDKMajorVersionComboBox.getItems().setAll(downloadJDKMaintainedVersions);
                                if (downloadJDKMaintainedVersions.size() > 0) {
                                    downloadJDKMajorVersionComboBox.getSelectionModel().select(0);
                                    downloadJDKSelectedMajorVersion = downloadJDKMajorVersionComboBox.getSelectionModel().getSelectedItem();
                                }
                            });

                            downloadJDKPane.setDisable(false);
                            selectMajorVersion();
                        });
                    }
                }
            });
    }

    private void rescan() {
        Platform.runLater(() -> {
            checkForLatestJDKMonVersion();
            if (checkingForUpdates.get()) { return; }
            if (isWindows) {
                winProgressIndicator.setVisible(true);
                winProgressIndicator.setIndeterminate(true);
            } else {
                macProgressIndicator.setVisible(true);
                macProgressIndicator.setIndeterminate(true);
            }
            Set<Distro> distrosFound = finder.getDistributions(searchPaths);
            distros.setAll(distrosFound);
            SwingUtilities.invokeLater(() -> checkForUpdates());
        });
    }

    private void checkForUpdates() {
        if (!online.get()) { return; }
        checkingForUpdates.set(true);
        AtomicBoolean updatesAvailable = new AtomicBoolean(false);
        StringBuilder msgBuilder       = new StringBuilder();
        List<Node>    distroEntries    = new ArrayList<>();

        finder.getAvailableUpdates(distros).entrySet().forEach(entry -> {
            HBox distroEntry = getDistroEntry(entry.getKey(), entry.getValue());
            distroEntries.add(distroEntry);
            if (distroEntry.getChildren().size() > 2 && distroEntry.getChildren().get(2) instanceof Label) {
                msgBuilder.append(entry.getKey().getName()).append(" ").append(((Label) distroEntry.getChildren().get(2)).getText()).append("\n");
                updatesAvailable.set(true);
            }
        });

        finder.checkForJavaFXUpdates(javafxSearchPaths).entrySet().forEach(entry -> {
            HBox javafxEntry = getJavaFXEntry(entry.getKey(), entry.getValue());
            distroEntries.add(javafxEntry);
        });

        Platform.runLater(() -> {
            int numberOfDistros = distroBox.getChildren().size();
            distroBox.getChildren().setAll(distroEntries);
            stage.sizeToScene();
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

    private HBox getDistroEntry(final Distro distribution, final List<Pkg> pkgs) {
        final boolean isDistributionInUse = distribution.isInUse();

        List<CVE> vulnerabilities = Helper.getCVEsForVersion(cves, VersionNumber.fromText(distribution.getVersion()));

        StringBuilder distroLabelBuilder = new StringBuilder(distribution.getName()).append(distribution.getFeature().isEmpty() ? "" : " (" + distribution.getFeature() + ")")
                                                                                    .append(distribution.getFxBundled() ? " (FX)" : "")
                                                                                    .append("  ")
                                                                                    .append(distribution.getVersion())
                                                                                    .append(BuildScope.BUILD_OF_GRAALVM == distribution.getBuildScope() ? " (JDK" + distribution.getJdkMajorVersion() + ")" : "")
                                                                                    .append(isDistributionInUse ? "*" : "");

        Label distroLabel = new Label(distroLabelBuilder.toString());
        distroLabel.setMinWidth(220);
        distroLabel.setAlignment(Pos.CENTER_LEFT);
        distroLabel.setMaxWidth(Double.MAX_VALUE);

        distroLabel.setTooltip(new Tooltip(isDistributionInUse ? "(Currently in use) " + distribution.getLocation() : distribution.getLocation()));
        distroLabel.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) {
                openDistribution(distribution);
            }
        });

        // Vulnerabilities
        AttentionIndicator attentionIndicator = new AttentionIndicator();
        attentionIndicator.setTooltip(new Tooltip("Possible vulnerabilities found"));
        if (vulnerabilities.isEmpty()) {
            attentionIndicator.setVisible(false);
        } else {
            final boolean isDarkMode = darkMode.get();
            List<Hyperlink> cveLinksFound = new ArrayList<>();
            vulnerabilities.forEach(cve -> {
                Hyperlink cveLink = new Hyperlink();
                cveLink.setTooltip(new Tooltip(distribution.getName() + " " + distribution.getVersion() + " might be affected by " + cve.id()));
                cveLink.setFont(isWindows ? Fonts.segoeUi(12) : Fonts.sfPro(12));
                cveLink.setText(cve.id() + " (Score " + String.format(Locale.US, "%.1f", cve.score()) + ", Severity " + cve.severity().getUiString() + ")");
                cveLink.setOnAction(e -> {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(new URI(cve.url()));
                        } catch (IOException | URISyntaxException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                cveLink.setTextFill(Color.BLACK);
                cveLink.setBackground(new Background(new BackgroundFill(Helper.getColorForCVE(cve, isDarkMode), new CornerRadii(5), Insets.EMPTY)));
                cveLinksFound.add(cveLink);
            });
            attentionIndicator.setVisible(true);
            attentionIndicator.setOnMousePressed(e -> {
                cveLinks.setAll(cveLinksFound);
                cveBox.getChildren().setAll(cveLinks);
                cveDialog.showAndWait();
            });
        }

        HBox distroBox = new HBox(3, distroLabel, attentionIndicator);
        distroBox.setAlignment(Pos.CENTER);

        HBox hBox = new HBox(5, distroBox);
        hBox.setMinWidth(360);

        if (pkgs.isEmpty()) { return hBox; }
        Collections.sort(pkgs, Comparator.comparing(Pkg::getDistributionName).reversed());

        Optional<Pkg> optFirstPkg = pkgs.stream()
                                        .filter(pkg -> !pkg.getDistribution().getApiString().toLowerCase().startsWith("graal"))
                                        .filter(pkg -> !pkg.getDistribution().getApiString().equalsIgnoreCase("liberica_native"))
                                        .filter(pkg -> !pkg.getDistribution().getApiString().equalsIgnoreCase("mandrel"))
                                        .filter(pkg -> !pkg.getDistribution().getApiString().toLowerCase().startsWith("gluon"))
                                        .sorted(Comparator.comparing(Pkg::getDistributionName).reversed())
                                        .findFirst();
        if (optFirstPkg.isEmpty()) { return hBox; }
        
        Pkg     firstPkg         = optFirstPkg.get();
        String  nameToCheck      = firstPkg.getDistribution().getApiString();
        Boolean fxBundledToCheck = firstPkg.isJavaFXBundled();
        String  versionToCheck   = firstPkg.getJavaVersion().getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, false);
        for (Distro distro : distros) {
            if (distro.getApiString().equals(nameToCheck) && distro.getVersion().equals(versionToCheck) && distro.getFxBundled() == fxBundledToCheck) {
                return hBox;
            }
        }

        // If available update is already installed don't show it
        if (distros.stream()
                   .filter(d -> d.getApiString().equals(firstPkg.getDistribution().getApiString()))
                   .filter(d -> VersionNumber.equalsExceptBuild(VersionNumber.fromText(d.getVersion()), firstPkg.getJavaVersion().getVersionNumber()))
                   .count() > 0) {
            return hBox;
        }

        Optional<Pkg> optionalZulu = pkgs.parallelStream()
                                         .sorted(Comparator.comparing(Pkg::getDistributionName).reversed())
                                         .filter(pkg -> pkg.getDistribution().getApiString().equals("zulu"))
                                         .findFirst();

        Optional<Pkg> optionalOpenJDK = pkgs.parallelStream()
                                            .sorted(Comparator.comparing(Pkg::getDistributionName).reversed())
                                            .filter(pkg -> pkg.getDistribution().getApiString().equals("oracle_open_jdk"))
                                            .findFirst();

        // TODO: Check if that works before next release
        //if (optionalZulu.isEmpty() && optionalOpenJDK.isEmpty()) { return hBox; }

        Label  arrowLabel   = new Label(" -> ");
        hBox.getChildren().add(arrowLabel);


        // ******************** Create popup **********************************
        Popup popup = new Popup();

        WinWindowButton   closePopupWinButton   = new WinWindowButton(WindowButtonType.CLOSE, WindowButtonSize.SMALL);
        MacosWindowButton closePopupMacOSButton = new MacosWindowButton(WindowButtonType.CLOSE, WindowButtonSize.SMALL);

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
        popupPane.setCenter(popupContent);
        popupPane.setTop(popupHeader);

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

        final String downloadFolder        = PropertyManager.INSTANCE.getString(PropertyManager.DOWNLOAD_FOLDER);
        final String distributionApiString = distribution.getApiString();
        final Semver availableJavaVersion  = firstPkg.getJavaVersion();
        if (distributionApiString.equals(nameToCheck)) {
            Label versionLabel = new Label(availableJavaVersion.toString(true));
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
            boolean     alreadyDownloaded = false;
            String      filename          = pkg.getFileName();
            ArchiveType archiveType       = pkg.getArchiveType();
            Label       archiveTypeLabel  = new Label(archiveType.getUiString());
            archiveTypeLabel.getStyleClass().add("tag-label");
            if (null != downloadFolder && !downloadFolder.isEmpty()) {
                alreadyDownloaded = new File(downloadFolder + File.separator + filename).exists();
            }
            if (pkg.isDirectlyDownloadable()) {
                if (alreadyDownloaded) {
                    archiveTypeLabel.setTooltip(new Tooltip(new StringBuilder().append("Already download ").append(filename).toString()));
                } else {
                    archiveTypeLabel.setTooltip(new Tooltip(new StringBuilder().append("Download ").append(filename).append(Verification.YES == pkg.getTckTested() ? " (TCK tested)" : "").toString()));
                }
                switch (archiveType) {
                    case APK, BIN, CAB, EXE, MSI, ZIP -> archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacosAccentColor.GREEN.getColorDark() : MacosAccentColor.GREEN.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
                    case DEB, TAR, TAR_GZ, TAR_Z, RPM -> archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacosAccentColor.ORANGE.getColorDark() : MacosAccentColor.ORANGE.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
                    case PKG, DMG -> archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacosAccentColor.YELLOW.getColorDark() : MacosAccentColor.YELLOW.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
                }
            } else {
                archiveTypeLabel.setTooltip(new Tooltip("Go to download page"));
                archiveTypeLabel.setTextFill(Color.WHITE);
                archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacosAccentColor.GRAPHITE.getColorDark() : MacosAccentColor.GRAPHITE.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
            }
            archiveTypeLabel.disableProperty().bind(blocked);
            if (pkg.isDirectlyDownloadable()) {
                if (alreadyDownloaded) {
                    archiveTypeLabel.setOnMouseClicked(e -> { openFileLocation(new File(downloadFolder)); });
                } else {
                    archiveTypeLabel.setOnMouseClicked(e -> { if (!blocked.get()) { downloadPkg(pkg); } });
                }
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
        final String releaseDetailsUrl = discoclient.getReleaseDetailsUrl(availableJavaVersion);
        if (null != releaseDetailsUrl && !releaseDetailsUrl.isEmpty() && firstPkg.getReleaseStatus() != EA) {
            Label releaseDetailsLabel = new Label("?");
            releaseDetailsLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacosAccentColor.BLUE.getColorDark() : MacosAccentColor.BLUE.getColorAqua(), new CornerRadii(10), Insets.EMPTY)));
            releaseDetailsLabel.getStyleClass().add("release-details-label");
            releaseDetailsLabel.setTooltip(new Tooltip("Release Details"));
            releaseDetailsLabel.setOnMouseClicked(e -> { if (!blocked.get()) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(releaseDetailsUrl));
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            } });

            hBox.getChildren().add(releaseDetailsLabel);
        }
        return hBox;
    }

    private HBox getJavaFXEntry(final Semver existingSemver, final SemverUri semverUri) {
        final String uri = semverUri.uri();

        Label javafxSDKLabel = new Label(new StringBuilder("JavaFX SDK ").append(existingSemver.toString(true)).toString());
        javafxSDKLabel.setMinWidth(220);
        javafxSDKLabel.setAlignment(Pos.CENTER_LEFT);
        javafxSDKLabel.setMaxWidth(Double.MAX_VALUE);

        Label spacerLabel = new Label("!");
        spacerLabel.setBackground(new Background(new BackgroundFill(Color.rgb(255, 214, 10), new CornerRadii(10), Insets.EMPTY)));
        spacerLabel.getStyleClass().add("attention-label");
        spacerLabel.setTooltip(new Tooltip(""));
        spacerLabel.setVisible(false);

        HBox hBox = new HBox(5, javafxSDKLabel, spacerLabel);
        hBox.setMinWidth(360);

        if (uri.isEmpty()) { return hBox; }

        Label  arrowLabel   = new Label(" -> ");
        hBox.getChildren().add(arrowLabel);

        Label versionLabel = new Label(semverUri.semver().toString(true));
        versionLabel.setMinWidth(56);
        hBox.getChildren().add(versionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hBox.getChildren().add(spacer);

        ArchiveType archiveType      = ArchiveType.getFromFileName(uri.substring(uri.lastIndexOf("/")));
        Label       archiveTypeLabel = new Label(archiveType.getUiString());
        archiveTypeLabel.getStyleClass().add("tag-label");
        archiveTypeLabel.setTooltip(new Tooltip("Download JavaFX SDK" + existingSemver.toString(true)));
        switch (archiveType) {
            case APK, BIN, CAB, EXE, MSI, ZIP -> archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacosAccentColor.GREEN.getColorDark() : MacosAccentColor.GREEN.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
            case DEB, TAR, TAR_GZ, TAR_Z, RPM -> archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacosAccentColor.ORANGE.getColorDark() : MacosAccentColor.ORANGE.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
            case PKG, DMG -> archiveTypeLabel.setBackground(new Background(new BackgroundFill(darkMode.get() ? MacosAccentColor.YELLOW.getColorDark() : MacosAccentColor.YELLOW.getColorAqua(), new CornerRadii(2.5), Insets.EMPTY)));
        }
        archiveTypeLabel.disableProperty().bind(blocked);
        archiveTypeLabel.setOnMouseClicked(e -> { if (!blocked.get()) { downloadJavaFXSDK(uri); }});
        hBox.getChildren().add(archiveTypeLabel);
        return hBox;
    }

    private void openDistribution(Distro distribution) {
        openFileLocation(new File(distribution.getLocation()));
    }

    private void openFileLocation(final File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Dialog createAboutDialog() {
        final boolean isDarkMode = darkMode.get();

        Dialog aboutDialog = new Dialog();
        aboutDialog.initOwner(stage);
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

        boolean rosetta2 = OperatingMode.EMULATED == sysInfo.operatingMode() && OperatingSystem.MACOS == sysInfo.operatingSystem();
        String environment = new StringBuilder().append("(")
                                                .append(sysInfo.operatingSystem().getUiString()).append(", ")
                                                .append(sysInfo.architecture().getUiString())
                                                .append(rosetta2 ? " (Rosetta2)" : "")
                                                .append(")").toString();
        Label environmentLabel = new Label(environment);
        environmentLabel.setFont(isWindows ? Fonts.segoeUi(12) : Fonts.sfPro(12));

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
            if (OperatingSystem.MACOS == Detector.getOperatingSystem()) {
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
        } else if (OperatingSystem.MACOS == operatingSystem) {
            descriptionLabel.setFont(Fonts.sfPro(11));
        } else if (OperatingSystem.LINUX == operatingSystem) {
            descriptionLabel.setFont(Fonts.sfPro(11));
        }
        descriptionLabel.setTextAlignment(TextAlignment.LEFT);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setAlignment(Pos.TOP_LEFT);

        VBox aboutTextBox = new VBox(10, nameLabel, versionLabel, environmentLabel, updateNode, descriptionLabel);

        HBox aboutBox = new HBox(20, aboutImage, aboutTextBox);
        aboutBox.setAlignment(Pos.CENTER);
        aboutBox.setPadding(new Insets(20, 20, 10, 20));
        aboutBox.setMinSize(420, 200);
        aboutBox.setMaxSize(420, 200);
        aboutBox.setPrefSize(420, 200);


        if (OperatingSystem.LINUX == operatingSystem && (Architecture.AARCH64 == architecture || Architecture.ARM64 == architecture)) {
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
                environmentLabel.setTextFill(Color.web("#dddddd"));
                descriptionLabel.setTextFill(Color.web("#dddddd"));
            } else {
                aboutBox.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 10, 10, false), Insets.EMPTY)));
                nameLabel.setTextFill(Color.web("#dddddd"));
                versionLabel.setTextFill(Color.web("#dddddd"));
                environmentLabel.setTextFill(Color.web("#dddddd"));
                descriptionLabel.setTextFill(Color.web("#dddddd"));
            }
        } else {
            if (isWindows) {
                aboutBox.setBackground(new Background(new BackgroundFill(Color.web("#ffffff"), CornerRadii.EMPTY, Insets.EMPTY)));
                nameLabel.setTextFill(Color.web("#2a2a2a"));
                versionLabel.setTextFill(Color.web("#2a2a2a"));
                environmentLabel.setTextFill(Color.web("#2a2a2a"));
                descriptionLabel.setTextFill(Color.web("#2a2a2a"));
            } else {
                aboutBox.setBackground(new Background(new BackgroundFill(Color.web("#efedec"), new CornerRadii(10, 10, 10, 10, false), Insets.EMPTY)));
                nameLabel.setTextFill(Color.web("#2a2a2a"));
                versionLabel.setTextFill(Color.web("#2a2a2a"));
                environmentLabel.setTextFill(Color.web("#2a2a2a"));
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
            final boolean alreadyDownloaded = new File(downloadFolder + File.separator + pkg.getFileName()).exists();
            final String  directDownloadUri = discoclient.getPkgDirectDownloadUri(pkg.getId());
            if (null == directDownloadUri) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.initOwner(stage);
                alert.setContentText("Problem downloading the package, please try again.");
                alert.getButtonTypes().add(ButtonType.CLOSE);
                alert.show();
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

            if (alreadyDownloaded) {
                openFileLocation(new File(downloadFolder.getAbsolutePath()));
            } else if (PropertyManager.INSTANCE.getBoolean(PropertyManager.REMEMBER_DOWNLOAD_FOLDER)) {
                new Thread((Runnable) worker).start();
            } else {
                Alert info = new Alert(AlertType.INFORMATION);
                info.initOwner(stage);
                info.setTitle("JDKMon");
                info.setHeaderText("JDKMon Download Info");
                info.setContentText("Download will be started and file will be saved to " + downloadFolder);
                info.setOnCloseRequest(e -> new Thread((Runnable) worker).start());
                info.show();
            }
        }
    }

    private void downloadJavaFXSDK(final String uri) {
        if (null == uri || uri.isEmpty()) { return; }
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
            final String filename = uri.substring(uri.lastIndexOf("/"));
            final String target = downloadFolder.getAbsolutePath() + File.separator + filename;
            worker = createWorker(uri, target);
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

    private void selectSearchPath(final boolean javafx) {
        stage.show();
        if (javafx) {
            boolean javafxSearchPathExists;
            if (javafxSearchPaths.isEmpty()) {
                javafxSearchPathExists = false;
            } else {
                javafxSearchPathExists = new File(javafxSearchPaths.get(0)).exists();
            }
            directoryChooser.setTitle("Add JavaFX search path");
            directoryChooser.setInitialDirectory(javafxSearchPathExists ? new File(javafxSearchPaths.get(0)) : new File(System.getProperty("user.home")));
            final File selectedFolder = directoryChooser.showDialog(stage);
            if (null != selectedFolder) {
                String javafxSearchPath = selectedFolder.getAbsolutePath() + File.separator;
                if (javafxSearchPaths.contains(javafxSearchPath)) { return; }
                javafxSearchPaths.add(javafxSearchPath);
                PropertyManager.INSTANCE.set(PropertyManager.JAVAFX_SEARCH_PATH, javafxSearchPaths.stream().collect(Collectors.joining(",")));
                PropertyManager.INSTANCE.storeProperties();
                rescan();
            }
        } else {
            boolean searchPathExists;
            if (searchPaths.isEmpty()) {
                searchPathExists = false;
            } else {
                searchPathExists = new File(searchPaths.get(0)).exists();
            }
            directoryChooser.setTitle("Add JDK search path");
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
    }

    private void resetToDefaultSearchPath(final boolean javafx) {
        stage.show();
        PropertyManager.INSTANCE.resetSearchPathProperty(javafx);
        if (javafx) {
            javafxSearchPaths.clear();
            javafxSearchPaths.addAll(Arrays.asList(PropertyManager.INSTANCE.getString(PropertyManager.JAVAFX_SEARCH_PATH).split(",")));
        } else {
            distros.clear();
            searchPaths.clear();
            searchPaths.addAll(Arrays.asList(PropertyManager.INSTANCE.getString(PropertyManager.SEARCH_PATH).split(",")));
            searchPathLabel.setText(searchPaths.stream().collect(Collectors.joining(", ")));
        }
        rescan();
    }

    private void checkForLatestJDKMonVersion() {
        if (!online.get()) { return; }
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

    private Dialog createCveDialog() {
        final boolean isDarkMode = darkMode.get();

        Dialog cveDialog = new Dialog();
        cveDialog.initStyle(StageStyle.TRANSPARENT);
        cveDialog.initModality(Modality.WINDOW_MODAL);

        cveStage = (Stage) cveDialog.getDialogPane().getScene().getWindow();
        cveStage.setAlwaysOnTop(true);
        cveStage.getIcons().add(dukeStageIcon);
        cveStage.getScene().setFill(Color.TRANSPARENT);
        cveStage.getScene().getStylesheets().add(Main.class.getResource(cssFile).toExternalForm());

        cveCloseMacWindowButton = new MacosWindowButton(WindowButtonType.CLOSE, WindowButtonSize.NORMAL);
        cveCloseMacWindowButton.setDarkMode(isDarkMode);

        cveCloseWinWindowButton = new WinWindowButton(WindowButtonType.CLOSE, WindowButtonSize.SMALL);
        cveCloseWinWindowButton.setDarkMode(isDarkMode);

        cveWindowTitle = new Label("Vulnerabilities found");
        if (isWindows) {
            cveWindowTitle.setFont(Fonts.segoeUi(9));
            cveWindowTitle.setTextFill(isDarkMode ? Color.web("#969696") : Color.web("#000000"));
            cveWindowTitle.setAlignment(Pos.CENTER_LEFT);
            cveWindowTitle.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(darkMode.get() ? "duke.png" : "duke_blk.png"), 12, 12, true, false)));
            cveWindowTitle.setGraphicTextGap(10);

            AnchorPane.setTopAnchor(cveCloseWinWindowButton, 0d);
            AnchorPane.setRightAnchor(cveCloseWinWindowButton, 0d);
            AnchorPane.setTopAnchor(cveWindowTitle, 0d);
            AnchorPane.setRightAnchor(cveWindowTitle, 0d);
            AnchorPane.setBottomAnchor(cveWindowTitle, 0d);
            AnchorPane.setLeftAnchor(cveWindowTitle, 10d);
        } else {
            cveWindowTitle.setFont(Fonts.sfProTextMedium(12));
            cveWindowTitle.setTextFill(isDarkMode ? Color.web("#dddddd") : Color.web("#000000"));
            cveWindowTitle.setAlignment(Pos.CENTER);

            AnchorPane.setTopAnchor(cveCloseMacWindowButton, 7.125d);
            AnchorPane.setLeftAnchor(cveCloseMacWindowButton, 11d);
            AnchorPane.setTopAnchor(cveWindowTitle, 0d);
            AnchorPane.setRightAnchor(cveWindowTitle, 0d);
            AnchorPane.setBottomAnchor(cveWindowTitle, 0d);
            AnchorPane.setLeftAnchor(cveWindowTitle, 0d);
        }
        cveWindowTitle.setMouseTransparent(true);

        cveHeaderPane = new AnchorPane();
        cveHeaderPane.getStyleClass().add("header");
        cveHeaderPane.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.1), 1, 0.0, 0, 1));
        if (isWindows) {
            cveHeaderPane.setMinHeight(31);
            cveHeaderPane.setMaxHeight(31);
            cveHeaderPane.setPrefHeight(31);
            cveHeaderPane.getChildren().addAll(cveCloseWinWindowButton, cveWindowTitle);
        } else {
            cveHeaderPane.setMinHeight(26.25);
            cveHeaderPane.setMaxHeight(26.25);
            cveHeaderPane.setPrefHeight(26.25);
            cveHeaderPane.getChildren().addAll(cveCloseMacWindowButton, cveWindowTitle);
        }

        cveBox = new VBox(5);
        cveBox.setFillWidth(true);
        cveBox.getChildren().setAll(cveLinks);

        VBox cveVBox = new VBox(15, cveBox, cveCloseButton);
        cveVBox.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        cveVBox.setAlignment(Pos.TOP_CENTER);
        cveVBox.setAlignment(Pos.CENTER);

        cvePane.getChildren().add(cveVBox);
        cvePane.setPadding(new Insets(10));

        BorderPane cveMainPane = new BorderPane();
        cveMainPane.setCenter(cvePane);
        cveMainPane.setTop(cveHeaderPane);

        if (OperatingSystem.LINUX == operatingSystem && (Architecture.AARCH64 == architecture || Architecture.ARM64 == architecture)) {
            cveMainPane.setOnMousePressed(press -> cveMainPane.setOnMouseDragged(drag -> {
                cveDialog.setX(drag.getScreenX() - press.getSceneX());
                cveDialog.setY(drag.getScreenY() - press.getSceneY());
            }));
            cveDialog.getDialogPane().setContent(new StackPane(cveMainPane));
        } else {
            StackPane cveGlassPane = new StackPane(cveMainPane);
            cveGlassPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            cveGlassPane.setMinSize(300, isWindows ? 150 : 150);
            cveGlassPane.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.35), 10.0, 0.0, 0.0, 5));
            cveMainPane.setOnMousePressed(press -> cveMainPane.setOnMouseDragged(drag -> {
                cveDialog.setX(drag.getScreenX() - press.getSceneX());
                cveDialog.setY(drag.getScreenY() - press.getSceneY());
            }));
            cveDialog.getDialogPane().setContent(cveGlassPane);
        }

        cveDialog.getDialogPane().setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        // Adjustments related to dark/light mode
        if (OperatingSystem.MACOS == Detector.getOperatingSystem()) {
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
                cveWindowTitle.setTextFill(Color.web("#969696"));
                cveHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                cveHeaderPane.setBorder(new Border(new BorderStroke(Color.web("#f2f2f2"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0.5, 0))));
                cveVBox.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), CornerRadii.EMPTY, Insets.EMPTY)));
                cvePane.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), CornerRadii.EMPTY, Insets.EMPTY)));
                cveMainPane.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), CornerRadii.EMPTY, Insets.EMPTY)));
                cveMainPane.setBorder(new Border(new BorderStroke(Color.web("#515352"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 1, 1, 1))));
            } else {
                cveWindowTitle.setTextFill(Color.web("#dddddd"));
                cveHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#343535"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                cveVBox.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), CornerRadii.EMPTY, Insets.EMPTY)));
                cvePane.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(0, 0, 10, 10, false), Insets.EMPTY)));
                cveMainPane.setBackground(new Background(new BackgroundFill(Color.web("#1d1f20"), new CornerRadii(10), Insets.EMPTY)));
                cveMainPane.setBorder(new Border(new BorderStroke(Color.web("#515352"), BorderStrokeStyle.SOLID, new CornerRadii(10, 10, 10, 10, false), new BorderWidths(1))));
            }
        } else {
            if (isWindows) {
                cveWindowTitle.setTextFill(Color.web("#000000"));
                cveHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#efedec"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                cveHeaderPane.setBorder(new Border(new BorderStroke(Color.web("#f2f2f2"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0.5, 0))));
                cveVBox.setBackground(new Background(new BackgroundFill(Color.web("#e3e5e5"), CornerRadii.EMPTY, Insets.EMPTY)));
                cvePane.setBackground(new Background(new BackgroundFill(Color.web("#e3e5e5"), CornerRadii.EMPTY, Insets.EMPTY)));
                cveMainPane.setBackground(new Background(new BackgroundFill(Color.web("#ecebe9"), CornerRadii.EMPTY, Insets.EMPTY)));
                cveMainPane.setBorder(new Border(new BorderStroke(Color.web("#f6f4f4"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 1, 1, 1))));
            } else {
                cveWindowTitle.setTextFill(Color.web("#000000"));
                cveHeaderPane.setBackground(new Background(new BackgroundFill(Color.web("#edefef"), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
                cveVBox.setBackground(new Background(new BackgroundFill(Color.web("#e3e5e5"), CornerRadii.EMPTY, Insets.EMPTY)));
                cvePane.setBackground(new Background(new BackgroundFill(Color.web("#e3e5e5"), new CornerRadii(0, 0, 10, 10, false), Insets.EMPTY)));
                cveMainPane.setBackground(new Background(new BackgroundFill(Color.web("#ecebe9"), new CornerRadii(10), Insets.EMPTY)));
                cveMainPane.setBorder(new Border(new BorderStroke(Color.web("#f6f4f4"), BorderStrokeStyle.SOLID, new CornerRadii(10, 10, 10, 10, false), new BorderWidths(1))));
            }
        }
        return cveDialog;
    }

    // Download a JDK related
    private Dialog createDownloadJDKDialog() {
        final boolean isDarkMode = darkMode.get();

        Dialog downloadJDKDialog = new Dialog();
        downloadJDKDialog.initOwner(stage);
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

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        downloadAutoExtractLabel = new Label("(auto extract)");
        downloadAutoExtractLabel.setFont(isWindows ? Fonts.segoeUi(13) : Fonts.sfPro(13));
        downloadAutoExtractLabel.setVisible(false);
        HBox downloadJDKFxBox = new HBox(downloadJDKBundledWithFXCheckBox, spacer, downloadAutoExtractLabel);

        Label downloadJDKMajorVersionLabel = new Label("Major version");
        Region findlJDKMajorVersionSpacer = new Region();
        HBox.setHgrow(findlJDKMajorVersionSpacer, Priority.ALWAYS);
        downloadJDKMajorVersionComboBox = new ComboBox<>();
        downloadJDKMajorVersionComboBox.setCellFactory(majorVersionListView -> isWindows ? new MajorVersionCell() : new MacosMajorVersionCell());
        downloadJDKMajorVersionComboBox.setMinWidth(150);
        downloadJDKMajorVersionComboBox.setMaxWidth(150);
        downloadJDKMajorVersionComboBox.setPrefWidth(150);
        HBox downloadJDKMajorVersionBox = new HBox(5, downloadJDKMajorVersionLabel, findlJDKMajorVersionSpacer, downloadJDKMajorVersionComboBox);
        downloadJDKMajorVersionBox.setAlignment(Pos.CENTER);

        Label downloadJDKUpdateLevelLabel = new Label("Update level");
        Region downloadJDKUpdateLevelSpacer = new Region();
        HBox.setHgrow(downloadJDKUpdateLevelSpacer, Priority.ALWAYS);
        downloadJDKUpdateLevelComboBox = new ComboBox<>();
        downloadJDKUpdateLevelComboBox.setCellFactory(updateLevelListView -> isWindows ? new UpdateLevelCell() : new MacosUpdateLevelCell());
        downloadJDKUpdateLevelComboBox.setMinWidth(150);
        downloadJDKUpdateLevelComboBox.setMaxWidth(150);
        downloadJDKUpdateLevelComboBox.setPrefWidth(150);
        HBox downloadJDKUpdateLevelBox = new HBox(5, downloadJDKUpdateLevelLabel, downloadJDKUpdateLevelSpacer, downloadJDKUpdateLevelComboBox);
        downloadJDKUpdateLevelBox.setAlignment(Pos.CENTER);

        Label downloadJDKDistributionLabel = new Label("Distribution");
        Region downloadJDKDistributionSpacer = new Region();
        HBox.setHgrow(downloadJDKDistributionSpacer, Priority.ALWAYS);
        downloadJDKDistributionComboBox = new ComboBox<>();
        downloadJDKDistributionComboBox.setCellFactory(distributionListView -> isWindows ? new DistributionCell() : new MacosDistributionCell());
        downloadJDKDistributionComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(final Distribution distribution) { return null == distribution ? null : distribution.getUiString(); }
            @Override public Distribution fromString(final String text) {
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
        downloadJDKOperatingSystemComboBox.setCellFactory(operatingSystemListView -> isWindows ? new OperatingSystemCell() : new MacosOperatingSystemCell());
        downloadJDKOperatingSystemComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(final OperatingSystem operatingSystem) { return null == operatingSystem ? null : operatingSystem.getUiString(); }
            @Override public OperatingSystem fromString(final String text) { return OperatingSystem.fromText(text); }
        });
        downloadJDKOperatingSystemComboBox.setMinWidth(150);
        downloadJDKOperatingSystemComboBox.setMaxWidth(150);
        downloadJDKOperatingSystemComboBox.setPrefWidth(150);
        HBox downloadJDKOperatingSystemBox = new HBox(5, downloadJDKOperatingSystemLabel, findlJDKOperatingSystemSpacer, downloadJDKOperatingSystemComboBox);
        downloadJDKOperatingSystemBox.setAlignment(Pos.CENTER);

        Label downloadJDKArchitectureLabel = new Label("Architecture");
        Region downloadJDKArchitectureSpacer = new Region();
        HBox.setHgrow(downloadJDKArchitectureSpacer, Priority.ALWAYS);
        downloadJDKArchitectureComboBox = new ComboBox<>();
        downloadJDKArchitectureComboBox.setCellFactory(architectureListView -> isWindows ? new ArchitectureCell() : new MacosArchitectureCell());
        downloadJDKArchitectureComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(final Architecture architecture) { return null == architecture ? null : architecture.getUiString(); }
            @Override public Architecture fromString(final String text) { return Architecture.fromText(text); }
        });
        downloadJDKArchitectureComboBox.setMinWidth(150);
        downloadJDKArchitectureComboBox.setMaxWidth(150);
        downloadJDKArchitectureComboBox.setPrefWidth(150);
        HBox downloadJDKArchitectureBox = new HBox(5, downloadJDKArchitectureLabel, downloadJDKArchitectureSpacer, downloadJDKArchitectureComboBox);
        downloadJDKArchitectureBox.setAlignment(Pos.CENTER);

        Label downloadJDKArchiveTypeLabel = new Label("Archive type");
        Region downloadJDKArchiveTypeSpacer = new Region();
        HBox.setHgrow(downloadJDKArchiveTypeSpacer, Priority.ALWAYS);
        downloadJDKArchiveTypeComboBox = new ComboBox<>();
        downloadJDKArchiveTypeComboBox.setCellFactory(archiveTypeListView -> isWindows ? new ArchiveTypeCell() : new MacosArchiveTypeCell());
        downloadJDKArchiveTypeComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(final ArchiveType archiveType) { return null == archiveType ? null : archiveType.getUiString(); }
            @Override public ArchiveType fromString(final String text) { return ArchiveType.fromText(text); }
        });
        downloadJDKArchiveTypeComboBox.setMinWidth(150);
        downloadJDKArchiveTypeComboBox.setMaxWidth(150);
        downloadJDKArchiveTypeComboBox.setPrefWidth(150);
        HBox downloadJDKArchiveTypeBox = new HBox(5, downloadJDKArchiveTypeLabel, downloadJDKArchiveTypeSpacer, downloadJDKArchiveTypeComboBox);
        downloadJDKArchiveTypeBox.setAlignment(Pos.CENTER);

        downloadJDKFilenameLabel = new Label("-");
        downloadJDKFilenameLabel.getStyleClass().add("small-label");
        HBox.setMargin(downloadJDKFilenameLabel, new Insets(10, 0, 0, 0));
        HBox.setHgrow(downloadJDKFilenameLabel, Priority.ALWAYS);
        HBox downloadJDKFilenameBox = new HBox(downloadJDKFilenameLabel);
        downloadJDKFilenameBox.setAlignment(Pos.CENTER);

        alreadyDownloadedLabel = new Label("(already dowloaded)");
        alreadyDownloadedLabel.getStyleClass().add("small-label");
        alreadyDownloadedLabel.setVisible(false);
        HBox.setMargin(alreadyDownloadedLabel, new Insets(0, 0, 10, 0));
        HBox.setHgrow(alreadyDownloadedLabel, Priority.ALWAYS);
        HBox alreadyDownloadedBox = new HBox(alreadyDownloadedLabel);
        alreadyDownloadedBox.setAlignment(Pos.CENTER);

        downloadJDKCancelButton = new Button("Cancel");

        Region spacerLeft = new Region();
        HBox.setMargin(spacerLeft, new Insets(10, 0, 10, 0));
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);

        tckTestedLink = new Hyperlink();

        tckTestedTag = new ImageView(new Image(Main.class.getResourceAsStream("tck.png")));
        tckTestedTag.setPreserveRatio(true);
        tckTestedTag.setFitHeight(14);
        tckTestedTag.setVisible(false);
        tckTestedTag.setOnMousePressed(e -> {
            if (tckTestedLink.getText().isEmpty()) { return; }
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(tckTestedLink.getText()));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        Tooltip.install(tckTestedTag, new Tooltip("Package is TCK tested"));

        Region spacerCenter = new Region();
        HBox.setMargin(spacerCenter, new Insets(10, 0, 10, 0));
        HBox.setHgrow(spacerCenter, Priority.ALWAYS);

        aqavitTestedLink = new Hyperlink();

        aqavitTestedTag = new ImageView(new Image(Main.class.getResourceAsStream("aqavit.png")));
        aqavitTestedTag.setPreserveRatio(true);
        aqavitTestedTag.setFitHeight(14);
        aqavitTestedTag.setVisible(false);
        aqavitTestedTag.setOnMousePressed(e -> {
            if (aqavitTestedLink.getText().isEmpty()) { return; }
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(aqavitTestedLink.getText()));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        Tooltip.install(aqavitTestedTag, new Tooltip("Package is AQAvit certified"));

        Region spacerRight = new Region();
        HBox.setMargin(spacerRight, new Insets(10, 0, 10, 0));
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        downloadJDKDownloadButton = new Button("Download");
        downloadJDKDownloadButton.setDisable(true);

        HBox downloadJDKButtonBox = new HBox(5, downloadJDKCancelButton, spacerLeft, tckTestedTag, spacerCenter, aqavitTestedTag, spacerRight, downloadJDKDownloadButton);
        downloadJDKButtonBox.setAlignment(Pos.CENTER);

        downloadJDKCloseMacWindowButton = new MacosWindowButton(WindowButtonType.CLOSE, WindowButtonSize.NORMAL);
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

            AnchorPane.setTopAnchor(downloadJDKCloseMacWindowButton, 7.125d);
            AnchorPane.setLeftAnchor(downloadJDKCloseMacWindowButton, 11d);
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
            downloadJDKHeaderPane.setMinHeight(26.25);
            downloadJDKHeaderPane.setMaxHeight(26.25);
            downloadJDKHeaderPane.setPrefHeight(26.25);
            downloadJDKHeaderPane.getChildren().addAll(downloadJDKCloseMacWindowButton, downloadJDKWindowTitle);
        }

        downloadJDKProgressBar = new ProgressBar(0);
        downloadJDKProgressBar.setVisible(false);
        VBox.setMargin(downloadJDKProgressBar, new Insets(0, 0, 5, 0));

        VBox downloadJDKVBox = new VBox(10, downloadJDKFxBox, downloadJDKMajorVersionBox, downloadJDKUpdateLevelBox, downloadJDKDistributionBox, downloadJDKOperatingSystemBox,
                                        downloadJDKArchitectureBox, downloadJDKArchiveTypeBox, downloadJDKFilenameBox, alreadyDownloadedBox, downloadJDKProgressBar, downloadJDKButtonBox);
        downloadJDKVBox.setAlignment(Pos.CENTER);

        downloadJDKPane.getChildren().add(downloadJDKVBox);
        downloadJDKPane.getStyleClass().add("jdk-mon");
        downloadJDKPane.setPadding(new Insets(10));

        BorderPane downloadJDKMainPane = new BorderPane();
        downloadJDKMainPane.setTop(downloadJDKHeaderPane);
        downloadJDKMainPane.setCenter(downloadJDKPane);

        downloadJDKProgressBar.prefWidthProperty().bind(downloadJDKMainPane.widthProperty());

        if (OperatingSystem.LINUX == operatingSystem && (Architecture.AARCH64 == architecture || Architecture.ARM64 == architecture)) {
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
        if (OperatingSystem.MACOS == Detector.getOperatingSystem()) {
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
                downloadJDKArchitectureLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKArchiveTypeLabel.setTextFill(Color.web("#2a2a2a"));
                downloadJDKFilenameLabel.setTextFill(Color.web("#2a2a2a"));
            }
        }

        return downloadJDKDialog;
    }

    private void selectMajorVersion() {
        if (null == downloadJDKSelectedMajorVersion) { return; }
        downloadJDKSelectedMajorVersion = downloadJDKMajorVersionComboBox.getSelectionModel().getSelectedItem();
        final boolean include_build = downloadJDKSelectedMajorVersion.isEarlyAccessOnly();
        downloadJDKBundledWithFXCheckBox.setDisable(true);
        if (downloadJDKBundledWithFXCheckBox.isSelected()) {
            downloadJDKUpdateLevelComboBox.getItems().clear();
            downloadJDKDistributionComboBox.getItems().clear();
            downloadJDKOperatingSystemComboBox.getItems().clear();
            downloadJDKArchitectureComboBox.getItems().clear();
            downloadJDKArchiveTypeComboBox.getItems().clear();
        }

        List<MinimizedPkg> pkgs;
        if (downloadJDKJavafxBundled) {
            pkgs = downloadJDKMinimizedPkgs.stream()
                                           .filter(pkg -> pkg.isJavaFXBundled())
                                           .filter(pkg -> pkg.getMajorVersion().getAsInt() == downloadJDKSelectedMajorVersion.getAsInt())
                                           .filter(pkg -> pkg.getReleaseStatus() == (include_build ? EA : GA))
                                           .filter(pkg -> pkg.isDirectlyDownloadable())
                                           .collect(Collectors.toList());
        } else {
            pkgs = downloadJDKMinimizedPkgs.stream()
                                           .filter(pkg -> pkg.getMajorVersion().getAsInt() == downloadJDKSelectedMajorVersion.getAsInt())
                                           .filter(pkg -> pkg.getReleaseStatus() == (include_build ? EA : GA))
                                           .filter(pkg -> pkg.isDirectlyDownloadable())
                                           .collect(Collectors.toList());
        }

        downloadJDKSelectedPkgsForMajorVersion.clear();
        downloadJDKSelectedPkgsForMajorVersion.addAll(pkgs);

        Platform.runLater(() -> downloadJDKBundledWithFXCheckBox.setDisable(false));
        List<Semver> versionList = downloadJDKSelectedMajorVersion.getVersions()
                                                                  .stream()
                                                                  .filter(semver -> downloadJDKSelectedMajorVersion.isEarlyAccessOnly() ? (semver.getReleaseStatus() == EA) : (semver.getReleaseStatus() == GA))
                                                                  .sorted(Comparator.comparing(Semver::getVersionNumber).reversed())
                                                                  .map(semVer -> semVer.getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build))
                                                                  .distinct().map(versionString -> Semver.fromText(versionString).getSemver1())
                                                                  .collect(Collectors.toList());
        Platform.runLater(() -> {
            downloadJDKUpdateLevelComboBox.getItems().setAll(versionList);
            if (versionList.size() > 0) {
                downloadJDKUpdateLevelComboBox.getSelectionModel().select(0);
            }
        });
    }

    private void selectVersionNumber() {
        downloadJDKSelectedVersionNumber = downloadJDKUpdateLevelComboBox.getSelectionModel().getSelectedItem();

        List<io.foojay.api.discoclient.pkg.Distribution> distrosForSelection;

        boolean include_build = downloadJDKSelectedMajorVersion.isEarlyAccessOnly();
        if (downloadJDKJavafxBundled) {
            distrosForSelection = downloadJDKSelectedPkgsForMajorVersion.stream()
                                                                        .filter(pkg -> pkg.isJavaFXBundled())
                                                                        .filter(pkg -> pkg.getJavaVersion().getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build).equals(downloadJDKSelectedVersionNumber.getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build)))
                                                                        .map(pkg -> pkg.getDistribution())
                                                                        .distinct()
                                                                        .sorted(Comparator.comparing(io.foojay.api.discoclient.pkg.Distribution::getName).reversed())
                                                                        .collect(Collectors.toList());
        } else {
            distrosForSelection = downloadJDKSelectedPkgsForMajorVersion.stream()
                                                                        .filter(pkg -> pkg.getJavaVersion().getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build).equals(downloadJDKSelectedVersionNumber.getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build)))
                                                                        .map(pkg -> pkg.getDistribution())
                                                                        .distinct()
                                                                        .sorted(Comparator.comparing(io.foojay.api.discoclient.pkg.Distribution::getName).reversed())
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
        downloadJDKArchiveTypes.clear();

        boolean include_build = downloadJDKSelectedMajorVersion.isEarlyAccessOnly();

        if (downloadJDKJavafxBundled) {
            downloadJDKSelectedPkgs.addAll(downloadJDKMinimizedPkgs.stream()
                                                                   .filter(pkg -> pkg.isJavaFXBundled())
                                                                   .filter(pkg -> pkg.getDistribution().getApiString().equals(downloadJDKSelectedDistribution.getApiString()))
                                                                   .filter(pkg -> pkg.getJavaVersion().getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build).equals(downloadJDKSelectedVersionNumber.getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build)))
                                                                   .filter(pkg -> pkg.getPackageType() == PackageType.JDK)
                                                                   .filter(pkg -> pkg.isDirectlyDownloadable())
                                                                   .collect(Collectors.toList()));
        } else {
            downloadJDKSelectedPkgs.addAll(downloadJDKMinimizedPkgs.stream()
                                                                   .filter(pkg -> pkg.getDistribution().getApiString().equals(downloadJDKSelectedDistribution.getApiString()))
                                                                   .filter(pkg -> pkg.getJavaVersion().getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build).equals(downloadJDKSelectedVersionNumber.getVersionNumber().toString(OutputFormat.REDUCED_COMPRESSED, true, include_build)))
                                                                   .filter(pkg -> pkg.getPackageType() == PackageType.JDK)
                                                                   .filter(pkg -> pkg.isDirectlyDownloadable())
                                                                   .collect(Collectors.toList()));
        }
        downloadJDKSelectedPkgs.forEach(pkg -> {
            downloadJDKOperatingSystems.add(pkg.getOperatingSystem());
            downloadJDKArchitectures.add(pkg.getArchitecture());
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
        List<MinimizedPkg> selection;
        if (downloadJDKJavafxBundled) {
            selection = downloadJDKSelectedPkgs.stream()
                                               .filter(pkg -> pkg.isJavaFXBundled())
                                               .filter(pkg -> downloadJDKSelectedDistribution.getApiString().equals(pkg.getDistribution().getApiString()))
                                               .filter(pkg -> downloadJDKSelectedOperatingSystem == pkg.getOperatingSystem())
                                               .collect(Collectors.toList());
        } else {
            selection = downloadJDKSelectedPkgs.stream()
                                               .filter(pkg -> downloadJDKSelectedDistribution.getApiString().equals(pkg.getDistribution().getApiString()))
                                               .filter(pkg -> downloadJDKSelectedOperatingSystem == pkg.getOperatingSystem())
                                               .collect(Collectors.toList());
        }

        downloadJDKArchitectures = selection.stream().map(pkg -> pkg.getArchitecture()).collect(Collectors.toSet());

        Platform.runLater(() -> {
            downloadJDKArchitectureComboBox.getItems().clear();
            downloadJDKArchitectures.forEach(architecture -> downloadJDKArchitectureComboBox.getItems().add(architecture));
            downloadJDKArchitectureComboBox.getItems().setAll(downloadJDKArchitectures);
            int selectIndex = -1;
            for (int i = 0; i < downloadJDKArchitectureComboBox.getItems().size() ; i++) {
                if (downloadJDKArchitectureComboBox.getItems().get(i) == architecture) {
                    selectIndex = i;
                    break;
                }
            }
            if (-1 == selectIndex) {
                if (downloadJDKArchitectures.size() > 0) {
                    downloadJDKArchitectureComboBox.getSelectionModel().select(0);
                }
            } else {
                downloadJDKArchitectureComboBox.getSelectionModel().select(selectIndex);
            }
        });
    }

    private void selectArchitecture() {
        downloadJDKSelectedArchitecture = downloadJDKArchitectureComboBox.getSelectionModel().getSelectedItem();
        List<MinimizedPkg> selection = downloadJDKSelectedPkgs.stream()
                                                              .filter(pkg -> pkg.isJavaFXBundled() == downloadJDKJavafxBundled)
                                                              .filter(pkg -> downloadJDKSelectedDistribution.getApiString().equals(pkg.getDistribution().getApiString()))
                                                              .filter(pkg -> downloadJDKSelectedOperatingSystem == pkg.getOperatingSystem())
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
        List<MinimizedPkg> selection = downloadJDKSelectedPkgs.stream()
                                                              .filter(pkg -> pkg.isJavaFXBundled() == downloadJDKJavafxBundled)
                                                              .filter(pkg -> downloadJDKSelectedDistribution.getApiString().equals(pkg.getDistribution().getApiString()))
                                                              .filter(pkg -> downloadJDKSelectedOperatingSystem == pkg.getOperatingSystem())
                                                              .filter(pkg -> downloadJDKSelectedArchitecture == pkg.getArchitecture())
                                                              .filter(pkg -> downloadJDKSelectedArchiveType == pkg.getArchiveType())
                                                              .collect(Collectors.toList());
        if (selection.size() > 0) {
            downloadJDKSelectedPkg = selection.get(0);

            final File    downloadFolder;
            final boolean alreadyDownloaded;
            if (PropertyManager.INSTANCE.getBoolean(PropertyManager.REMEMBER_DOWNLOAD_FOLDER)) {
                if (!PropertyManager.INSTANCE.getString(PropertyManager.DOWNLOAD_FOLDER).isEmpty()) {
                    File folder = new File(PropertyManager.INSTANCE.getString(PropertyManager.DOWNLOAD_FOLDER));
                    if (folder.isDirectory()) {
                        downloadFolder = folder;
                        alreadyDownloaded = new File(downloadFolder.getAbsolutePath() + File.separator + downloadJDKSelectedPkg.getFilename()).exists();
                    } else {
                        alreadyDownloaded = false;
                    }
                } else {
                    alreadyDownloaded = false;
                }
            } else {
                alreadyDownloaded = false;
            }

            Platform.runLater(() -> {
                alreadyDownloadedLabel.setVisible(alreadyDownloaded);
                downloadJDKFilenameLabel.setText(null == downloadJDKSelectedPkg ? "-" : downloadJDKSelectedPkg.getFilename());
                tckTestedTag.setVisible(Verification.YES == downloadJDKSelectedPkg.getTckTested());
                tckTestedLink.setText(Verification.YES == downloadJDKSelectedPkg.getTckTested() ? downloadJDKSelectedPkg.getTckCertUri() : "");
                aqavitTestedTag.setVisible(Verification.YES == downloadJDKSelectedPkg.getAqavitCertified());
                aqavitTestedLink.setText(Verification.YES == downloadJDKSelectedPkg.getAqavitCertified() ? downloadJDKSelectedPkg.getAqavitCertUri() : "");
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

    private void downloadPkgDownloadJDK(final MinimizedPkg pkg) {
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
            final boolean alreadyDownloaded = new File(downloadFolder + File.separator + pkg.getFilename()).exists();
            final String  directDownloadUri = discoclient.getPkgDirectDownloadUri(pkg.getId());
            if (null == directDownloadUri) {
                new Alert(AlertType.ERROR, "Problem downloading the package, please try again.", ButtonType.CLOSE).show();
                return;
            }
            final String target = downloadFolder.getAbsolutePath() + File.separator + pkg.getFilename();
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

                    if (downloadAutoExtractLabel.isVisible()) {
                        switch(pkg.getArchiveType()) {
                            case TAR_GZ -> Helper.untar(target, downloadFolder.getAbsolutePath());
                            case ZIP    -> Helper.unzip(target, downloadFolder.getAbsolutePath());
                        }
                    }

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

            if (alreadyDownloaded) {
                openFileLocation(new File(downloadFolder.getAbsolutePath()));
            } else if (PropertyManager.INSTANCE.getBoolean(PropertyManager.REMEMBER_DOWNLOAD_FOLDER)) {
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
