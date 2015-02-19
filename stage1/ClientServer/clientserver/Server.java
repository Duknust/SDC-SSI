package clientserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    static int numSerie = 0;
    
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(12345);
            //ServerSocket ss = new ServerSocket(Integer.parseInt(args[0]));
            while(true){
                Socket socket = ss.accept();
                ClientHandler ch = new ClientHandler(socket, numSerie);
                new Thread(ch).start();
                numSerie++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
