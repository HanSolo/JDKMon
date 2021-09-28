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

import eu.hansolo.fx.jdkmon.Main;
import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.pkg.VersionNumber;

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

    public  static final String     VERSION_PROPERTIES       = "version.properties";
    public  static final String     JDKMON_PROPERTIES        = "jdkmon.properties";
    public  static final String     SEARCH_PATH              = "searchpath";
    public  static final String     REMEMBER_DOWNLOAD_FOLDER = "remember_download_folder";
    public  static final String     DOWNLOAD_FOLDER          = "download_folder";
    public  static final String     DARK_MODE                = "dark_mode";
    public  static final String     VERSION                  = "version";
    private              Properties jdkMonProperties;
    private              Properties versionProperties;


    // ******************** Constructors **************************************
    PropertyManager() {
        jdkMonProperties = new Properties();
        // Load properties
        final String jdkMonPropertiesFilePath = new StringBuilder(System.getProperty("user.home")).append(File.separator).append(JDKMON_PROPERTIES).toString();

        // Create properties file if not exists
        Path path = Paths.get(jdkMonPropertiesFilePath);
        if (!Files.exists(path)) { createProperties(jdkMonProperties); }

        // Load properties file
        try (FileInputStream jdkMonPropertiesFile = new FileInputStream(jdkMonPropertiesFilePath)) {
            jdkMonProperties.load(jdkMonPropertiesFile);
        } catch (IOException ex) {
            System.out.println("Error reading jdkmon properties file. " + ex);
        }

        // If properties empty, fill with default values
        if (jdkMonProperties.isEmpty()) { createProperties(jdkMonProperties); }

        // Version number properties
        versionProperties = new Properties();
        try {
            versionProperties.load(Main.class.getResourceAsStream(VERSION_PROPERTIES));
        } catch (IOException ex) {
            System.out.println("Error reading version properties file. " + ex);
        }

    }


    // ******************** Methods *******************************************
    public Properties getJdkMonProperties() { return jdkMonProperties; }

    public Object get(final String KEY) { return jdkMonProperties.getOrDefault(KEY, ""); }
    public void set(final String KEY, final String VALUE) {
        jdkMonProperties.setProperty(KEY, VALUE);
        try {
            jdkMonProperties.store(new FileOutputStream(String.join(File.separator, System.getProperty("user.dir"), JDKMON_PROPERTIES)), null);
        } catch (IOException exception) {
            System.out.println("Error writing properties file: " + exception);
        }
    }

    public String getString(final String key) { return jdkMonProperties.getOrDefault(key, "").toString(); }

    public double getDouble(final String key) { return Double.parseDouble(jdkMonProperties.getOrDefault(key, "0").toString()); }

    public float getFloat(final String key) { return Float.parseFloat(jdkMonProperties.getOrDefault(key, "0").toString()); }

    public int getInt(final String key) { return Integer.parseInt(jdkMonProperties.getOrDefault(key, "0").toString()); }

    public long getLong(final String key) { return Long.parseLong(jdkMonProperties.getOrDefault(key, "0").toString()); }

    public boolean getBoolean(final String key) { return Boolean.parseBoolean(jdkMonProperties.getOrDefault(key, Boolean.FALSE).toString()); }

    public boolean hasKey(final String key) { return jdkMonProperties.containsKey(key); }

    public void storeProperties() {
        if (null == jdkMonProperties) { return; }
        final String propFilePath = new StringBuilder(System.getProperty("user.home")).append(File.separator).append(JDKMON_PROPERTIES).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            jdkMonProperties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void resetProperties() {
        final String propFilePath = new StringBuilder(System.getProperty("user.home")).append(File.separator).append(JDKMON_PROPERTIES).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            final String searchPath;
            switch(DiscoClient.getOperatingSystem()) {
                case MACOS  : searchPath = Finder.MACOS_JAVA_INSTALL_PATH;   break;
                case WINDOWS: searchPath = Finder.WINDOWS_JAVA_INSTALL_PATH; break;
                case LINUX  : searchPath = Finder.LINUX_JAVA_INSTALL_PATH;   break;
                default     : searchPath = "";
            }
            jdkMonProperties.put(SEARCH_PATH, searchPath);
            jdkMonProperties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public VersionNumber getVersionNumber() {
        return VersionNumber.fromText(versionProperties.getProperty(VERSION));
    }


    // ******************** Properties ****************************************
    private void createProperties(Properties properties) {
        final String propFilePath = new StringBuilder(System.getProperty("user.home")).append(File.separator).append(JDKMON_PROPERTIES).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            final String searchPath;
            switch(DiscoClient.getOperatingSystem()) {
                case MACOS  : searchPath = Finder.MACOS_JAVA_INSTALL_PATH;   break;
                case WINDOWS: searchPath = Finder.WINDOWS_JAVA_INSTALL_PATH; break;
                case LINUX  : searchPath = Finder.LINUX_JAVA_INSTALL_PATH;   break;
                default     : searchPath = "";
            }
            properties.put(SEARCH_PATH, searchPath);
            properties.put(REMEMBER_DOWNLOAD_FOLDER, "FALSE");
            properties.put(DOWNLOAD_FOLDER, "");
            properties.put(DARK_MODE, "FALSE");
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
