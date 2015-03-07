package diffiehellman;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class DiffieHellman {

    public KeyPair generateKeyPair() {
        String p = "99494096650139337106186933977618513974146274831566768179581759037259788798151499814653951492724365471316253651463342255785311748602922458795201382445323499931625451272600173180136123245441204133515800495917242011863558721723303661523372572477211620144038809673692512025566673746993593384600667047373692203583";
        String g = "44157404837960328768872680677686802650999163226766694797650810379076416463147265401084491113667624054557335394761604876882446924929840681990106974314935015501571333024773172440352475358750668213444607353872754650805031912866692119819377041901642732455911509867728218394542745330014071040326856846990119719675";

        BigInteger bP = new BigInteger(p);
        BigInteger bG = new BigInteger(g);

        KeyPair aPair = null;
        try {
            //DHGenParameterSpec dhParams = new DHGenParameterSpec(bP.intValueExact(), bG.intValueExact());
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");

            keyGen.initialize(1024, new SecureRandom());
            aPair = keyGen.generateKeyPair();

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }
        return aPair;
    }

    public SecretKey getSessionKey(Key myPrivate, Key otherPublic) {

        SecretKey secretKey = null;
        try {
            KeyAgreement aKeyAgree = null;
            aKeyAgree = KeyAgreement.getInstance("DH");
            aKeyAgree.init(myPrivate);
            aKeyAgree.doPhase(otherPublic, true);
            secretKey = aKeyAgree.generateSecret("AES");

            //secretKey = new SecretKeySpec(secretKeyBytes, "DH");
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }
        return secretKey;
    }

    public byte[] decypherMessage(SecretKey key, byte[] cypheredMessage) {
        byte[] message = null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            message = cipher.doFinal(cypheredMessage);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message;
    }

    public byte[] encryptMessage(SecretKey key, byte[] message) {
        byte[] cyphered = null;
        try {

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cyphered = cipher.doFinal(message);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cyphered;
    }
}
