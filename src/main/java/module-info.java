module eu.hansolo.fx.jdkmon {
    // Java
    requires java.base;

    // Java-FX
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;

    // 3rd Party
    requires com.google.gson;
    requires slf4j.api;
    requires org.apache.logging.log4j;
    requires io.foojay.api.discoclient;
    requires java.validation;
    requires FXTrayIcon;

    exports eu.hansolo.fx.jdkmon;
    exports eu.hansolo.fx.jdkmon.tools;
    exports eu.hansolo.fx.jdkmon.controls;
    exports eu.hansolo.fx.jdkmon.notification;
}