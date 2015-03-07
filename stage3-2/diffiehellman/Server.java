package diffiehellman;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    static int numSerie = 0;
    
    //public static void main(int argc, String[] args) {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(12345);
            //ServerSocket ss = new ServerSocket(Integer.parseInt(args[0]));
            System.out.println("[SYS-S] Server started");
            while(true){
                Socket socket = ss.accept();
                ClientHandlerDump ch = new ClientHandlerDump(socket, numSerie);
                new Thread(ch).start();
                numSerie++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
