
package clientserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            //Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
            
            PropertiesHandler ph = new PropertiesHandler();
            Properties prop = ph.getProperties();
            String ciph = prop.getProperty("methodName");

            boolean needsIV = false;

            if (prop.getProperty("needsIV").equals("true")){
                needsIV=true;
            }
            
            BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter toServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        
            Cipher c = Cipher.getInstance(ciph);
            
            String chave = "HelloWorldMyNameIsSaulwonderfulx";
            byte[] keyBytes = chave.substring(0, 32).getBytes("UTF-8");
            SecretKeySpec key = new SecretKeySpec(keyBytes, ciph);
            
            byte[] iv = null;
            
            if (needsIV){
                iv = c.getIV();
                c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            } else {
                c.init(Cipher.ENCRYPT_MODE, key);
            }
            CipherInputStream fromServerC = new CipherInputStream(socket.getInputStream(), c);
            CipherOutputStream toServerC = new CipherOutputStream(socket.getOutputStream(), c);
            
            //BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter toConsole = new BufferedWriter(new OutputStreamWriter(System.out));
            
            toServer.write(chave);
            toServer.flush();
            System.out.println("key sent");
            
            if(needsIV){
                int test;
                int i=0;
                while((test=iv[i++])!=0) {
                    toServer.write(test);
                    toServer.flush();
                }   
                System.out.println("--IV sent--");
            }
            
            while(true){
                int test;
                while((test=System.in.read())!=-1) {
                    toServerC.write((byte)test);
                    toServerC.flush();
                }
                System.out.println("--message sent--");
            }
        
        } catch (IOException ex){
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }

}
