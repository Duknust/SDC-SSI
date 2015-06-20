package org.bsl.security.certValidator;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

public class CertValidator {

    public static void main(String[] args) {
        CertValidator cv = new CertValidator();
        // X509Certificate caCert = cv.getCertFromFile("resources/cacert.pem");
        // X509Certificate clientCert = cv
        // .getCertFromFile("resources/client_cert.pem");
        if (args.length < 2) {
            System.err.println("Please enter two files");
            return;
        } else {
            X509Certificate caCert = cv.getCertFromFile(args[0]);
            X509Certificate clientCert = cv.getCertFromFile(args[1]);

            System.out.println(cv.certValidate(clientCert, caCert));
            System.out.println(cv.certValidate(caCert, clientCert));
            System.out.println(cv.certValidate_pubkey(caCert, clientCert));
        }
    }

    public X509Certificate getCertFromFile(String filename) {
        X509Certificate cert = null;
        try {
            /*
             * BufferedReader br = new BufferedReader(new FileReader(filename));
             * StringBuilder sb = new StringBuilder(); boolean startingToAppend
             * = false; String line = br.readLine();
             *
             * while (line != null) { if (line.contains("END")) {
             * startingToAppend = false; } if (startingToAppend) {
             * sb.append(line); sb.append(System.lineSeparator()); } if
             * (line.contains("BEGIN")) { startingToAppend = true; } line =
             * br.readLine(); } String everything = sb.toString();
             *
             *
             * byte[] encodedKey = everything.getBytes();
             */
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            cert = (X509Certificate) cf
                    .generateCertificate(new FileInputStream(filename));

        } catch (IOException | CertificateException e) {
            e.printStackTrace();
        }
        return cert;
    }

    public X509Certificate getCertFromString(byte[] pem) {
        X509Certificate cert = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            cert = (X509Certificate) cf
                    .generateCertificate(new ByteArrayInputStream(pem));

        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return cert;
    }

    public Key getKeyFromFile(String filename) {
        RSAPrivateKey privKey = null;
        try {
            FileInputStream fis = new FileInputStream(filename);
            byte[] all = new byte[1024];

            fis.read(all);

            byte[] encodedKey = all;
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            e.printStackTrace();
        }
        return privKey;
    }

    public boolean certValidate(X509Certificate caCert,
            X509Certificate clientCert) {
        boolean res = false;
        try {
            clientCert.checkValidity(new Date());
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            return false;
        }

        byte[] caSig = clientCert.getSignature();
        try {
            Signature sig = Signature.getInstance(clientCert.getSigAlgName());
            sig.initVerify(caCert.getPublicKey());
            sig.update(clientCert.getTBSCertificate());
            res = sig.verify(clientCert.getSignature());

        } catch (NoSuchAlgorithmException | CertificateEncodingException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return res;
    }

    public boolean certValidate_pubkey(X509Certificate caCert,
            X509Certificate clientCert) {
        try {
            clientCert.checkValidity(new Date());
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            return false;
        }

        try {
            clientCert.verify(caCert.getPublicKey());
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | CertificateException | NoSuchProviderException e) {
            return false;
        }

        return true;
    }
}
