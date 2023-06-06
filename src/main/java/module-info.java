module ru.media.myvideoplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens ru.media.myvideoplayer to javafx.fxml;
    exports ru.media.myvideoplayer;
}