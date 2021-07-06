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

import io.foojay.api.discoclient.DiscoClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


public enum PropertyManager {
    INSTANCE;

    public  static final String     SEARCH_PATH          = "searchpath";
    private static final String     PROPERTIES_FILE_NAME = "jdkmon.properties";
    private              Properties properties;


    // ******************** Constructors **************************************
    PropertyManager() {
        properties = new Properties();
        // Load properties
        final String propFilePath = new StringBuilder(System.getProperty("user.home")).append(File.separator).append(PROPERTIES_FILE_NAME).toString();

        // Create properties file if not exists
        Path path = Paths.get(propFilePath);
        if (!Files.exists(path)) { createProperties(properties); }

        // Load properties file
        try (FileInputStream propFile = new FileInputStream(propFilePath)) {
            properties.load(propFile);
        } catch (IOException ex) {
            System.out.println("Error reading properties file. " + ex);
        }

        // If properties empty, fill with default values
        if (properties.isEmpty()) { createProperties(properties); }
    }


    // ******************** Methods *******************************************
    public Properties getProperties() { return properties; }

    public Object get(final String KEY) { return properties.getOrDefault(KEY, ""); }
    public void set(final String KEY, final String VALUE) {
        properties.setProperty(KEY, VALUE);
        try {
            properties.store(new FileOutputStream(String.join(File.separator, System.getProperty("user.dir"), PROPERTIES_FILE_NAME)), null);
        } catch (IOException exception) {
            System.out.println("Error writing properties file: " + exception);
        }
    }

    public String getString(final String key) { return properties.getOrDefault(key, "").toString(); }

    public double getDouble(final String key) { return Double.parseDouble(properties.getOrDefault(key, "0").toString()); }

    public float getFloat(final String key) { return Float.parseFloat(properties.getOrDefault(key, "0").toString()); }

    public int getInt(final String key) { return Integer.parseInt(properties.getOrDefault(key, "0").toString()); }

    public long getLong(final String key) { return Long.parseLong(properties.getOrDefault(key, "0").toString()); }

    public void storeProperties() {
        if (null == properties) { return; }
        final String propFilePath = new StringBuilder(System.getProperty("user.home")).append(File.separator).append(PROPERTIES_FILE_NAME).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void resetProperties() {
        final String propFilePath = new StringBuilder(System.getProperty("user.home")).append(File.separator).append(PROPERTIES_FILE_NAME).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            final String searchPath;
            switch(DiscoClient.getOperatingSystem()) {
                case MACOS  : searchPath = Finder.MACOS_JAVA_INSTALL_PATH;   break;
                case WINDOWS: searchPath = Finder.WINDOWS_JAVA_INSTALL_PATH; break;
                case LINUX  : searchPath = Finder.LINUX_JAVA_INSTALL_PATH;   break;
                default     : searchPath = "";
            }
            properties.put(SEARCH_PATH, searchPath);
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    // ******************** Properties ****************************************
    private void createProperties(Properties properties) {
        final String propFilePath = new StringBuilder(System.getProperty("user.home")).append(File.separator).append(PROPERTIES_FILE_NAME).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            final String searchPath;
            switch(DiscoClient.getOperatingSystem()) {
                case MACOS  : searchPath = Finder.MACOS_JAVA_INSTALL_PATH;   break;
                case WINDOWS: searchPath = Finder.WINDOWS_JAVA_INSTALL_PATH; break;
                case LINUX  : searchPath = Finder.LINUX_JAVA_INSTALL_PATH;   break;
                default     : searchPath = "";
            }
            properties.put(SEARCH_PATH, searchPath);
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
