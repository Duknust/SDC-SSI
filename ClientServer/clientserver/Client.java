
package clientserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket(args[1], Integer.parseInt(args[2]));
            
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter toServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter toConsole = new BufferedWriter(new OutputStreamWriter(System.out));
            
            while(true){
                String send = fromConsole.readLine();
                toServer.write(send);
                toServer.flush();
                String recv = fromServer.readLine();
                toConsole.write(recv);
                toConsole.flush();
            }
            
        } catch (IOException ex){
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
