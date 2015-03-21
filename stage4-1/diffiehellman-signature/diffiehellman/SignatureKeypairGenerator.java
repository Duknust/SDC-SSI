/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diffiehellman;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64DecoderStream;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64EncoderStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author duarteduarte
 */
public class SignatureKeypairGenerator {

    public static void main(String[] args) throws Exception {
        toFile(args[0]);
        if (fromFile(args[0]) == null) {
            throw new Exception("Ai o crlh!");
        }
    }

    public static void toFile(String filename) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.generateKeyPair();

            X509EncodedKeySpec eksPub = new X509EncodedKeySpec(kp.getPublic().getEncoded());
            PKCS8EncodedKeySpec eksPriv = new PKCS8EncodedKeySpec(kp.getPrivate().getEncoded());

            FileOutputStream fosPub = new FileOutputStream(filename + ".pub");
            FileOutputStream fosPriv = new FileOutputStream(filename + ".priv");

            OutputStream osEncoded = new BASE64EncoderStream(fosPub);
            osEncoded.write(eksPub.getEncoded());
            fosPub.flush();
            fosPub.close();

            osEncoded = new BASE64EncoderStream(fosPriv);
            osEncoded.write(eksPriv.getEncoded());
            osEncoded.flush();
            fosPriv.close();

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SignatureKeypairGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SignatureKeypairGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SignatureKeypairGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static KeyPair fromFile(String filename) {
        KeyPair kp = null;
        try {
            FileInputStream fisPub = new FileInputStream(filename + ".pub");
            FileInputStream fisPriv = new FileInputStream(filename + ".priv");

            byte[] buffer = new byte[1024];

            InputStream osDecoder = new BASE64DecoderStream(fisPub);
            osDecoder.read(buffer);
            fisPub.close();
            X509EncodedKeySpec encodedpks = new X509EncodedKeySpec(buffer);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pubk = kf.generatePublic(encodedpks);

            buffer = new byte[1024];
            osDecoder = new BASE64DecoderStream(fisPriv);
            osDecoder.read(buffer);
            fisPriv.close();
            PKCS8EncodedKeySpec encpriv = new PKCS8EncodedKeySpec(buffer);

            PrivateKey privk = kf.generatePrivate(encpriv);

            kp = new KeyPair(pubk, privk);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SignatureKeypairGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(SignatureKeypairGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return kp;
    }
}
