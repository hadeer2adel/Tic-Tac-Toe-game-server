package server;

import DTO.UserData;
import DataBase.DataAccessObject;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class UserHandler extends Thread
{
    private static Vector<UserHandler> UserHandlers = new Vector<>();
    private static ArrayList<UserData> onlinePlayers = new ArrayList<>();
    
    private UserData player;
    private Socket clientSocket;
    private DataInputStream ear;
    private DataOutputStream mouth;
        
    public UserHandler(Socket cs) {
        clientSocket = cs;
        UserHandlers.add(this);
        start();
    }
    
    public void run(){
        while(true){
            try {
                ear = new DataInputStream(clientSocket.getInputStream());
                mouth = new DataOutputStream(clientSocket.getOutputStream());
                String serverResponse = ear.readUTF();
                JsonReader jsonReader = Json.createReader(new StringReader(serverResponse));
                JsonObject requestJson = jsonReader.readObject();
                handleRequest(requestJson);
            } catch (IOException ex) {
                try {
                    clientSocket.close();
                    ear.close();
                    mouth.close();
                } catch (IOException ex1) {
                    Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }
    
    private void handleRequest(JsonObject requestJson) {
        try {
            String responseType = requestJson.getString("request");
            switch (responseType) {
                case "login":
                    login(requestJson);
                    break;
                default:
                    System.out.println("Unknown response type: " + responseType);
                    break;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void login(JsonObject requestJson) throws SQLException, IOException{
        String email = requestJson.getString("email");
        String password = requestJson.getString("password");
        
        UserData player = DataAccessObject.getUser(email, password);

        if (player != null) {
            player.setIs_available(true);
            player.setIs_onGame(false);
            DataAccessObject.updateStatus(player);
            onlinePlayers = DataAccessObject.getAvailableUser();
            
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response","login")
                    .add("status","success")
                    .add("id",player.getId())
                    .add("name",player.getName())
                    .add("email",player.getEmail())
                    .add("password",player.getPassword())
                    .add("score",player.getScore())
                    .add("is_available",player.getIs_available())
                    .add("is_onGame",player.getIs_onGame())
                    .build();
            mouth.writeUTF(responseJson.toString());
        } 
        else {
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response","login")
                    .add("status","fail")
                    .build();
            mouth.writeUTF(responseJson.toString());
        }
    }
}