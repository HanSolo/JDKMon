/*
 * Copyright (c) 2023 by Gerrit Grunwald
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

import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;


public class SearchTextFieldSkin extends TextFieldSkin {
    private Region  searchIcon;
    private Text    searchText;
    private Region  closeIcon;


    public SearchTextFieldSkin(final SearchTextField control){
        super(control);

        initGraphics();
        registerListeners();
    }


    private void initGraphics() {
        searchIcon = new Region();
        searchIcon.getStyleClass().addAll("search-icon");
        searchIcon.setFocusTraversable(false);

        searchText = new Text("Search");
        searchText.getStyleClass().addAll("search-text");
        searchText.setFocusTraversable(false);

        closeIcon = new Region();
        closeIcon.getStyleClass().addAll("close-icon");
        closeIcon.setFocusTraversable(false);
        closeIcon.setVisible(false);

        getChildren().addAll(searchIcon, searchText, closeIcon);
    }

    private void registerListeners() {
        closeIcon.setOnMouseClicked(event -> getSkinnable().setText(""));
        getSkinnable().textProperty().addListener(o -> {
            closeIcon.setVisible(getSkinnable().getText().isEmpty() ? false : true);
        });

        getSkinnable().focusedProperty().addListener(o -> {
            searchIcon.setVisible(!getSkinnable().isFocused() && getSkinnable().getText().isEmpty());
            searchText.setVisible(!getSkinnable().isFocused() && getSkinnable().getText().isEmpty());
            closeIcon.setVisible(getSkinnable().isFocused() && !getSkinnable().getText().isEmpty() ? true : false);
        });

        getSkinnable().widthProperty().addListener(o -> {
            final double size = searchIcon.getMaxWidth() < 0 ? 20.8 : searchIcon.getWidth();
            searchIcon.setTranslateX(-getSkinnable().getWidth() * 0.5 + size * 0.7);
            searchText.setTranslateX(-getSkinnable().getWidth() * 0.5 + size * 2.25);
            closeIcon.setTranslateX(getSkinnable().getWidth() * 0.5 - size * 0.7);
        });

        getSkinnable().heightProperty().addListener(observable -> {
            closeIcon.setMaxSize(getSkinnable().getHeight() * 0.8, getSkinnable().getHeight() * 0.8);
            searchIcon.setMaxSize(getSkinnable().getHeight() * 0.8, getSkinnable().getHeight() * 0.8);
        });

        getSkinnable().sceneProperty().addListener(observable -> {
            searchIcon.setTranslateX(-getSkinnable().getWidth() * 0.5 + closeIcon.getWidth() * 0.7);
            searchText.setTranslateX(-getSkinnable().getWidth() * 0.5 + closeIcon.getWidth() * 2.25);
            closeIcon.setTranslateX(getSkinnable().getWidth() * 0.5 - searchIcon.getWidth() * 0.7);
        });
    }
}
