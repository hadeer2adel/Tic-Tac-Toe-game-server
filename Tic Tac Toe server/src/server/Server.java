package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable{
    
    private Thread thread;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int port = 5005;
    private static boolean running;

    public boolean isRunning() {
        return running;
    }

    public Server(){
        try{
            serverSocket = new ServerSocket(port);
            running = true;
            thread = new Thread(this);
            thread.start();
        } catch(IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stopServer() {
        running = false;
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
                thread.stop();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        UserHandler.stopAll();
    }

    @Override
    public void run() {
        while(running){
            try {
                clientSocket = serverSocket.accept();
                new UserHandler(clientSocket);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
