
package Screens;

import DataBase.DataAccessObject;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;
import server.Server;


public class Chart_ScreenController implements Initializable{

   
    private Stage stage;
    private Scene scene;
    private Parent root;
    private static Server server;
    
    @FXML
    private PieChart chart;

    public static Server getServer() {
        return server;
    }

    public static void setServer(Server server) {
        Chart_ScreenController.server = server;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            server = Home_ScreenController.getServer();
            int [] players = DataAccessObject.getChartData();
            
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Offline Players", players[0]),
                    new PieChart.Data("Available Players", players[1]),
                    new PieChart.Data("OnGame Players", players[2]));
            
            chart.setData(pieChartData);
        } catch (SQLException ex) {
            Logger.getLogger(Chart_ScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void switchToHome(ActionEvent event) throws IOException{
        Home_ScreenController.setServer(server);
        root = FXMLLoader.load(getClass().getResource("/Screens/Home_Screen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
}