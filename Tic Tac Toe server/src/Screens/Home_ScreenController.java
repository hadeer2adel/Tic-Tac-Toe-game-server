
package Screens;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Home_ScreenController {

     private Stage stage;
     private Scene scene;
     private Parent root;
    
    public void switcToChartScreen(ActionEvent event) throws IOException{//inviation Screen
        root = FXMLLoader.load(getClass().getResource("/Screens/Chart_Screen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
}
