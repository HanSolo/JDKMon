module eu.hansolo.fx.jdkmon {
    //
    // Java
    requires java.net.http;
    requires java.desktop;

    // Java-FX
    requires javafx.controls;

    // 3rd Party
    requires transitive com.google.gson;
    requires transitive eu.hansolo.jdktools;
    requires io.foojay.api.discoclient;
    requires eu.hansolo.cvescanner;
    requires com.dustinredmond.fxtrayicon;
    requires org.apache.commons.compress;
    requires org.tukaani.xz;

    opens eu.hansolo.fx.jdkmon to javafx.controls;

    exports eu.hansolo.fx.jdkmon;
    exports eu.hansolo.fx.jdkmon.tools;
    exports eu.hansolo.fx.jdkmon.controls;
    exports eu.hansolo.fx.jdkmon.notification;
}