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
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
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

            byte[] iv = dh.generateIV();

            toClient.write(Base64.getEncoder().encodeToString(iv) + "\n");
            toClient.flush();

            SignatureKeypairGenerator.toFile("server");
            KeyPair signatureKeys = SignatureKeypairGenerator.fromFile("server");
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(signatureKeys.getPrivate());

            KeyFactory keyFactRSA = KeyFactory.getInstance("RSA");

            //send public key to client (sig)
            toClient.write(Base64.getEncoder().encodeToString(
                    new X509EncodedKeySpec(signatureKeys.getPublic().getEncoded()).getEncoded()) + "\n");
            toClient.flush();

            //read client public key (sig)
            PublicKey signatureClientPublicKey = keyFactRSA.generatePublic(
                    new X509EncodedKeySpec(
                            Base64.getDecoder().decode(
                                    fromClient.readLine())));

            //read client signature
            byte[] sigClient = dh.decypherMessage(
                    sessionKey, Base64.getDecoder().decode(fromClient.readLine()), iv);

            sig.update(signatureKeys.getPublic().getEncoded());
            sig.update(signatureClientPublicKey.getEncoded());

            toClient.write(Base64.getEncoder().encodeToString(dh.encryptMessage(sessionKey, sig.sign(), iv)) + "\n");
            toClient.flush();

            Signature sigFromClient = Signature.getInstance("SHA1withRSA");
            sigFromClient.initVerify(signatureClientPublicKey);
            sigFromClient.update(signatureClientPublicKey.getEncoded());
            sigFromClient.update(signatureKeys.getPublic().getEncoded());
            if (!sigFromClient.verify(sigClient)) {
                toClient.close();
                fromClient.close();
                System.err.println("[SERVER] Wrong signature");
                System.exit(0);
            }

            while (!closeConnection) {

                String b64 = fromClient.readLine();
                byte[] rcv = Base64.getDecoder().decode(b64);
                System.out.println(new String(rcv));
                byte[] clientMessageBytes = dh.decodeMac(sessionKey, rcv, iv);
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
        } catch (InvalidKeyException ex) {
            Logger.getLogger(ClientHandlerDump.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(ClientHandlerDump.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
