package ru.media.myvideoplayer;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class VideoController implements Initializable {
    @FXML
    private Label currentTimeLabel;
    @FXML
    private Label fsLabel;
    @FXML
    private MediaView media;
    private MediaPlayer player;
    @FXML
    private Button play;
    @FXML
    private Label speedLabel;
    @FXML
    private Slider timeLine;
    @FXML
    private Label totalTimeLabel;
    @FXML
    private VBox vbox;
    @FXML
    private HBox volumeHbox;
    @FXML
    private Label volumeLabel;
    @FXML
    private Slider volumeSlider;
    private boolean isMuted;
    private boolean isEnded;
    private boolean isPlaying;

    private final ImageView playView = new ImageView();
    private final ImageView pauseView = new ImageView();
    private final ImageView resetView = new ImageView();
    private final ImageView volumeView = new ImageView();
    private final ImageView fullscreenView = new ImageView();
    private final ImageView muteView = new ImageView();
    private final ImageView exitView = new ImageView();
    private double savedVolume = 0.0;
    private static Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a video to play");
        File file = fileChooser.showOpenDialog(stage);
        Media content = new Media(file.toURI().toString());
        player = new MediaPlayer(content);
        media.setMediaPlayer(player);

        initImageView(playView, "icons/play.png");
        initImageView(pauseView, "icons/pause.png");
        initImageView(resetView, "icons/reset.png");
        initImageView(volumeView, "icons/volume.png");
        initImageView(fullscreenView, "icons/fs.png");
        initImageView(muteView, "icons/mute.png");
        initImageView(exitView, "icons/exit.png");

        play.setGraphic(playView);
        volumeLabel.setGraphic(volumeView);
        speedLabel.setText("1x");
        fsLabel.setGraphic(fullscreenView);
        volumeSlider.setValue(0.5);
        play.setOnAction(actionEvent -> {
            Button buttonPlay = (Button) actionEvent.getSource();
            if(isEnded){
                timeLine.setValue(0);
                isEnded = false;
                isPlaying = false;
                buttonPlay.setGraphic(pauseView);
            } else if (isPlaying) {
                buttonPlay.setGraphic(playView);
                player.pause();
                isPlaying = false;
            } else {
                buttonPlay.setGraphic(pauseView);
                player.play();
                isPlaying = true;
            }
        });
        volumeSlider.valueProperty().addListener(observable -> {
            player.setVolume(volumeSlider.getValue());
            if(player.getVolume() != 0.0 ){
                volumeLabel.setGraphic(volumeView);
                isMuted = false;
            } else{
                volumeLabel.setGraphic(muteView);
                isMuted = true;
            }
        });
        speedLabel.setOnMouseClicked(mouseEvent -> {
            if(speedLabel.getText().equals("1x")){
                speedLabel.setText("2x");
                player.setRate(2.0);
            } else{
                speedLabel.setText("1x");
                player.setRate(1.0);
            }
        });
        volumeLabel.setOnMouseClicked(mouseEvent -> {
            if(isMuted){
                volumeLabel.setGraphic(volumeView);
                volumeSlider.setValue(savedVolume);
                isMuted = false;
            } else{
                volumeLabel.setGraphic(muteView);
                savedVolume = volumeSlider.getValue();
                volumeSlider.setValue(0);
                isMuted = true;
            }
        });

        vbox.sceneProperty().addListener((observableValue, scene, t1) -> {
            if(scene == null && t1 != null){
                media.fitHeightProperty().bind(t1.heightProperty().subtract(volumeHbox.heightProperty().add(20)));
            }
        });
        fsLabel.setOnMouseClicked(mouseEvent -> {
            Label label = (Label) mouseEvent.getSource();
            Stage stage = (Stage) label.getScene().getWindow();
            if(stage.isFullScreen()){
                stage.setFullScreen(false);
                fsLabel.setGraphic(fullscreenView);
            } else{
                stage.setFullScreen(true);
                fsLabel.setGraphic(exitView);
            }
            stage.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ESCAPE){
                    fsLabel.setGraphic(fullscreenView);
                }
            });
        });
        player.totalDurationProperty().addListener((observableValue, duration, t1) -> {
            timeLine.setMax(t1.toSeconds());
            totalTimeLabel.setText(getTime(t1));
        });
        timeLine.valueChangingProperty().addListener((observableValue, aBoolean, t1) -> {
            if(!t1){
                player.seek(Duration.seconds(timeLine.getValue()));
            }
        });
        timeLine.valueProperty().addListener((observableValue, number, t1) -> {
            double curr = player.getCurrentTime().toSeconds();
            if(Math.abs(curr - t1.doubleValue()) > 0.5){
                player.seek(Duration.seconds(t1.doubleValue()));
            }
        });
        player.volumeProperty().bindBidirectional(volumeSlider.valueProperty());
        bindTimeLabel();
        player.currentTimeProperty().addListener((observableValue, duration, t1) -> {
            if(!timeLine.isValueChanging()){
                timeLine.setValue(t1.toSeconds());
            }
        });
        player.setOnEndOfMedia(() -> {
            play.setGraphic(resetView);
            isEnded = true;
            if (!currentTimeLabel.textProperty().equals(totalTimeLabel)){
                currentTimeLabel.textProperty().unbind();
                currentTimeLabel.setText(getTime(player.getTotalDuration()) + " / ");
            }
        });
    }

    private void initImageView(ImageView view, String path){
        Image image = new Image(new File(path).toURI().toString());
        view.setImage(image);
        int IMAGE_SIZE = 25;
        view.setFitHeight(IMAGE_SIZE);
        view.setFitWidth(IMAGE_SIZE);
    }
    private void bindTimeLabel(){
        currentTimeLabel.textProperty().bind(Bindings.createStringBinding(() -> getTime(player.getCurrentTime()) + "/", player.currentTimeProperty()));
    }

    private String getTime(Duration currentTime) {
        int hours = (int) currentTime.toHours();
        int minutes = (int) currentTime.toMinutes();
        int seconds = (int) currentTime.toSeconds();
        if(seconds > 59) seconds = seconds % 60;
        if(minutes > 59) minutes = minutes % 60;
        if(hours > 59) hours = hours % 60;
        if(hours > 0) return String.format("%d:%02d:%02d", hours, minutes, seconds);
        else return String.format("%02d:%02d", minutes, seconds);

    }

    public static void setStage(Stage stage){
        VideoController.stage = stage;
    }

}
