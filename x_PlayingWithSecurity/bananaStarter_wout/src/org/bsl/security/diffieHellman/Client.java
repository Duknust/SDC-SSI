package org.bsl.security.diffieHellman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.bsl.security.diffieHellman.DiffieHellman;
import org.bsl.security.diffieHellman.SignatureKeypairGenerator;

public class Client {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 23456);
            //Socket socket = new Socket(args[0], Integer.parseInt(args[1]));

            BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter toServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter toConsole = new BufferedWriter(new OutputStreamWriter(System.out));

            DiffieHellman dh = new DiffieHellman();
            KeyPair mineKeys = dh.generateKeyPair(false);

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();

            //to Server (client writes first)
            KeyPair signatureKeys = SignatureKeypairGenerator.fromCertAndKey(s + "/certs/bananaStarterClient/client.pem", s + "/certs/bananaStarterClient/clientkey.der");
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, SignatureKeypairGenerator.fromCert(s + "/certs/bananaStarterServer/server.pem"));
            byte[] cipherDH = cipher.doFinal(mineKeys.getPublic().getEncoded());

            toServer.write(Base64.getEncoder().encodeToString(cipherDH) + "\n");
            toServer.flush();

            //from Server (now server writes)
            byte[] dhSignedBytes = Base64.getDecoder().decode(fromServer.readLine().trim());
            Cipher toDecrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            toDecrypt.init(Cipher.DECRYPT_MODE, signatureKeys.getPrivate());
            byte[] dhDecrypted = toDecrypt.doFinal(dhSignedBytes);

            //dh agreement
            KeyFactory keyFactDH = KeyFactory.getInstance("DH");
            X509EncodedKeySpec ks = new X509EncodedKeySpec(dhDecrypted);
            PublicKey otherPublicKey = keyFactDH.generatePublic(ks);
            SecretKey sessionKey = dh.getSessionKey(mineKeys.getPrivate(), otherPublicKey);

            byte[] iv = Base64.getDecoder().decode(fromServer.readLine());

            //read server public key (sig)
            KeyFactory keyFactRSA = KeyFactory.getInstance("RSA");
            PublicKey signatureServerPublicKey = keyFactRSA.generatePublic(
                    new X509EncodedKeySpec(
                            Base64.getDecoder().decode(
                                    fromServer.readLine())));

            //write to server publickey
            toServer.write(Base64.getEncoder().encodeToString(
                    new X509EncodedKeySpec(signatureKeys.getPublic().getEncoded()).getEncoded()) + "\n");
            toServer.flush();

            //create signature
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(signatureKeys.getPrivate());

            sig.update(signatureKeys.getPublic().getEncoded());
            sig.update(signatureServerPublicKey.getEncoded());

            //write signature of client public key and server public key
            toServer.write(Base64.getEncoder().encodeToString(dh.encryptMessage(sessionKey, sig.sign(), iv)) + "\n");
            toServer.flush();

            //read server signature
            byte[] sigServer = dh.decypherMessage(sessionKey, Base64.getDecoder().decode(fromServer.readLine()), iv);

            Signature sigFromServer = Signature.getInstance("SHA1withRSA");
            sigFromServer.initVerify(signatureServerPublicKey);
            sigFromServer.update(signatureServerPublicKey.getEncoded());
            sigFromServer.update(signatureKeys.getPublic().getEncoded());
            if (!sigFromServer.verify(sigServer)) {
                toServer.close();
                fromServer.close();
                System.err.println("[CLIENT] Wrong signature");
                System.exit(0);
            }
            boolean stop = false;
            while (!stop) {
                String rcv = fromConsole.readLine().trim();
                if (rcv.equals("stop")) {
                    stop = true;
                } else {
                    byte[] clientMessageBytes = dh.generateMAC(sessionKey, rcv.getBytes(), iv);
                    //dh.encryptMessage(sessionKey, rcv.getBytes("UTF-8"));
                    toServer.write(Base64.getEncoder().encodeToString(clientMessageBytes) + "\n");
                    //System.out.println(new String(clientMessageBytes, "UTF-8"));
                    toServer.flush();
                    System.out.println("[SYS-C] message sent");
                    //System.out.println(new String(clientMessageBytes) + "\n");

                }
            }
            toServer.close();

        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
