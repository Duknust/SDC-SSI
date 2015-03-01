//created by duknust
//find in https://github.com/Duknust

package clientserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientHandlerDump implements Runnable{
    Socket socket = null;
    int numSerie = -1;
    
    public ClientHandlerDump(Socket socket, int numSerie){
        this.socket = socket;
        this.numSerie = numSerie;
    }
    
    @Override
    public void run() {
        BufferedReader fromClient = null;
        BufferedWriter toClient = null;
        boolean closeConnection = false;
        try {
            fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            toClient = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            File file = new File("dump.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            while(!closeConnection){
                String line = fromClient.readLine();
                if(line.equals("closeconnection")){
                    closeConnection = true;
                    System.out.println("=["+numSerie+"]=");
                } else {
                    FileWriter fw = new FileWriter(file.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(this.numSerie+" "+line);
                    bw.flush();
                    this.numSerie++;
                }
            }
        } catch (IOException ex) {
            System.out.println("=["+numSerie+"]=");
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fromClient!=null)
                    fromClient.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}