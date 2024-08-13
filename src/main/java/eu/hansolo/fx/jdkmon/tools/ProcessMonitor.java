/*
 * Copyright (c) 2024 by Gerrit Grunwald
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

import eu.hansolo.jdktools.OperatingSystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


public class ProcessMonitor {
    private static final OperatingSystem          OS              = Finder.detectOperatingSystem();
    private static final Matcher                  MATCHER         = OperatingSystem.WINDOWS == OS ? Constants.JAVA_PATH_PATTERN_WIN.matcher("") : Constants.JAVA_PATH_PATTERN.matcher("");
    private        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private              Set<String>              activeJavaSet   = new HashSet<>();
    private              boolean                  running         = false;


    public ProcessMonitor() {

    }


    public void start() {
        if (this.running) { return; }
        this.running = true;
        this.executorService.scheduleWithFixedDelay(() -> this.scan(), 0, 30, TimeUnit.SECONDS);
    }

    public void stop() throws InterruptedException {
        this.executorService.shutdown();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            this.executorService.shutdownNow();
            this.running = false;
        }
    }

    public List<String> runOnce() {
        scan();
        return getActiveJavaInstallations();
    }

    public List<String> getActiveJavaInstallations() {
        return new ArrayList<>(activeJavaSet);
    }

    private void scan() {
        ProcessHandle.allProcesses().forEach(process -> checkProcessDetails(process));
    }

    private void checkProcessDetails(final ProcessHandle process) {
        Optional<String> cmdLineOpt = process.info().commandLine();
        if (cmdLineOpt.isPresent()) {
            String cmdLine = cmdLineOpt.get();
            MATCHER.reset(cmdLine);
            final List<MatchResult> results = MATCHER.results().collect(Collectors.toList());
            if (!results.isEmpty()) {
                MatchResult result = results.get(0);
                activeJavaSet.add(result.group(1));
            }
        }
    }

    private String processDetails(final ProcessHandle process) {
        return String.format("%8d %8s %10s %26s %-40s",
                             process.pid(),
                             toString(process.parent().map(ProcessHandle::pid)),
                             toString(process.info().user()),
                             toString(process.info().startInstant()),
                             toString(process.info().commandLine()));
    }
    private String toString(final Optional<?> optional) {
        return optional.map(Object::toString).orElse("-");
    }


    public static void main(String[] args) {
        new ProcessMonitor();
    }
}
