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

import eu.hansolo.fx.jdkmon.tools.Helper;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.util.Duration;


public class WinProgress extends Region {
    private static final double                                PREFERRED_WIDTH        = 16;
    private static final double                                PREFERRED_HEIGHT       = 16;
    private static final double                                MINIMUM_WIDTH          = 5;
    private static final double                                MINIMUM_HEIGHT         = 5;
    private static final double                                MAXIMUM_WIDTH          = 1024;
    private static final double                                MAXIMUM_HEIGHT         = 1024;
    private static final StyleablePropertyFactory<WinProgress> FACTORY                = new StyleablePropertyFactory<>(Region.getClassCssMetaData());
    private static final PseudoClass                           DARK_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("dark");
    private static final CssMetaData<WinProgress, Color>       PROGRESS_STROKE        = FACTORY.createColorCssMetaData("-progress-stroke", s -> s.progressStroke, Color.BLACK, false);
    private static final CssMetaData<WinProgress, Color>       PROGRESS_FILL          = FACTORY.createColorCssMetaData("-progress-fill", s -> s.progressFill, Color.BLACK, false);
    private final        BooleanProperty                       darkMode;
    private final        StyleableProperty<Color>              progressStroke;
    private final        StyleableProperty<Color>              progressFill;
    private              boolean                               _indeterminate;
    private              BooleanProperty                       indeterminate;
    private              String                                userAgentStyleSheet;
    private              double                                size;
    private              double                                width;
    private              double                                height;
    private              Canvas                                canvas;
    private              GraphicsContext                       ctx;
    private              Pane                                  pane;
    private              double                                lineWidth;
    private              double                                halfLineWidth;
    private              double                                ballSize;
    private              double                                _progress;
    private              DoubleProperty                        progress;
    private              long                                  lastDelta;
    private              long                                  lastTimerCall;
    private              int                                   numberOfBalls;
    private              long                                  lastDecrease;
    private              boolean                               decreasing;
    private              AnimationTimer                        timer;
    private              DoubleProperty                        ball0Angle;
    private              DoubleProperty                        ball1Angle;
    private              DoubleProperty                        ball2Angle;
    private              DoubleProperty                        ball3Angle;
    private              DoubleProperty                        ball4Angle;
    private              Timeline                              timeline;



