module org.example.moka_music_player {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;

    // Explicitly require JNA for native library access
    requires vlcj;
    requires com.sun.jna;
    requires com.sun.jna.platform;

    requires jaudiotagger;
    requires annotations;
    requires com.google.gson;
    requires AhmedUtilsV2;
    requires org.slf4j;
    requires java.desktop;

    // Export packages that need to be accessed by other modules or the JVM
    exports gui.main;
    exports test;
    exports infrastructure.audio;
    exports config;
    exports domain.model;

    // Open packages for reflection (JavaFX and vlcj factory often need this)
    opens gui.main to javafx.fxml;
    opens gui.controllers to javafx.fxml;
    opens domain.model to javafx.base;
    exports infrastructure.media;
    opens infrastructure.media to javafx.base;
    opens infrastructure.audio to javafx.base;
    exports application.dto;
    opens application.dto to javafx.base;
    opens domain.library to com.google.gson;
    exports infrastructure.scanner;
    opens infrastructure.scanner to javafx.base;
    exports infrastructure.storge;
    opens infrastructure.storge to javafx.base;
    exports infrastructure.mapper;
    opens infrastructure.mapper to javafx.base;
    exports infrastructure.factory;
    opens infrastructure.factory to javafx.base;
    exports platform;
    exports bootstrap;
    exports domain.audio;
    opens domain.audio to javafx.base;
}
