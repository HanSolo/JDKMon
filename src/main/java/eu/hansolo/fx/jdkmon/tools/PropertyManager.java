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
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.util.Helper;
import eu.hansolo.jdktools.versioning.VersionNumber;
import io.foojay.api.discoclient.DiscoClient;

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

    public static final String     VERSION_PROPERTIES                = "version.properties";
    public static final String     JDKMON_PROPERTIES                 = "jdkmon.properties";
    public static final String     PROPERTY_VERSION                  = "version";
    public static final String     PROPERTY_SEARCH_PATH              = "searchpath";
    public static final String     PROPERTY_JAVAFX_SEARCH_PATH       = "javafx_searchpath";
    public static final String     PROPERTY_REMEMBER_DOWNLOAD_FOLDER = "remember_download_folder";
    public static final String     PROPERTY_DOWNLOAD_FOLDER          = "download_folder";
    public static final String     PROPERTY_DARK_MODE                = "dark_mode";
    public static final String     PROPERTY_FEATURES                 = "features";
    public static final String     PROPERTY_AUTO_EXTRACT             = "autoextract";
    public static final String     PROPERTY_SHOW_UNKNOWN_BUILDS      = "show_unknown_builds";
    public static final String     PROPERTY_SHOW_NOTIFICATIONS       = "show_notifications";
    private             Properties properties;
    private             Properties versionProperties;


    // ******************** Constructors **************************************
    PropertyManager() {
        properties = new Properties();
        // Load properties
        final String jdkMonPropertiesFilePath = new StringBuilder(Constants.HOME_FOLDER).append(JDKMON_PROPERTIES).toString();

        // Create properties file if not exists
        Path path = Paths.get(jdkMonPropertiesFilePath);
        if (!Files.exists(path)) { createProperties(properties); }

        // Load properties file
        try (FileInputStream jdkMonPropertiesFile = new FileInputStream(jdkMonPropertiesFilePath)) {
            properties.load(jdkMonPropertiesFile);
        } catch (IOException ex) {
            System.out.println("Error reading jdkmon properties file. " + ex);
        }

        // If properties empty, fill with default values
        if (properties.isEmpty()) {
            createProperties(properties);
        } else {
            validateProperties();
        }

        // Version number properties
        versionProperties = new Properties();
        try {
            versionProperties.load(Main.class.getResourceAsStream(VERSION_PROPERTIES));
        } catch (IOException ex) {
            System.out.println("Error reading version properties file. " + ex);
        }

    }


    // ******************** Methods *******************************************
    public Properties getProperties() { return properties; }

    public Object get(final String KEY) { return properties.getOrDefault(KEY, ""); }
    public void set(final String KEY, final String VALUE) {
        properties.setProperty(KEY, VALUE);
        storeProperties();
    }

    public String getString(final String key) { return properties.getOrDefault(key, "").toString(); }
    public void setString(final String key, final String value) { properties.setProperty(key, value); }

    public double getDouble(final String key) { return getDouble(key, 0); }
    public double getDouble(final String key, final double defaultValue) { return Double.parseDouble(properties.getOrDefault(key, Double.toString(defaultValue)).toString()); }
    public void setDouble(final String key, final double value) { properties.setProperty(key, Double.toString(value)); }

    public float getFloat(final String key) { return getFloat(key, 0); }
    public float getFloat(final String key, final float defaultValue) { return Float.parseFloat(properties.getOrDefault(key, Float.toString(defaultValue)).toString()); }
    public void setFloat(final String key, final float value) { properties.setProperty(key, Float.toString(value)); }

    public int getInt(final String key) { return getInt(key, 0); }
    public int getInt(final String key, final int defaultValue) { return Integer.parseInt(properties.getOrDefault(key, Integer.toString(defaultValue)).toString()); }
    public void setInt(final String key, final int value) { properties.setProperty(key, Integer.toString(value)); }

    public long getLong(final String key) { return getLong(key, 0); }
    public long getLong(final String key, final long defaultValue) { return Long.parseLong(properties.getOrDefault(key, Long.toString(defaultValue)).toString()); }
    public void setLong(final String key, final long value) { properties.setProperty(key, Long.toString(value)); }

    public boolean getBoolean(final String key) { return getBoolean(key, false); }
    public boolean getBoolean(final String key, final boolean defaultValue) { return Boolean.parseBoolean(properties.getOrDefault(key, Boolean.toString(defaultValue)).toString()); }
    public void setBoolean(final String key, final boolean value) { properties.setProperty(key, Boolean.toString(value)); }

    public boolean hasKey(final String key) { return properties.containsKey(key); }

    public void storeProperties() {
        if (null == properties) { return; }
        final String propFilePath = new StringBuilder(Constants.HOME_FOLDER).append(JDKMON_PROPERTIES).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void resetSearchPathProperty(final boolean javafx) {
        final String propFilePath = new StringBuilder(Constants.HOME_FOLDER).append(JDKMON_PROPERTIES).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            if (javafx) {
                properties.put(PROPERTY_JAVAFX_SEARCH_PATH, Constants.HOME_FOLDER);
            } else {
                final String searchPath = getDefaultSearchPath();
                properties.put(PROPERTY_SEARCH_PATH, searchPath);
            }
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public VersionNumber getVersionNumber() {
        return VersionNumber.fromText(versionProperties.getProperty(PROPERTY_VERSION));
    }


    // ******************** Properties ****************************************
    private String getDefaultSearchPath() {
        final OperatingSystem operatingSystem = Helper.getOperatingSystem();
        final String searchPath;
        switch(operatingSystem) {
            case MACOS                           -> searchPath = Finder.MACOS_JAVA_INSTALL_PATH;
            case WINDOWS                         -> searchPath = Finder.WINDOWS_JAVA_INSTALL_PATH;
            case LINUX, ALPINE_LINUX, LINUX_MUSL -> searchPath = Finder.LINUX_JAVA_INSTALL_PATH;
            default                              -> searchPath = "";
        }
        return searchPath;
    }

    private void validateProperties() {
        if (null == properties) { return; }
        boolean storeProperties = false;
        if (!properties.containsKey(PROPERTY_SEARCH_PATH)) { properties.put(PROPERTY_SEARCH_PATH, getDefaultSearchPath()); storeProperties = true; }
        if (!properties.containsKey(PROPERTY_JAVAFX_SEARCH_PATH)) { properties.put(PROPERTY_JAVAFX_SEARCH_PATH, Constants.HOME_FOLDER); storeProperties = true; }
        if (!properties.containsKey(PROPERTY_REMEMBER_DOWNLOAD_FOLDER)) { properties.put(PROPERTY_REMEMBER_DOWNLOAD_FOLDER, "FALSE"); storeProperties = true; }
        if (!properties.containsKey(PROPERTY_DOWNLOAD_FOLDER)) { properties.put(PROPERTY_DOWNLOAD_FOLDER, ""); storeProperties = true; }
        if (!properties.containsKey(PROPERTY_DARK_MODE)) { properties.put(PROPERTY_DARK_MODE, "FALSE"); storeProperties = true; }
        if (!properties.containsKey(PROPERTY_FEATURES)) { properties.put(PROPERTY_FEATURES, Constants.FEATURES); storeProperties = true; }
        if (!properties.containsKey(PROPERTY_AUTO_EXTRACT)) { properties.put(PROPERTY_AUTO_EXTRACT, "FALSE"); storeProperties = true; }
        if (!properties.containsKey(PROPERTY_SHOW_UNKNOWN_BUILDS)) { properties.put(PROPERTY_SHOW_UNKNOWN_BUILDS, "FALSE"); storeProperties = true; }
        if (!properties.containsKey(PROPERTY_SHOW_NOTIFICATIONS)) { properties.put(PROPERTY_SHOW_NOTIFICATIONS, "TRUE"); storeProperties = true; }
        if (storeProperties) { storeProperties(); }
    }

    private void createProperties(Properties properties) {
        final String propFilePath = new StringBuilder(Constants.HOME_FOLDER).append(JDKMON_PROPERTIES).toString();
        try (OutputStream output = new FileOutputStream(propFilePath)) {
            final String searchPath = getDefaultSearchPath();
            properties.put(PROPERTY_SEARCH_PATH, searchPath);
            properties.put(PROPERTY_JAVAFX_SEARCH_PATH, Constants.HOME_FOLDER);
            properties.put(PROPERTY_REMEMBER_DOWNLOAD_FOLDER, "FALSE");
            properties.put(PROPERTY_DOWNLOAD_FOLDER, "");
            properties.put(PROPERTY_DARK_MODE, "FALSE");
            properties.put(PROPERTY_FEATURES, Constants.FEATURES);
            properties.put(PROPERTY_AUTO_EXTRACT, "FALSE");
            properties.put(PROPERTY_SHOW_UNKNOWN_BUILDS, "FALSE");
            properties.put(PROPERTY_SHOW_NOTIFICATIONS, "TRUE");
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
