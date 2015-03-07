package diffiehellman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;

public class Client {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            //Socket socket = new Socket(args[0], Integer.parseInt(args[1]));

            BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter toServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter toConsole = new BufferedWriter(new OutputStreamWriter(System.out));

            DiffieHellman dh = new DiffieHellman();
            KeyPair mineKeys = dh.generateKeyPair();

            toServer.write(Base64.getEncoder().encodeToString(mineKeys.getPublic().getEncoded()) + "\n");
            toServer.flush();
            byte[] otherPublicKeyBytes = Base64.getDecoder().decode(fromServer.readLine().trim());

            KeyFactory keyFact = KeyFactory.getInstance("DH");
            X509EncodedKeySpec ks = new X509EncodedKeySpec(otherPublicKeyBytes);

            PublicKey otherPublicKey = keyFact.generatePublic(ks);

            SecretKey sessionKey = dh.getSessionKey(mineKeys.getPrivate(), otherPublicKey);

            boolean stop = false;
            while (!stop) {
                String rcv = fromConsole.readLine().trim();
                if (rcv.equals("stop")) {
                    stop = true;
                } else {
                    byte[] clientMessageBytes = dh.encryptMessage(sessionKey, rcv.getBytes("UTF-8"));
                    toServer.write(Base64.getEncoder().encodeToString(clientMessageBytes) + "\n");
                    toServer.flush();
                    System.out.println("[SYS-C] message sent");
                }
            }
            toServer.close();

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
