package server;

import DTO.Records;
import DTO.UserData;
import DataBase.DataAccessObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class UserHandler extends Thread {

    private static Vector<UserHandler> UserHandlers = new Vector<>();

    private int id;
    private boolean online;
    private Socket clientSocket;
    private DataInputStream ear;
    private DataOutputStream mouth;

    public UserHandler(Socket cs) {
        System.out.println("server.UserHandler.<init>()");
        clientSocket = cs;
        start();
    }

    @Override
    public void run() {
        System.out.println("server.UserHandler.run()");
        while (!clientSocket.isClosed()) {
            try {
                ear = new DataInputStream(clientSocket.getInputStream());
                mouth = new DataOutputStream(clientSocket.getOutputStream());
                String serverResponse = ear.readUTF();
                JsonReader jsonReader = Json.createReader(new StringReader(serverResponse));
                JsonObject requestJson = jsonReader.readObject();

                if (requestJson.containsKey("response")) {
                    handleResponse(requestJson);
                } else if (requestJson.containsKey("request")) {
                    handleRequest(requestJson);
                }

            } catch (SocketException se) {
                closeSocket();
            } catch (IOException ex) {
                closeSocket();
            }
        }
    }
    
    public static void stopAll(){
        for (UserHandler user : UserHandlers) {
            user.closeSocket();
        }
    }

    private void closeSocket(){
        try {
            UserData player = new UserData(id, "", "", "", 0, false, false);
            int r = DataAccessObject.updateStatus(player);
            online = false;
            UserHandlers.remove(this);
            clientSocket.close();
            ear.close();
            mouth.close();
        } catch (IOException ex1) {
            Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex1);
        } catch (SQLException ex) {
            Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleResponse(JsonObject responseJson) {
        System.out.println("server.UserHandler.handleResponse()");
        try {
            String responseType = responseJson.getString("response");
            switch (responseType) {
                case "invite":
                    receiveInvitation(responseJson);
                    break;
                case "playAgain":
                    receivePlayAgain(responseJson);
                    break;
                default:
                    System.out.println("Unknown response type: " + responseType);
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleRequest(JsonObject requestJson) {
        System.out.println("server.UserHandler.handleRequest()");
        try {
            String requestType = requestJson.getString("request");
            switch (requestType) {
                case "login":
                    login(requestJson);
                    break;
                case "signup":
                    signUp(requestJson);
                    break;
                case "availablePlayers":
                    getAvailablePlayers(requestJson);
                    break;
                case "invite":
                    sendInvitation(requestJson);
                    break;
                case "move":
                    sendMove(requestJson);
                    break;   
                case "logout":
                    logout(requestJson);
                    break;
                case "record":
                    record(requestJson);
                    break;
                case "allRecords":
                    getAllRecords(requestJson);
                    break;
                case "playAgain":
                    sendPlayAgain(requestJson);
                    break;
                case "cancel":
                    sendCancel(requestJson);
                    break;
                default:
                    System.out.println("Unknown response type: " + requestType);
                    break;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void login(JsonObject requestJson) throws SQLException, IOException {
        System.out.println("server.UserHandler.login()");
        String email = requestJson.getString("email");
        String password = requestJson.getString("password");

        UserData player = DataAccessObject.getUser(email, password);

        if (player != null) {
            player.setIs_available(true);
            player.setIs_onGame(false);
            DataAccessObject.updateStatus(player);

            online = true;
            id = player.getId();
            UserHandlers.add(this);

            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response", "login")
                    .add("status", "success")
                    .add("id", player.getId())
                    .add("name", player.getName())
                    .add("score", player.getScore())
                    .build();
            mouth.writeUTF(responseJson.toString());
        } else {
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response", "login")
                    .add("status", "fail")
                    .build();
            mouth.writeUTF(responseJson.toString());
        }
    }

    private void signUp(JsonObject requestJson) throws SQLException, IOException {
        System.out.println("server.UserHandler.signUp()");
        String email = requestJson.getString("email");
        String password = requestJson.getString("password");
        String name = requestJson.getString("name");
        UserData user = new UserData(MIN_PRIORITY, name, email, password, MIN_PRIORITY, true, true);
        int added = DataAccessObject.addUser(user);

        if (added > 0) {
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response", "signup")
                    .add("status", "success")
                    .build();
            mouth.writeUTF(responseJson.toString());
        } else {
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response", "signup")
                    .add("status", "fail")
                    .build();
            mouth.writeUTF(responseJson.toString());
        }

    }

    private void getAvailablePlayers(JsonObject requestJson) throws SQLException, IOException {
        int idd = requestJson.getInt("id");
        JsonArrayBuilder usersBuilder = Json.createArrayBuilder();

        ArrayList<UserData> onlinePlayers = DataAccessObject.getAvailableUser();
        for (UserData player : onlinePlayers) {
            if(idd != player.getId()){
                JsonObject user = Json.createObjectBuilder()
                    .add("id", player.getId())
                    .add("name", player.getName())
                    .add("score", player.getScore())
                    .build();
            usersBuilder.add(user);
            }
        }
        JsonArray users = usersBuilder.build();
        JsonObject responseJson = Json.createObjectBuilder()
                .add("response", "availablePlayers")
                .add("status", "success")
                .add("players", users)
                .build();
        mouth.writeUTF(responseJson.toString());
        
    }

    private void sendInvitation(JsonObject requestJson) throws IOException {
        System.out.println("server.UserHandler.sendInvitation()");
        int id2 = requestJson.getInt("id2");
        UserHandler player2 = getUser(id2);
        if(player2.online){
            player2.mouth.writeUTF(requestJson.toString());
            player2.mouth.flush();
        }
        else {
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response", "invite")
                    .add("status", false)
                    .add("id1", requestJson.getInt("id1"))
                    .add("id2", requestJson.getInt("id2"))
                    .add("name1", requestJson.getString("name1"))
                    .add("name2", requestJson.getString("name2"))
                    .build();
            mouth.writeUTF(responseJson.toString());
            mouth.flush();
        }
    }

    private void receiveInvitation(JsonObject responseJson) throws IOException {
        System.out.println("server.UserHandler.receiveInvitation()");
        int id1 = responseJson.getInt("id1");
        UserHandler player1 = getUser(id1);
        int id2 = responseJson.getInt("id2");
        UserHandler player2 = getUser(id2);
        
        if(responseJson.getBoolean("status")){
            try {
                UserData user = new UserData(id1, "", "", "", 0, true, true);
                DataAccessObject.updateStatus(user);
                player1.online = false;
                
                user = new UserData(id2, "", "", "", 0, true, true);
                DataAccessObject.updateStatus(user);
                player2.online = false;
            } catch (SQLException ex) {
                Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        player1.mouth.writeUTF(responseJson.toString());
        mouth.flush();
    }
    
    private void logout(JsonObject requestJson) throws SQLException, IOException {
        int idd = requestJson.getInt("id");
        UserData player = new UserData(idd, "", "", "", 0, false, false);
        int r = DataAccessObject.updateStatus(player);
        
        if(r != 0){
            online = false;
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response", "login")
                    .add("status", "success")
                    .build();
            mouth.writeUTF(responseJson.toString());
        } else {
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response", "login")
                    .add("status", "fail")
                    .build();
            mouth.writeUTF(responseJson.toString());
        }
    }

    private UserHandler getUser(int id) {
        System.out.println("server.UserHandler.getUser()");
        UserHandler userHandler = null;
        for (UserHandler user : UserHandlers) {
            if (user.id == id) {
                userHandler = user;
            }
        }
        return userHandler;
    }
    
    private void sendMove(JsonObject requestJson) throws IOException
    {
        System.out.println("server.UserHandler.sendMove()");
        int id2 = requestJson.getInt("player2Id");
        UserHandler player2 = getUser(id2);
        player2.mouth.writeUTF(requestJson.toString());
        mouth.flush();
    }
    
    
    private void record(JsonObject requestJson) throws SQLException, IOException {
        String recordname = requestJson.getString("recordName");
        int pid = requestJson.getInt("id");
        String moves = requestJson.getString("movesList");
        Records record=new Records(0,recordname,moves,pid);
        int added = DataAccessObject.addRecord(record);

        if (added > 0) {
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response", "record")
                    .add("status", "success")
                    .build();
            mouth.writeUTF(responseJson.toString());
        } else {
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response", "record")
                    .add("status", "fail")
                    .build();
            mouth.writeUTF(responseJson.toString());
        }

    }
     
    private void getAllRecords(JsonObject requestJson) throws SQLException, IOException {
        int idd = requestJson.getInt("id");
        JsonArrayBuilder usersBuilder = Json.createArrayBuilder();

        ArrayList<Records> records = DataAccessObject.getAllRecords(idd);
        for (Records player : records) {
            
                JsonObject user = Json.createObjectBuilder()
                    .add("id", player.getId())
                    .add("name", player.getName())
                    .add("steps", player.getSteps())
                    .build();
            usersBuilder.add(user);
            
        }
        JsonArray users = usersBuilder.build();
        JsonObject responseJson = Json.createObjectBuilder()
                .add("response", "allRecords")
                .add("status", "success")
                .add("records", users)
                .build();
        mouth.writeUTF(responseJson.toString());
        
    }

    private void sendPlayAgain(JsonObject requestJson) throws IOException {
        int myID = requestJson.getInt("myID");
        int id1 = requestJson.getInt("id1");
        int id2 = requestJson.getInt("id2");
        UserHandler player;
        
        if(id1 == myID)
            player = getUser(id2);
        else 
            player = getUser(id1);
        
        player.mouth.writeUTF(requestJson.toString());
        player.mouth.flush();
    }

    private void receivePlayAgain(JsonObject responseJson) throws IOException {
        int id1 = responseJson.getInt("id1");
        UserHandler player1 = getUser(id1);
        int id2 = responseJson.getInt("id2");
        UserHandler player2 = getUser(id2);
        
        if(! responseJson.getBoolean("status")){
            try {
                UserData user1 = new UserData(id1, "", "", "", responseJson.getInt("score1"), true, false);
                DataAccessObject.updateStatus(user1);
                DataAccessObject.updateScore(user1);
                player1.online = true;
                
                UserData user2 = new UserData(id2, "", "", "", responseJson.getInt("score2"), true, false);
                DataAccessObject.updateStatus(user2);
                DataAccessObject.updateScore(user2);
                player2.online = true;
            } catch (SQLException ex) {
                Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        int myID = responseJson.getInt("myID");
        UserHandler player;
        if(id1 == myID)
            player = getUser(id2);
        else 
            player = getUser(id1);
        
        player.mouth.writeUTF(responseJson.toString());
        player.mouth.flush();
    }
    
    private void sendCancel(JsonObject requestJson) throws IOException {
        int id1 = requestJson.getInt("id1");
        UserHandler player1 = getUser(id1);
        int id2 = requestJson.getInt("id2");
        UserHandler player2 = getUser(id2);
        
        try {
            UserData user1 = new UserData(id1, "", "", "", requestJson.getInt("score1"), true, false);
            DataAccessObject.updateStatus(user1);
            DataAccessObject.updateScore(user1);
            player1.online = true;

            UserData user2 = new UserData(id2, "", "", "", requestJson.getInt("score2"), true, false);
            DataAccessObject.updateStatus(user2);
            DataAccessObject.updateScore(user2);
            player2.online = true;
        } catch (SQLException ex) {
            Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        player2.mouth.writeUTF(requestJson.toString());
        player2.mouth.flush();
    }
}
