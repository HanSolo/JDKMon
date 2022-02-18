/*
 * Copyright (c) 2022 by Gerrit Grunwald
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

import eu.hansolo.fx.jdkmon.controls.MacosComboBoxCell;
import io.foojay.api.discoclient.pkg.MajorVersion;


public class MacosMajorVersionCell extends MacosComboBoxCell<MajorVersion> {

    @Override protected void updateItem(final MajorVersion majorVersion, final boolean empty) {
        super.updateItem(majorVersion, empty);

        if (empty || null == majorVersion) {
            setText(null);
            setGraphic(null);
        } else {
            setText(majorVersion.toString());
            setGraphic(isSelected() ? (isDark() ? WHITE_CHECK_MARK : BLACK_CHECK_MARK) : EMPTY_CHECK_MARK);
        }
    }

    @Override public String toString() {
        return Integer.toString(getItem().getAsInt());
    }
}

