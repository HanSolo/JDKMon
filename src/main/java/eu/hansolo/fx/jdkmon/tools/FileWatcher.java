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

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;


public class FileWatcher implements Runnable {
    protected static final List<WatchService> watchServices = new ArrayList<>();
    protected       List<FileObserver>        observers = Collections.synchronizedList(new ArrayList<>());
    protected       File                      folder;
    
    
    public FileWatcher(final File folder) {
        this.folder = folder;
    }
    
    public void watch() {
        if (folder.exists()) {
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }
    }


    @Override public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(folder.getAbsolutePath());
            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            watchServices.add(watchService);
            boolean poll = true;
            while (poll) {
                poll = pollEvents(watchService);
            }
        } catch (IOException | InterruptedException | ClosedWatchServiceException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    protected boolean pollEvents(final WatchService watchService) throws InterruptedException {
        WatchKey key  = watchService.take();
        Path     path = (Path) key.watchable();
        for (WatchEvent<?> event : key.pollEvents()) {
            notifyObservers(event.kind(), path.resolve((Path) event.context()).toFile());
        }
        return key.reset();
    }
    
    protected void notifyObservers(final WatchEvent.Kind<?> kind, final File file) {
        FileEvent event = new FileEvent(file);
        if (kind == ENTRY_CREATE) {
            for (FileObserver observer : observers) {
                observer.onCreated(event);
            }
            if (file.isDirectory()) {
                new FileWatcher(file).setObservers(observers).watch();
            }
        } else if (kind == ENTRY_MODIFY) {
            for (FileObserver observer : observers) {
                observer.onModified(event);
            }
        } else if (kind == ENTRY_DELETE) {
            for (FileObserver observer : observers) {
                observer.onDeleted(event);
            }
        }
    }


    public FileWatcher addObserver(final FileObserver observer) {
        observers.add(observer);
        return this;
    }
    public FileWatcher removeObserver(final FileObserver observer) {
        observers.remove(observer);
        return this;
    }

    public List<FileObserver> getObservers() {
        return observers;
    }
    public FileWatcher setObservers(final List<FileObserver> observers) {
        this.observers = observers;
        return this;
    }
    
    public static List<WatchService> getWatchServices() {
        return Collections.unmodifiableList(watchServices);
    }
}
