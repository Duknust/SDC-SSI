/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsl.security.diffieHellman;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bsl.security.certValidator.CertValidator;

/**
 *
 * @author duarteduarte
 */
public class SignatureKeypairGenerator {

    public static void main(String[] args) throws Exception {
        //String filename = args[0];
        String filename = "ze";
        if (fromFile(filename) == null) {
            throw new Exception("Ai o crlh!");
        }
    }

    private static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static KeyPair fromFile(String filename) {
        KeyPair kp = null;
        try {
            byte[] pubKeyBytes = Base64.getDecoder().decode(readFile(filename, Charset.forName("UTF-8")));
            byte[] privKeyBytes = Base64.getDecoder().decode(readFile(filename, Charset.forName("UTF-8")));

            X509EncodedKeySpec encodedpks = new X509EncodedKeySpec(pubKeyBytes);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pubk = kf.generatePublic(encodedpks);

            PKCS8EncodedKeySpec encpriv = new PKCS8EncodedKeySpec(privKeyBytes);

            PrivateKey privk = kf.generatePrivate(encpriv);

            kp = new KeyPair(pubk, privk);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SignatureKeypairGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(SignatureKeypairGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return kp;
    }

    public static KeyPair fromCertAndKey(String certFilename, String keyFilename) {

        KeyPair kp = null;

        CertValidator cv = new CertValidator();
        X509Certificate clientCert = cv.getCertFromFile(certFilename);
        PrivateKey clientPrivKey = (PrivateKey) cv.getKeyFromFile(keyFilename);
        PublicKey clientPubKey = clientCert.getPublicKey();
        kp = new KeyPair(clientPubKey, clientPrivKey);

        return kp;
    }

    public static PublicKey fromCert(String certFilename) {
        PublicKey pk = null;

        CertValidator cv = new CertValidator();
        X509Certificate clientCert = cv.getCertFromFile(certFilename);
        pk = clientCert.getPublicKey();

        return pk;
    }

    public static Certificate getCert(String certFilename) {

        CertValidator cv = new CertValidator();
        X509Certificate clientCert = cv.getCertFromFile(certFilename);
        return clientCert;
    }
}
