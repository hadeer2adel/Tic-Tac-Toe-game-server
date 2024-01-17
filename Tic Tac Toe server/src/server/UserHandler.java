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
        while (true) {
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

            } catch (IOException ex) {
                try {
                    online = false;
                    UserHandlers.remove(this);
                    clientSocket.close();
                    ear.close();
                    mouth.close();
                    Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex1) {
                    Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
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
                    .add("email", player.getEmail())
                    .add("password", player.getPassword())
                    .add("score", player.getScore())
                    .add("is_available", player.getIs_available())
                    .add("is_onGame", player.getIs_onGame())
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
        int id = requestJson.getInt("id");
        JsonArrayBuilder usersBuilder = Json.createArrayBuilder();

        ArrayList<UserData> onlinePlayers = DataAccessObject.getAvailableUser();
        for (UserData player : onlinePlayers) {
            if(id != player.getId()){
                JsonObject user = Json.createObjectBuilder()
                    .add("id", player.getId())
                    .add("name", player.getName())
                    .add("score", player.getScore())
                    .build();
            usersBuilder.add(user);
            }
        }
        if(!onlinePlayers.isEmpty()){
            JsonArray users = usersBuilder.build();
            JsonObject responseJson = Json.createObjectBuilder()
                    .add("response", "availablePlayers")
                    .add("status", "success")
                    .add("players", users)
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

    private void sendInvitation(JsonObject requestJson) throws IOException {
        System.out.println("server.UserHandler.sendInvitation()");
        int id2 = requestJson.getInt("id2");
        UserHandler player2 = getUser(id2);
        player2.mouth.writeUTF(requestJson.toString());
        mouth.flush();
    }

    private void receiveInvitation(JsonObject responseJson) throws IOException {
        System.out.println("server.UserHandler.receiveInvitation()");
        int id1 = responseJson.getInt("id1");
        UserHandler player1 = getUser(id1);
        player1.mouth.writeUTF(responseJson.toString());
        mouth.flush();
    }

    private UserHandler getUser(int id) {
        System.out.println("server.UserHandler.getUser()");
        UserHandler userHandler = null;
        for (UserHandler user : UserHandlers) {
            if (user.online && user.id == id) {
                userHandler = user;
            }
        }
        return userHandler;
    }
}
