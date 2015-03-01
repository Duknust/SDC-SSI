
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

            if (prop.getProperty("needsIV").equals("true")){
                needsIV=true;
            }

            String chave = fromClient.readLine().trim();

            byte[] iv = new byte[1024];

            if(needsIV){
                int test;
                int i=0;
                while ((test=fromClient.read()) != 0) {
                    iv[i++]=(byte)test;
                }
            }

            int test;
            while(!closeConnection){
                fromClientC = new CipherInputStream(socket.getInputStream(),Cipher.getInstance(ciph));
                toClientC = new CipherOutputStream(socket.getOutputStream(),Cipher.getInstance(ciph));

                byte[] message = new byte[1024];
                int counter=0;
                while ((test=fromClientC.read()) != -1) {
                    message[counter++]=(byte)test;
                }

                String line = Arrays.toString(message);
                System.out.println(line);

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
                byte[] original = cipher.doFinal(message);

                System.out.println(Arrays.toString(original));

                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(this.numSerie+" "+line);
                bw.flush();
                this.numSerie++;
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
