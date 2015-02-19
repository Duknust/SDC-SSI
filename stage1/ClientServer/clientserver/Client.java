
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
            Socket socket = new Socket("localhost", 12345);
            //Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
            
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter toServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter toConsole = new BufferedWriter(new OutputStreamWriter(System.out));
            
            while(true){
                String send = fromConsole.readLine();
                toServer.write(send+"\n");
                toServer.flush();
                System.out.println("--enviado--");
                String recv = fromServer.readLine();
                toConsole.write(recv);
                toConsole.flush();
            }
        
        } catch (IOException ex){
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
