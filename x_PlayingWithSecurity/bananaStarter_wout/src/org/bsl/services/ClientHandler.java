/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsl.services;

import org.bsl.security.diffieHellman.DiffieHellman;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.bsl.security.diffieHellman.exceptions.MessageNotAuthenticatedException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import org.bsl.classes.OosUser;
import org.bsl.classes.Project;
import org.bsl.classes.User;
import org.bsl.client.Main;
import org.bsl.security.certValidator.CertValidator;
import org.bsl.security.diffieHellman.SignatureKeypairGenerator;
import org.bsl.types.Message;
import org.bsl.types.TypeOP;
import org.bsl.types.requests.ReqActProj;
import org.bsl.types.requests.ReqNotifEuros;
import org.bsl.types.responses.RepAddEuros;
import org.bsl.types.responses.RepWrongLogin;
import org.bsl.types.responses.RepLogin;
import org.bsl.types.responses.RepMapProj;
import org.bsl.types.responses.RepProj;
import sun.security.x509.X500Name;

public class ClientHandler extends Thread {

    //public static ObjectOutputStream ous;
    private OosUser os;
    private XStream serializer = new XStream(new StaxDriver());
    private BufferedReader fromClient;
    private Socket socket;
    private BufferedWriter toClient;
    private SecretKey sessionKey;
    private byte[] iv;
    private DiffieHellman dh;
    private X509Certificate clientCertificate = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.serializer.processAnnotations(Message.class);
    }

    public String getUser() {
        return this.os.getUser();
    }

    public void sendToOwner(Message m) {
        sendMsg(m);
    }

    @Override
    public void run() {
        this.connectClient();

        boolean read = false;

        InputStream ins = null;
        OutputStream outs = null;
        Message received = null;
        Message response = null;
        TypeOP type;
        String myName = "";

        os = new OosUser("", this.socket, this.sessionKey);
        Server.addoos(os);
        while (true) {

            received = null;
            response = null;
            type = TypeOP.NULL;

            try {
                System.out.println("/Waiting packages in Server");
                received = this.receiveMsg();
                //received = (Message) ois.readObject();
                System.out.println("|Reveived packages in Server");

                if (received != null) {
                    type = received.getType();
                    System.out.println("\\Server_Received " + type + "\n");
                    switch (type) {
                        case NULL:
                            break;
                        case REQ_LOGIN:
                            //checka se o user recebido é igual ao do map
                            User contem = Server.getUser(received.getUser().getName());
                            X500Name dn = (X500Name) this.clientCertificate.getSubjectDN();
                            if (!dn.getCommonName().equals(received.getUser().getName())) {
                                response = new RepWrongLogin("Wrong DN in certificate");
                                sendMsg(response);
                                this.fromClient.close();
                                this.toClient.close();
                                this.socket.close();
                                return;
                            }
                            if (contem != null) {//existe
                                if (contem.equals(received.getUser()) == true) {//checka user e pass
                                    //resposta.criaREPLOGIN(1);
                                    response = new RepLogin(1);
                                    HashMap<String, Project> map;
                                    map = new HashMap<>(Server.getMapProjects());
                                    Message act = new RepMapProj(map);
                                    //act.criaREPMAPPROJ(map);
                                    sendMsg(act);
                                    //ous.writeObject(act);
                                    //Associa o OOS ao user
                                    myName = contem.getName();
                                    Server.addUser(this.socket, myName);//adiciona ao map dos pares OOS/user

                                } else {
                                    response = new RepLogin(0);
                                }
                            } else {
                                response = new RepLogin(2);
                            }

                            System.out.println("Server will send " + response.getType() + response.getValue1());
                            sendMsg(response);
                            //ous.writeObject(response);
                            break;

                        case REQ_MAP_PROJ:

                            HashMap<String, Project> map = new HashMap<>(Server.getMapProjects());
                            response = new RepMapProj(map);

                            //ous.writeObject(response);
                            sendMsg(response);
                            break;

                        case REQ_PROJ:
                            String name = received.getProj().getName();
                            Project p = Server.getProject(name);
                            if (p == null)//não existe no map, então posso criar um
                            {
                                Project proj = received.getProj();
                                Server.addProject(proj);

                                response = new RepProj(1);
                                Message act = new ReqActProj(Server.getProject(name));
                                Server.sendToEverybody(act);
                            } else {
                                response = new RepProj(0);
                            }
                            sendMsg(response);
                            //ous.writeObject(response);

                            break;

                        case REQ_ADD_EUROS:

                            String projectName = received.getString1();
                            Project projReqAddE = Server.getProject(projectName);
                            if (projReqAddE != null)//se existe no map
                            {

                                int work = Server.addEurosProj(projectName, received.getString2(), received.getValue1());//Adiciona euros e historico
                                if (work > 0) {
                                    response = new RepAddEuros(1, received.getValue1(), projectName);
                                    Message p2 = new ReqActProj(Server.getProject(projectName));

                                    Server.sendToEverybody(p2);
                                    sendMsg(response);
                                    //ous.writeObject(response);

                                    Message p3 = new ReqNotifEuros(myName, projectName, received.getValue1());
                                    Server.sendToUser(p3, Server.getProject(projectName).getUser());
                                } else if (work == 0) {// NÃO EXISTE
                                    response = new RepAddEuros(0, 0, projectName);
                                    //ous.writeObject(response);
                                    sendMsg(response);
                                } else {//-1 USER == DONO
                                    response = new RepAddEuros(-1, received.getValue1(), projectName);
                                    //ous.writeObject(response);
                                    sendMsg(response);
                                }

                            } else {
                                response = new RepAddEuros(0, 0, projectName);
                                sendMsg(response);
                                //ous.writeObject(response);
                            }

                            break;

                        case REQ_RETRY:

                            String nameRetry = received.getString1();
                            //Server.addUser(ous, nameRetry); //deixa de ser assim
                            Server.addUser(this.socket, nameRetry); //tem de ser assim
                            HashMap<String, Project> mapretry = new HashMap<>(Server.getMapProjects());

                            response = new RepMapProj(mapretry);

                            sendMsg(response);
                            //ous.writeObject(response);
                            break;

                    }

                    read = true;
                }
            } catch (RuntimeException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                Server.remove(os);
                break;

            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void connectClient() {
        try {
            fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            toClient = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            dh = new DiffieHellman();
            KeyPair mineKeys = dh.generateKeyPair(true);

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
            KeyPair signatureKeys = SignatureKeypairGenerator.fromCertAndKey(s + "/certs/bananaStarterServer/server.pem", s + "/certs/bananaStarterServer/serverkey.der");

            X509Certificate certificate = (X509Certificate) SignatureKeypairGenerator.getCert(s + "/certs/bananaStarterServer/server.pem");
            CertValidator cv = new CertValidator();
            String certInString = fromClient.readLine();
            this.clientCertificate = cv.getCertFromString(Base64.getDecoder().decode(certInString));

            toClient.write(Base64.getEncoder().encodeToString(certificate.getEncoded()) + "\n");
            toClient.flush();

            //from Client (now server writes)
            String dhb64 = fromClient.readLine().trim();
            String dhsigb64 = fromClient.readLine().trim();
            byte[] dhBytes = Base64.getDecoder().decode(dhb64);
            byte[] dhSignedBytes = Base64.getDecoder().decode(dhsigb64);

            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(clientCertificate);
            sig.update(dhBytes);
            boolean validDHSig = sig.verify(dhSignedBytes);

            if (!validDHSig) {
                this.socket.close();
                return;
            }

            //to Client (client writes first)
            sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(signatureKeys.getPrivate());
            sig.update(mineKeys.getPublic().getEncoded());

            toClient.write(Base64.getEncoder().encodeToString(mineKeys.getPublic().getEncoded()) + "\n");
            toClient.write(Base64.getEncoder().encodeToString(sig.sign()) + "\n");
            toClient.flush();

            //dh agreement
            KeyFactory keyFact = KeyFactory.getInstance("DH");
            X509EncodedKeySpec ks = new X509EncodedKeySpec(dhBytes);

            PublicKey otherPublicKey = keyFact.generatePublic(ks);
            this.sessionKey = dh.getSessionKey(mineKeys.getPrivate(), otherPublicKey);

            iv = dh.generateIV();

            toClient.write(Base64.getEncoder().encodeToString(iv) + "\n");
            toClient.flush();

            //SignatureKeypairGenerator.toFile("server");
            //Path currentRelativePath = Paths.get("");
            //String s = currentRelativePath.toAbsolutePath().toString();
            //KeyPair signatureKeys = SignatureKeypairGenerator.fromCertAndKey(s + "/certs/bananaStarterServer/server.pem", s + "/certs/bananaStarterServer/serverkey.der");
            sig = Signature.getInstance("SHA1withRSA");
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
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateEncodingException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Message receiveMsg() {
        Message msg = null;
        try {
            String b64 = fromClient.readLine();
            byte[] rcvMsg = Base64.getDecoder().decode(b64);

            byte[] clientMessageBytes = this.dh.decodeMac(sessionKey, rcvMsg, iv);

            String toBeParsed = new String(clientMessageBytes, "UTF-8");

            msg = (Message) this.serializer.fromXML(toBeParsed);

        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessageNotAuthenticatedException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg;
    }

    public Message sendMsg(Message msg) {
        try {
            this.serializer = new XStream(new StaxDriver());
            this.serializer.processAnnotations(Message.class);
            String message = this.serializer.toXML(msg);

            byte[] clientMessageBytes = dh.generateMAC(sessionKey, message.getBytes(), iv);
            toClient.write(Base64.getEncoder().encodeToString(clientMessageBytes) + "\n");
            toClient.flush();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

}
