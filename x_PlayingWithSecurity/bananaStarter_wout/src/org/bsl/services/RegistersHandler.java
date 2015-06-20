/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsl.services;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.bsl.classes.User;
import org.bsl.security.diffieHellman.SignatureKeypairGenerator;
import org.bsl.types.Message;
import org.bsl.types.requests.ReqRegister;
import org.bsl.types.responses.RepRegister;

/**
 *
 * @author duarteduarte
 */
class RegistersHandler implements Runnable {

    private final ServerSocket ssRegisters;
    private BufferedReader br = null;
    private BufferedWriter bw = null;
    private X509Certificate certificate = null;
    private KeyPair signatureKeys = null;
    private final String certificateToSend;
    private final XStream serializer;

    RegistersHandler(ServerSocket ssRegisters) throws CertificateEncodingException {
        this.ssRegisters = ssRegisters;

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        this.certificate = (X509Certificate) SignatureKeypairGenerator.getCert(s + "/certs/bananaStarterServer/server.pem");
        this.certificateToSend = Base64.getEncoder().encodeToString(this.certificate.getEncoded());

        this.signatureKeys = SignatureKeypairGenerator.fromCertAndKey(s + "/certs/bananaStarterServer/server.pem", s + "/certs/bananaStarterServer/serverkey.der");
        this.serializer = new XStream(new StaxDriver());
        this.serializer.processAnnotations(Message.class);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = ssRegisters.accept();
                this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                this.bw.write(this.certificateToSend + "\n");
                this.bw.flush();

                String keyStringB64 = this.br.readLine();
                String request = this.br.readLine();

                byte[] keyStringBytes = Base64.getDecoder().decode(keyStringB64);

                Cipher toDecrypt = Cipher.getInstance("RSA");
                toDecrypt.init(Cipher.DECRYPT_MODE, this.signatureKeys.getPrivate());
                byte[] simetricKey = toDecrypt.doFinal(keyStringBytes);

                SecretKeySpec sks = new SecretKeySpec(simetricKey, "AES");

                toDecrypt = Cipher.getInstance("AES");
                toDecrypt.init(Cipher.DECRYPT_MODE, sks);
                byte[] byteFromMessage = Base64.getDecoder().decode(request);
                byte[] messageDecrypted = toDecrypt.doFinal(byteFromMessage);
                ByteArrayInputStream bais = new ByteArrayInputStream(messageDecrypted);
                ReqRegister msg = (ReqRegister) serializer.fromXML(bais);

                String nameRe = msg.getString1();
                String passRe = msg.getString2();
                User user = new User(nameRe, passRe);
                boolean registered = Server.addUser(user);
                Message response;
                if (registered) {
                    response = new RepRegister(nameRe, 0);
                } else {
                    response = new RepRegister(nameRe, 1);
                }

                bw.write(this.serializer.toXML(response) + "\n");
                bw.flush();
                bw.close();
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(RegistersHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(RegistersHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchPaddingException ex) {
                Logger.getLogger(RegistersHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeyException ex) {
                Logger.getLogger(RegistersHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalBlockSizeException ex) {
                Logger.getLogger(RegistersHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BadPaddingException ex) {
                Logger.getLogger(RegistersHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
