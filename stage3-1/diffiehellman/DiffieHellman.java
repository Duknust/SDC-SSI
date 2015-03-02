
package diffiehellman;

import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHGenParameterSpec;

public class DiffieHellman {
    public static void main(String[] args) {
        String p = "99494096650139337106186933977618513974146274831566768179581759037259788798151499814653951492724365471316253651463342255785311748602922458795201382445323499931625451272600173180136123245441204133515800495917242011863558721723303661523372572477211620144038809673692512025566673746993593384600667047373692203583";
        String g = "44157404837960328768872680677686802650999163226766694797650810379076416463147265401084491113667624054557335394761604876882446924929840681990106974314935015501571333024773172440352475358750668213444607353872754650805031912866692119819377041901642732455911509867728218394542745330014071040326856846990119719675";
        
        BigInteger bP = new BigInteger(p);
        BigInteger bG = new BigInteger(g);
        
        try {
            DHGenParameterSpec dhParams = new DHGenParameterSpec(bP.intValueExact(), bG.intValueExact());
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH","BC");
            
            keyGen.initialize(dhParams, new SecureRandom());
            
            KeyAgreement aKeyAgree = KeyAgreement.getInstance("DH","BC");
            KeyPair aPair = keyGen.generateKeyPair();
            KeyAgreement bKeyAgree = KeyAgreement.getInstance("DH","BC");
            KeyPair bPair = keyGen.generateKeyPair();
            
            aKeyAgree.init(aPair.getPrivate());
            bKeyAgree.init(bPair.getPrivate());
            
            aKeyAgree.doPhase(bPair.getPublic(), true);
            bKeyAgree.doPhase(aPair.getPublic(), true);
            
            MessageDigest hash = MessageDigest.getInstance("SHA1", "BC");
            System.out.println(new String(hash.digest(aKeyAgree.generateSecret())));
            System.out.println(new String(hash.digest(bKeyAgree.generateSecret())));
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(DiffieHellman.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