    // ******************** Constructors **************************************
    public WinProgress() {
        this(0);
    }
    public WinProgress(final double progress) {
        this.darkMode          = new BooleanPropertyBase(false) {
            @Override protected void invalidated() { pseudoClassStateChanged(DARK_MODE_PSEUDO_CLASS, get()); }
            @Override public Object getBean() { return WinProgress.this; }
            @Override public String getName() { return "darkMode"; }
        };
        this.progressStroke    = new StyleableObjectProperty<>(PROGRESS_STROKE.getInitialValue(WinProgress.this)) {
            @Override protected void invalidated() { redraw(); }
            @Override public Object getBean() { return WinProgress.this; }
            @Override public String getName() { return "progressStroke"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return PROGRESS_STROKE; }
        };
        this.progressFill      = new StyleableObjectProperty<>(PROGRESS_FILL.getInitialValue(WinProgress.this)) {
            @Override protected void invalidated() { redraw(); }
            @Override public Object getBean() { return WinProgress.this; }
            @Override public String getName() { return "progressFill"; }
            @Override public CssMetaData<? extends Styleable, Color> getCssMetaData() { return PROGRESS_FILL; }
        };
        this._indeterminate    = false;
        this._progress         = Helper.clamp(0.0, 1.0, progress);
        this.lastDelta         = System.nanoTime();
        this.lastTimerCall     = lastDelta;
        this.lastDecrease      = System.nanoTime();
        this.decreasing        = false;
        this.numberOfBalls     = 1;
        this.timer             = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastDecrease + 4_400_000_000l) {
                    decreasing = true;
                    lastDecrease = now;
                }
                if (now > lastTimerCall + 16_66_000l) {
                    if (numberOfBalls < 5) {
                        if (now > lastDelta + 190_000_000L) {
                            numberOfBalls++;
                            lastDelta = now;
                        }
                    }
                    redraw();
                    lastTimerCall = now;
                }
            }
        };
        this.ball0Angle        = new SimpleDoubleProperty(0);
        this.ball1Angle        = new SimpleDoubleProperty(-10);
        this.ball2Angle        = new SimpleDoubleProperty(-20);
        this.ball3Angle        = new SimpleDoubleProperty(-30);
        this.ball4Angle        = new SimpleDoubleProperty(-40);
        this.timeline          = new Timeline();
        this.lineWidth         = 1;
        this.halfLineWidth     = 0.5;
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
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        getStyleClass().add("progress");

        canvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctx    = canvas.getGraphicsContext2D();

        pane = new Pane(canvas);

        KeyValue kvBall0_0 = new KeyValue(ball0Angle, 0);
        KeyValue kvBall0_1 = new KeyValue(ball0Angle, 225);
        KeyValue kvBall0_2 = new KeyValue(ball0Angle, 360);
        KeyFrame kfBall0_0 = new KeyFrame(Duration.ZERO, kvBall0_0);
        KeyFrame kfBall0_1 = new KeyFrame(Duration.millis(190), kvBall0_1);
        KeyFrame kfBall0_2 = new KeyFrame(Duration.millis(2200), kvBall0_2);
        timeline.getKeyFrames().addAll(kfBall0_0, kfBall0_1, kfBall0_2);

        KeyValue kvBall1_0 = new KeyValue(ball1Angle, -10);
        KeyValue kvBall1_1 = new KeyValue(ball1Angle, 0);
        KeyValue kvBall1_2 = new KeyValue(ball1Angle, 215);
        KeyValue kvBall1_3 = new KeyValue(ball1Angle, 350);
        KeyFrame kfBall1_0 = new KeyFrame(Duration.ZERO, kvBall1_0);
        KeyFrame kfBall1_1 = new KeyFrame(Duration.millis(100), kvBall1_1);
        KeyFrame kfBall1_2 = new KeyFrame(Duration.millis(400), kvBall1_2);
        KeyFrame kfBall1_3 = new KeyFrame(Duration.millis(2200), kvBall1_3);
        timeline.getKeyFrames().addAll(kfBall1_0, kfBall1_1, kfBall1_2, kfBall1_3);

        KeyValue kvBall2_0 = new KeyValue(ball2Angle, -20);
        KeyValue kvBall2_1 = new KeyValue(ball2Angle, 0);
        KeyValue kvBall2_2 = new KeyValue(ball2Angle, 205);
        KeyValue kvBall2_3 = new KeyValue(ball2Angle, 340);
        KeyFrame kfBall2_0 = new KeyFrame(Duration.ZERO, kvBall2_0);
        KeyFrame kfBall2_1 = new KeyFrame(Duration.millis(200), kvBall2_1);
        KeyFrame kfBall2_2 = new KeyFrame(Duration.millis(500), kvBall2_2);
        KeyFrame kfBall2_3 = new KeyFrame(Duration.millis(2200), kvBall2_3);
        timeline.getKeyFrames().addAll(kfBall2_0, kfBall2_1, kfBall2_2, kfBall2_3);

        KeyValue kvBall3_0 = new KeyValue(ball3Angle, -30);
        KeyValue kvBall3_1 = new KeyValue(ball3Angle, 0);
        KeyValue kvBall3_2 = new KeyValue(ball3Angle, 195);
        KeyValue kvBall3_3 = new KeyValue(ball3Angle, 330);
        KeyFrame kfBall3_0 = new KeyFrame(Duration.ZERO, kvBall3_0);
        KeyFrame kfBall3_1 = new KeyFrame(Duration.millis(300), kvBall3_1);
        KeyFrame kfBall3_2 = new KeyFrame(Duration.millis(600), kvBall3_2);
        KeyFrame kfBall3_3 = new KeyFrame(Duration.millis(2200), kvBall3_3);
        timeline.getKeyFrames().addAll(kfBall3_0, kfBall3_1, kfBall3_2, kfBall3_3);

        KeyValue kvBall4_0 = new KeyValue(ball4Angle, -40);
        KeyValue kvBall4_1 = new KeyValue(ball4Angle, 0);
        KeyValue kvBall4_2 = new KeyValue(ball4Angle, 185);
        KeyValue kvBall4_3 = new KeyValue(ball4Angle, 320);
        KeyFrame kfBall4_0 = new KeyFrame(Duration.ZERO, kvBall4_0);
        KeyFrame kfBall4_1 = new KeyFrame(Duration.millis(400), kvBall4_1);
        KeyFrame kfBall4_2 = new KeyFrame(Duration.millis(700), kvBall4_2);
        KeyFrame kfBall4_3 = new KeyFrame(Duration.millis(2200), kvBall4_3);
        timeline.getKeyFrames().addAll(kfBall4_0, kfBall4_1, kfBall4_2, kfBall4_3);

        timeline.setCycleCount(-1);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        // add listeners to your propertes like
        //value.addListener(o -> handleControlPropertyChanged("VALUE"));
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double height) { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double width) { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double height) { return super.computePrefWidth(height); }
    @Override protected double computePrefHeight(final double width) { return super.computePrefHeight(width); }
    @Override protected double computeMaxWidth(final double height) { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double width) { return MAXIMUM_HEIGHT; }

    public boolean isDarkMode() { return darkMode.get(); }
    public void setDarkMode(final boolean darkMode) { this.darkMode.set(darkMode); }
    public BooleanProperty darkModeProperty() { return darkMode; }

    public Color getProgressStroke() { return progressStroke.getValue(); }
    public void setProgressStroke(final Color color) { progressStroke.setValue(color); }
    public ObjectProperty<Color> progressStrokeProperty() { return (ObjectProperty<Color>) progressStroke; }

    public Color getProgressFill() { return progressFill.getValue(); }
    public void setProgressFill(final Color color) { progressFill.setValue(color); }
    public ObjectProperty<Color> progressFillProperty() { return (ObjectProperty<Color>) progressFill; }

    public double getProgress() { return null == progress ? _progress : progress.get(); }
    public void setProgress(final double progress) {
        if (null == this.progress) {
            _progress = Helper.clamp(0.0, 1.0, progress);
            redraw();
        } else {
            this.progress.set(Helper.clamp(0.0, 1.0, progress));
        }
    }
    public DoubleProperty progressProperty() {
        if (null == progress) {
            progress = new DoublePropertyBase(_progress) {
                @Override protected void invalidated() {
                    if (isBound()) {
                        if (get() > 1 || get() < 0) { throw new IllegalArgumentException("Value cannot be smaller than 0 or larger than 1"); }
                    } else {
                        set(Helper.clamp(0.0, 1.0, get()));
                    }
                    redraw();
                }
                @Override public Object getBean() { return WinProgress.this; }
                @Override public String getName() { return "progress"; }
            };
        }
        return progress;
    }

    public boolean isIndeterminate() { return null == indeterminate ? _indeterminate : indeterminate.get(); }
    public void setIndeterminate(final boolean indeterminate) {
        if (indeterminate) {
            timer.start();
            timeline.play();
        } else {
            timer.stop();
            timeline.stop();
        }
        if (null == this.indeterminate) {
            _indeterminate = indeterminate;
            redraw();
        } else {
            this.indeterminate.set(indeterminate);
        }
    }
    public BooleanProperty indeterminateProperty() {
        if (null == indeterminate) {
            indeterminate = new BooleanPropertyBase(_indeterminate) {
                @Override protected void invalidated() { redraw(); }
                @Override public Object getBean() { return WinProgress.this; }
                @Override public String getName() { return "indeterminate"; }
            };
        }
        return indeterminate;
    }


    // ******************** Layout *******************************************
    @Override public String getUserAgentStylesheet() {
        if (null == userAgentStyleSheet) { userAgentStyleSheet = WinProgress.class.getResource("win-progress.css").toExternalForm(); }
        return userAgentStyleSheet;
    }

    private void resize() {
        width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            canvas.setWidth(width);
            canvas.setHeight(height);

            lineWidth     = Helper.clamp(1, 10, size * 0.0625);
            halfLineWidth = lineWidth * 0.5;
            ballSize      = Helper.clamp(2, 5, size * 0.125);

            if (!isIndeterminate()) { redraw(); }
        }
    }

    private void redraw() {
        ctx.clearRect(0, 0, width, height);
        if (isIndeterminate()) {
            ctx.setFill(isDarkMode() ? Color.WHITE : Color.BLACK);
            for (int i = 0 ; i < numberOfBalls ; i++) {
                ctx.save();
                ctx.translate(size * 0.5, size * 0.5);
                switch(i) {
                    case 0 -> ctx.rotate(ball0Angle.get());
                    case 1 -> ctx.rotate(ball1Angle.get());
                    case 2 -> ctx.rotate(ball2Angle.get());
                    case 3 -> ctx.rotate(ball3Angle.get());
                    case 4 -> ctx.rotate(ball4Angle.get());
                }
                ctx.fillOval(-ballSize * 0.5, -size * 0.5, ballSize, ballSize);
                ctx.translate(-size * 0.5, -size * 0.5);
                ctx.restore();
            }
        } else {
            ctx.setFill(getProgressFill());
            ctx.setStroke(getProgressStroke());
            ctx.setLineWidth(lineWidth);
            ctx.strokeOval(halfLineWidth, halfLineWidth, size - lineWidth, size - lineWidth);
            ctx.fillArc(halfLineWidth, halfLineWidth, size - lineWidth, size - lineWidth, 90, -360.0 * getProgress(), ArcType.ROUND);
        }
    }
}
