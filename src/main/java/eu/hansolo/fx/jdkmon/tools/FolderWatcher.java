/*
 * Copyright (c) 2025 by Gerrit Grunwald
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

import eu.hansolo.toolbox.properties.BooleanProperty;
import eu.hansolo.toolbox.properties.ReadOnlyBooleanProperty;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;


public class FolderWatcher {
    private FileSystem      fileSystem;
    private WatchService    watchService;
    private Path            pathToWatch;
    private AtomicBoolean   running        = new AtomicBoolean(false);
    private BooleanProperty changeDetected = new BooleanProperty(false);
    private boolean         valid          = true;


    public FolderWatcher(final String folderName) {
        try {
            final File file = new File(folderName);
            this.valid = file.exists() && file.isDirectory();
            if (valid) {
                fileSystem   = FileSystems.getDefault();
                watchService = fileSystem.newWatchService();
                pathToWatch  = Paths.get(folderName);
                pathToWatch.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            }
        } catch (final Exception e) {
            throw new RuntimeException("Error initializing FolderWatcher", e);
        }
    }

    public void startWatching() throws Exception {
        if (running.get() || !valid) { return; }
        this.running.set(true);
        while (this.running.get()) {
            final WatchKey key = watchService.take();
            key.pollEvents().forEach(evt -> {
                //Object context = evt.context();
                //System.out.printf("%s %d %s\n", evt.kind(), evt.count(), context);
                this.changeDetected.set(!this.changeDetected.get());
            });
            key.reset();
        }
    }

    public void stopWatching() {
        this.running.set(false);
    }

    public ReadOnlyBooleanProperty changeDetectedProperty() { return this.changeDetected; }

    public boolean isWatching() { return this.running.get(); }
}
