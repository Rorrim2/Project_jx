package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;


import javafx.stage.Stage;
import java.io.IOException;



public class Controller {
    @FXML
    private BorderPane border_log_scene;
    @FXML
    private MenuItem menu_item_edit;
    @FXML
    private TextField text_field_username;
    @FXML
    private TextField text_field_pathname;
    @FXML
    private Label text_failed_log_in;
    public static String username;
    public static String pathname;

    public void switchMode(ActionEvent event) {
        String caspian = getClass().getResource("caspian.css").toExternalForm();
        String modena = getClass().getResource("modena_dark.css").toExternalForm();

        Scene scene = border_log_scene.getScene();

        if(scene.getStylesheets().contains(caspian)) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(modena);
            menu_item_edit.setText("Switch to light mode");
        }
        else if(scene.getStylesheets().contains(modena)) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(caspian);
            menu_item_edit.setText("Switch to dark mode");
        }
    }
    public void buttonLogPressed(ActionEvent event) {
        username =  text_field_username.getText();
        pathname = text_field_pathname.getText();
        if(username.isEmpty() || pathname.isEmpty()) {
            text_failed_log_in.setOpacity(1);
            text_field_username.clear();
            text_field_pathname.clear();
        }
        else {
            Parent tableViewParent = null;
            /*FXMLLoader loader = new FXMLLoader(getClass().getResource("main_scene.fxml"));
            loader.setController(new Main_Scene_Controller());
            try {
                tableViewParent = loader.load();
            }

            catch (Exception e) {
                System.out.println(e.getStackTrace());
            }*/
            try {
                tableViewParent = FXMLLoader.load(Main_Scene_Controller.class.getResource("C:\\Users\\aliss\\IdeaProjects\\project_jx\\src\\sample\\main_scene.fxml"));
            }
            catch (Exception e) {
                System.out.println(e.getStackTrace());
            }

            Scene tableViewScene = new Scene(tableViewParent);

            String caspian = getClass().getResource("caspian.css").toExternalForm();
            //Stage information
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setTitle("User application");
            tableViewScene.getStylesheets().add(caspian);
            window.setScene(tableViewScene);
            window.show();
        }
    }
}
