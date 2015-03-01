
package clientserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ClientHandlerDump implements Runnable{ 
    Socket socket = null;
    int numSerie = -1;

    public ClientHandlerDump(Socket socket, int numSerie){
        this.socket = socket;
        this.numSerie = numSerie;
    }

    @Override
    public void run() {
        System.out.println("[SYS-S] new connection handler");
        BufferedReader fromClient = null;
        BufferedWriter toClient = null;
        
        CipherInputStream fromClientC = null;
        CipherOutputStream toClientC = null;
        boolean closeConnection = false;
        try {
            fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            toClient = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            File file = new File("dump.txt");

            if (!file.exists()) {
                file.createNewFile();
            }

            PropertiesHandler ph = new PropertiesHandler();
            Properties prop = ph.getProperties();
            String ciph = prop.getProperty("methodName");

            boolean needsIV = false;

            if (prop.getProperty("needsIV").toLowerCase().equals("true")){
                needsIV=true;
            }
            
            String chave = fromClient.readLine().trim();
            System.out.println("[SYS-S] received key");
            
            byte[] iv = new byte[1024];

            if(needsIV){
                int test;
                int i=0;
                while ((test=fromClient.read()) != 0) {
                    iv[i++]=(byte)test;
                }
                System.out.println("[SYS-S] received IV");
            }
            
            int test;
            while(!closeConnection){
                
                SecretKeySpec skeySpec = new SecretKeySpec(chave.getBytes("UTF-8"),ciph);
                Cipher cipher = Cipher.getInstance(ciph);

                IvParameterSpec ivect=null;
                
                if (needsIV){
                    ivect = new IvParameterSpec(iv);
                    cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivect);
                }
                else {
                    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
                }
                
                fromClientC = new CipherInputStream(socket.getInputStream(),cipher);
                toClientC = new CipherOutputStream(socket.getOutputStream(),cipher);

                byte[] tmp = new byte[1024];
                int counter=0;
                boolean stop = false;
                while (counter<1024 && !stop) {
                    test=fromClientC.read();
                    if(test!=-1){
                        System.out.println("---"+test+"---");
                        tmp[counter++]=(byte)test;
                    } else 
                        stop=true;
                }
                fromClientC.close();
                System.out.println("[SYS-S] received message");

                byte[] message = new byte[counter];
                for(int i=0; i<counter; i++)
                    message[i]=tmp[i];
                
                //String line = Arrays.toString(message);
                //System.out.println("S----"+line+"-----");

                System.out.println("[SYS-S] clean message");
                byte[] original = cipher.doFinal(message);

                System.out.println(new String(message, "UTF-8"));

                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(this.numSerie+" "+new String(message, "UTF-8"));
                bw.flush();
                closeConnection=true;
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientHandlerDump.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("=["+numSerie+"]=");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ClientHandlerDump.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(ClientHandlerDump.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(ClientHandlerDump.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(ClientHandlerDump.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(ClientHandlerDump.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(ClientHandlerDump.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
