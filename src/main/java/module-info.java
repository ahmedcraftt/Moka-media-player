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

    // Export packages that need to be accessed by other modules or the JVM
    exports ui.main;
    exports test;
    exports infrastructure.audio;
    exports config;
    exports entities;

    // Open packages for reflection (JavaFX and vlcj factory often need this)
    opens ui.main to javafx.fxml;
    opens ui.controllers to javafx.fxml;
    opens entities to javafx.base;
    exports infrastructure.media;
    opens infrastructure.media to javafx.base;
    opens infrastructure.audio to javafx.base;
    opens mediaLibrary to com.google.gson;
}
