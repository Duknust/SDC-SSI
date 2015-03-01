
package filecypher;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class FileCypher {

    //prog -genkey <keyfile>
    //prog -enc <keyfile> <infile> <outfile>
    //prog -dec <keyfile> <infile> <outfile>
    
    public static void main(String[] args) {
        String keyfile = null;
        String infile = null;
        String outfile = null;
        boolean inEnc = false;
        boolean inDec = false;
        switch(args[1]){
            case "-genkey":
                keyfile = genkey();
                break;
            case "-enc":
                keyfile = args[2];
                infile = args[3];
                outfile = args[4];
                inEnc = true;
                break;
            case "-dec":
                keyfile = args[2];
                infile = args[3];
                outfile = args[4];
                inDec = true;
                break;
            default:
                System.out.println("Something went wrong");
        }
        if (inEnc || inDec){
            byte [] key;
            if (inEnc){
                try {
                    key = readFromFile(keyfile).getBytes("UTF-8");
                    String clearText = readFromFile(infile);

                    Cipher rc4 = Cipher.getInstance("RC4");
                    SecretKeySpec rc4Key = new SecretKeySpec(key, "RC4");
                    rc4.init(Cipher.ENCRYPT_MODE, rc4Key);

                    byte[] cipherText = rc4.update(clearText.getBytes("UTF-8"));
                    
                    writeToFileByte(cipherText, outfile);

                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchPaddingException ex) {
                    Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidKeyException ex) {
                    Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            } else {
                try {
                    key = readFromFile(keyfile).getBytes("UTF-8"); 
                
                    Cipher rc4Decrypt = Cipher.getInstance("RC4");
                    SecretKeySpec rc4Key = new SecretKeySpec(key, "RC4");
                    rc4Decrypt.init(Cipher.DECRYPT_MODE, rc4Key);
                    
                    byte[] cipherText = readFromFile(infile).getBytes("UTF-8"); 
                    
                    byte[] clearText = rc4Decrypt.update(cipherText);
                    
                    writeToFileString(new String(clearText, "UTF-8"), outfile);
                    
                } catch (InvalidKeyException ex) {
                    Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchPaddingException ex) {
                    Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        }
    }
    

    private static String genkey() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static String readFromFile(String filename) throws FileNotFoundException, IOException {
        String everything = null;
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
        }
        return everything;
    }

    private static void writeToFileByte(byte[] cipherText, String outfile) {
        FileOutputStream fos = null;
        try {
            BufferedOutputStream bos = null;
            fos = new FileOutputStream(new File(outfile));
            bos = new BufferedOutputStream(fos);
            bos.write(cipherText);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void writeToFileString(String string, String outfile) {
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream(outfile), "utf-8"));
            writer.write(string);
        } catch (IOException ex) {
           Logger.getLogger(FileCypher.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           try {writer.close();} catch (Exception ex) {}
        }
    }
    
}
