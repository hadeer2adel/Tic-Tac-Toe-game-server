
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


public class Home_ScreenController implements Initializable{

    private Stage stage;
    private Scene scene;
    private Parent root;
    private static Server server;
     
    @FXML
    private Button btn_server_is_off;

    public static Server getServer() {
        return server;
    }

    public static void setServer(Server server) {
        Home_ScreenController.server = server;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        server = Chart_ScreenController.getServer();
        if (server != null) {
            btn_server_is_off.setText("Server is On");
        } 
        else{
            btn_server_is_off.setText("Server is Off");
        }
    }
    
    public void switchServer(ActionEvent event) throws IOException, SQLException{//inviation Screen
        if (server == null) {
            btn_server_is_off.setText("Server is On");
            DataAccessObject.start();
            server = new Server();
        } 
        else{
            btn_server_is_off.setText("Server is Off");
            DataAccessObject.stop();
            server.stopServer();
            server = null;
        }
    }
    
    public void switcToChartScreen(ActionEvent event) throws IOException{//inviation Screen
        if (server != null) {
            Chart_ScreenController.setServer(server);
            root = FXMLLoader.load(getClass().getResource("/Screens/Chart_Screen.fxml"));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }
    
}
