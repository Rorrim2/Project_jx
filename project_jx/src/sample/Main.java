package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("log_scene.fxml"));
        String caspian = getClass().getResource("caspian.css").toExternalForm();

        primaryStage.setTitle("Log in");
        Parent primaryParent = FXMLLoader.load(getClass().getResource("log_scene.fxml"));
        Scene primaryScene = new Scene(primaryParent);
        primaryStage.setScene(primaryScene);
        primaryScene.getStylesheets().add(caspian);
        primaryStage.show();
}


    public static void main(String[] args) {
        launch(args);
    }
}
