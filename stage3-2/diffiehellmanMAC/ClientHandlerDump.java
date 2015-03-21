package diffiehellman;

import diffiehellman.exceptions.MessageNotAuthenticatedException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

public class ClientHandlerDump implements Runnable {

    Socket socket = null;
    int numSerie = -1;

    public ClientHandlerDump(Socket socket, int numSerie) {
        this.socket = socket;
        this.numSerie = numSerie;
    }

    @Override
    public void run() {
        System.out.println("[SYS-S] new connection handler");
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

            DiffieHellman dh = new DiffieHellman();
            KeyPair mineKeys = dh.generateKeyPair();

            String pubKeyMsg = fromClient.readLine().trim();
            byte[] otherPublicKeyBytes = Base64.getDecoder().decode(pubKeyMsg);
            toClient.write(Base64.getEncoder().encodeToString(mineKeys.getPublic().getEncoded()) + "\n");
            toClient.flush();

            KeyFactory keyFact = KeyFactory.getInstance("DH");
            X509EncodedKeySpec ks = new X509EncodedKeySpec(otherPublicKeyBytes);

            PublicKey otherPublicKey = keyFact.generatePublic(ks);

            SecretKey sessionKey = dh.getSessionKey(mineKeys.getPrivate(), otherPublicKey);

            while (!closeConnection) {

                String b64 = fromClient.readLine();
                byte[] rcv = Base64.getDecoder().decode(b64);
                System.out.println(new String(rcv));
                byte[] clientMessageBytes = dh.decodeMac(sessionKey, rcv);
//decypherMessage(sessionKey, Base64.getDecoder().decode(rcv));
                System.out.println("[SYS] Received message");
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(this.numSerie + " " + new String(clientMessageBytes, "UTF-8"));
                bw.flush();
                closeConnection = true;
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientHandlerDump.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("=[" + numSerie + "]=");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | MessageNotAuthenticatedException ex) {
            Logger.getLogger(ClientHandlerDump.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
