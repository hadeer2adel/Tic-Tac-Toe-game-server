package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    
    ServerSocket serverSocket;
    Socket socket;
    int port = 5005;
    
    public Server()
    {
        try{
            serverSocket = new ServerSocket(port);
            while(true)
            {
                socket = serverSocket.accept();
                new UserHandler(socket);
            }
            
        } catch(IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {
        new Server();
    }
    
}
