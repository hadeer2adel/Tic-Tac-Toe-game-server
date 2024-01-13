
package Screens;

import DataBase.DataAccessObject;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import server.Server;


public class Home_ScreenController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    private Server server;
     
    @FXML
    private Button btn_server_is_off;
    
    public void switchServer(ActionEvent event) throws IOException, SQLException{//inviation Screen
        String status = btn_server_is_off.getText();
        if (status.equalsIgnoreCase("Server is Off")) {
            btn_server_is_off.setText("Server is On");
            DataAccessObject.start();
            server = new Server();
        } 
        else if (status.equalsIgnoreCase("Server is On")) {
            btn_server_is_off.setText("Server is Off");
            DataAccessObject.stop();
            server.stopServer();
        }
    }
    
    public void switcToChartScreen(ActionEvent event) throws IOException{//inviation Screen
        String status = btn_server_is_off.getText();
        if (status.equalsIgnoreCase("Server is On")) {
            root = FXMLLoader.load(getClass().getResource("/Screens/Chart_Screen.fxml"));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }
    
}
