module eu.hansolo.fx.jdkmon {
    //
    // Java
    requires java.net.http;
    requires java.desktop;

    // Java-FX
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;

    // 3rd Party
    requires transitive com.google.gson;
    requires transitive eu.hansolo.jdktools;
    requires transitive ch.qos.logback.classic;
    requires transitive ch.qos.logback.core;
    requires transitive org.slf4j;
    requires io.foojay.api.discoclient;
    requires eu.hansolo.cvescanner;
    requires com.dustinredmond.fxtrayicon;
    //requires FXTrayIcon;
    requires org.apache.commons.compress;
    requires org.tukaani.xz;
    requires eu.hansolo.toolbox;

    exports eu.hansolo.fx.jdkmon;
    exports eu.hansolo.fx.jdkmon.tools;
    exports eu.hansolo.fx.jdkmon.controls;
    exports eu.hansolo.fx.jdkmon.notification;
}