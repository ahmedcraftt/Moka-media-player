module org.example.moka_music_player {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;

    // Explicitly require JNA for native library access
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires jaudiotagger;
    requires com.google.gson;
    requires org.slf4j;
    requires java.desktop;
    requires java.sql;
    requires uk.co.caprica.vlcj;
    requires annotations;

    // Export packages that need to be accessed by other modules or the JVM
    exports gui.main;
    exports infrastructure.audio;
    exports config;
    exports domain.model.metadata;
    exports domain.model.media;
    exports domain.model.library;
    exports infrastructure.media;
    exports application.dto;
    exports infrastructure.scanner;
    exports infrastructure.storage;
    exports infrastructure.mapper;
    exports platform;
    exports bootstrap;
    exports domain.audio;

    // Open packages for reflection
    opens gui.main to javafx.fxml;
    opens gui.controllers to javafx.fxml;
    opens infrastructure.media to javafx.base;
    opens infrastructure.audio to javafx.base;
    opens application.dto to javafx.base;
    opens infrastructure.scanner to javafx.base;
    opens infrastructure.storage to javafx.base;
    opens infrastructure.mapper to javafx.base;
    opens infrastructure.factory to javafx.base;
    opens domain.audio to javafx.base;
    opens gui.utils to javafx.fxml;
    opens domain.model.media to com.google.gson, javafx.base;
    opens domain.model.metadata to com.google.gson, javafx.base;
    opens domain.model.library to com.google.gson, javafx.base;
}
