package diffiehellman;

import diffiehellman.exceptions.MessageNotAuthenticatedException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;

public class DiffieHellman {

    private boolean needsIV = false;

    public DiffieHellman() {
        try {
            Properties prop = new Properties();
            prop.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            String propertie = prop.getProperty("needsIV");
            this.needsIV = propertie.toLowerCase().equals("true");
        } catch (IOException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public KeyPair generateKeyPair() {
        String p = "99494096650139337106186933977618513974146274831566768179581759037259788798151499814653951492724365471316253651463342255785311748602922458795201382445323499931625451272600173180136123245441204133515800495917242011863558721723303661523372572477211620144038809673692512025566673746993593384600667047373692203583";
        String g = "44157404837960328768872680677686802650999163226766694797650810379076416463147265401084491113667624054557335394761604876882446924929840681990106974314935015501571333024773172440352475358750668213444607353872754650805031912866692119819377041901642732455911509867728218394542745330014071040326856846990119719675";

        BigInteger bP = new BigInteger(p);
        BigInteger bG = new BigInteger(g);

        KeyPair aPair = null;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
            DHParameterSpec dhps = new DHParameterSpec(bP, bG);
            keyGen.initialize(dhps, new SecureRandom());
            aPair = keyGen.generateKeyPair();

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }
        return aPair;
    }

    public byte[] generateMAC(SecretKey key, byte[] message, byte[] iv) {
        byte[] messageWithMAC = null;
        try {

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);
            byte[] macOfMessage = mac.doFinal(message);

            //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //outputStream.write(message);
            //outputStream.write(macOfMessage);
            byte[] encryptedMessage = this.encryptMessage(key, message, iv);

            //messageWithMAC = outputStream.toByteArray();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(encryptedMessage);
            outputStream.write(macOfMessage);

            messageWithMAC = outputStream.toByteArray();

        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }

        return messageWithMAC;
    }

    public byte[] decodeMac(SecretKey key, byte[] message, byte[] iv) throws MessageNotAuthenticatedException {
        byte[] decryptedMessage = null;
        try {
            //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //outputStream.write(message);
            //outputStream.write(macOfMessage);
            //messageWithMAC = outputStream.toByteArray();
            byte[] cleanMessage = new byte[message.length - 20];
            System.arraycopy(message, 0, cleanMessage, 0, message.length - 20);

            byte[] recvMac = new byte[20];
            System.arraycopy(message, message.length - 20, recvMac, 0, 20);
            System.out.println(message.length);
            System.out.println(cleanMessage.length);
            System.out.println(recvMac.length);

            decryptedMessage = this.decypherMessage(key, cleanMessage, iv);

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);
            byte[] calculatedMac = mac.doFinal(decryptedMessage);

            boolean res = Arrays.equals(recvMac, calculatedMac);
            if (res == false) {
                throw new MessageNotAuthenticatedException("Wrong mac");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }

        return decryptedMessage;
    }

    public SecretKey getSessionKey(Key myPrivate, Key otherPublic) {

        SecretKey secretKey = null;
        try {
            KeyAgreement aKeyAgree;
            aKeyAgree = KeyAgreement.getInstance("DH");
            aKeyAgree.init(myPrivate);
            aKeyAgree.doPhase(otherPublic, true);
            secretKey = aKeyAgree.generateSecret("AES");

        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }
        return secretKey;
    }

    public byte[] decypherMessage(SecretKey key, byte[] cypheredMessage, byte[] iv) {
        byte[] message = null;
        try {
            Cipher cipher = Cipher.getInstance("AES"); // AES/CBC/PKCS5Padding , AES/CBC/NoPadding , AES/CFB8/PKCS5Padding , AES/CFB8/NoPadding , AES/CFB/NoPadding
            if (this.needsIV) {
                IvParameterSpec ivect = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, key, ivect);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, key);
            }
            message = cipher.doFinal(cypheredMessage);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message;
    }

    public byte[] encryptMessage(SecretKey key, byte[] message, byte[] iv) {
        byte[] cyphered = null;
        try {
            Cipher cipher = Cipher.getInstance("AES"); // AES/CBC/PKCS5Padding , AES/CBC/NoPadding , AES/CFB8/PKCS5Padding , AES/CFB8/NoPadding , AES/CFB/NoPadding
            if (this.needsIV) {
                IvParameterSpec ivect = new IvParameterSpec(iv);
                cipher.init(Cipher.ENCRYPT_MODE, key, ivect);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            }
            cyphered = cipher.doFinal(message);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cyphered;
    }

    public byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return iv;
    }
}
