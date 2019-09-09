package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Server App");
        Parent primaryParent = FXMLLoader.load(getClass().getResource("serverapp.fxml"));
        Scene primaryScene = new Scene(primaryParent);
        primaryStage.setScene(primaryScene);
        primaryStage.show();

}


    public static void main(String[] args) {
        launch(args);
    }
}
