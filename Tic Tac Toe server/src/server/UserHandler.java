package server;

import DTO.UserData;
import DataBase.DataAccessObject;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UserHandler extends Thread
{
    private static Vector<UserHandler> UserHandlers = new Vector<>();
    private static ArrayList<UserData> onlinePlayers = new ArrayList<>();
    
    private UserData player;
    private String ip;
    private Socket userSocket;
    private BufferedReader br;
    private PrintWriter pw;
        
    public UserHandler(Socket cs) {
        try {
            ip = cs.getInetAddress().getHostAddress();
            userSocket = cs;
            br = new BufferedReader(new InputStreamReader(cs.getInputStream()));   
            pw = new PrintWriter(cs.getOutputStream(), true);
            
            UserHandlers.add(this);
            
            onlinePlayers = DataAccessObject.getAvailableUser();
            start();
        } catch (IOException ex) {
            Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run()
    {
        while(true)
        {
            try {
                String message = br.readLine();
                if(message != null)
                    System.out.println("Received from client : " + message);
                
            } catch (IOException ex) {
                try {
                    userSocket.close();
                    br.close();
                    pw.close();
                } catch (IOException ex1) {
                    Logger.getLogger(UserHandler.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }
}