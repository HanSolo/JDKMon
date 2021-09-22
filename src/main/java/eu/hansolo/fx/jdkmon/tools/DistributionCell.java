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

package eu.hansolo.fx.jdkmon.tools;

import io.foojay.api.discoclient.pkg.Distribution;
import javafx.scene.control.ListCell;


public class DistributionCell extends ListCell<Distribution> {

    @Override protected void updateItem(final Distribution distribution, final boolean empty) {
        super.updateItem(distribution, empty);

        if (empty || null == distribution) {
            setText(null);
            setGraphic(null);
        } else {
            setText(distribution.getUiString());
        }
    }

    @Override public String toString() {
        return getItem().getUiString();
    }
}
