/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.services;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.security.diffiehellman.exceptions.MessageNotAuthenticatedException;
import org.security.diffiehellman.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import org.classes.OosUser;
import org.classes.Project;
import org.classes.User;
import org.security.diffiehellman.SignatureKeypairGenerator;
import org.types.Message;
import org.types.TypeOP;
import org.types.requests.ReqActProj;
import org.types.requests.ReqNotifEuros;
import org.types.responses.RepAddEuros;
import org.types.responses.RepLogin;
import org.types.responses.RepMapProj;
import org.types.responses.RepProj;
import org.types.responses.RepRegister;

public class ClientHandler extends Thread {

    public static ObjectOutputStream ous;
    private OosUser os;
    private XStream serializer = new XStream(new StaxDriver());
    private BufferedReader fromClient;
    private Socket socket;
    private BufferedWriter toClient;
    private SecretKey sessionKey;
    private byte[] iv;
    private DiffieHellman dh;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.serializer.processAnnotations(Message.class);
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

        try {
            ins = this.socket.getInputStream();
            outs = this.socket.getOutputStream();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        ObjectInputStream ois = null;
        ObjectOutputStream ous = null;

        try {
            ois = new ObjectInputStream(ins);
            ous = new ObjectOutputStream(outs);
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        os = new OosUser("", ous);
        Server.addoos(os);
        while (true) {

            received = null;
            response = null;
            type = TypeOP.NULL;

            try {
                System.out.println("/Waiting packages in Server");
                received = (Message) ois.readObject();
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
                            if (contem != null) {//existe
                                if (contem.equals(received.getUser()) == true) {//checka user e pass
                                    //resposta.criaREPLOGIN(1);
                                    response = new RepLogin(1);
                                    //Mensagem act = new Message();//Envia o map de projectos para encher o map do cliente
                                    HashMap<String, Project> map = new HashMap<>(Server.getMapProjects(0));
                                    Message act = new RepMapProj(map);
                                    //act.criaREPMAPPROJ(map);
                                    ous.writeObject(act);
                                    //Associa o OOS ao user
                                    myName = contem.getName();
                                    Server.addUser(ous, myName);//adiciona ao map dos pares OOS/user

                                } else {
                                    response = new RepLogin(0);
                                }
                            } else {
                                response = new RepLogin(2);
                            }

                            System.out.println("Server will send " + response.getType() + response.getValue1());
                            try {
                                ous.writeObject(response);
                            } catch (IOException i) {
                                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, i);
                            }
                            break;

                        case REQ_MAP_PROJ:

                            HashMap<String, Project> map = new HashMap<>(Server.getMapProjects(0));
                            response = new RepMapProj(map);

                            ous.writeObject(response);
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
                            ous.writeObject(response);

                            break;

                        case REQ_ADD_EUROS:

                            String projectName = received.getString1();
                            Project projReqAddE = Server.getProject(projectName);
                            if (projReqAddE != null)//se existe no map
                            {

                                int work = Server.addEurosProj(projectName, received.getString2(), received.getValue1());//Adiciona euros e historico
                                if (work > 0) {
                                    response = new RepAddEuros(1, received.getValue1(), projectName);
                                    Message p2 = new ReqActProj(projReqAddE.clone());

                                    Server.sendToEverybody(p2);
                                    ous.writeObject(response);

                                    Message p3 = new ReqNotifEuros(myName, projectName, received.getValue1());
                                    Server.sendToUser(p3, projReqAddE.getUser());
                                } else if (work == 0) {// NÃO EXISTE
                                    response = new RepAddEuros(0, 0, projectName);
                                    ous.writeObject(response);
                                } else {//-1 USER == DONO
                                    response = new RepAddEuros(-1, received.getValue1(), projectName);
                                    ous.writeObject(response);
                                }

                            } else {
                                response = new RepAddEuros(0, 0, projectName);
                                ous.writeObject(response);
                            }

                            break;

                        case REQ_REGISTER:

                            String nameRe = received.getString1();
                            String passRe = received.getString2();
                            User user = new User(nameRe, passRe);
                            boolean registered = Server.addUser(user);
                            if (registered == false)//nao existe
                            {
                                response = new RepRegister(nameRe, 1);
                            } else {
                                response = new RepRegister(nameRe, 0);
                            }

                            ous.writeObject(response);
                            break;

                        case REQ_RETRY:

                            String nameRetry = received.getString1();
                            Server.addUser(ous, nameRetry);
                            HashMap<String, Project> mapretry = new HashMap<>(Server.getMapProjects(0));

                            response = new RepMapProj(mapretry);

                            ous.writeObject(response);
                            break;

                    }

                    read = true;
                }
            } catch (IOException | RuntimeException | ClassNotFoundException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                Server.remove(os);
                break;

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

            //from Client (now server writes)
            String dhb64 = fromClient.readLine().trim();
            String dhsigb64 = fromClient.readLine().trim();
            byte[] dhBytes = Base64.getDecoder().decode(dhb64);
            byte[] dhSignedBytes = Base64.getDecoder().decode(dhsigb64);

            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(SignatureKeypairGenerator.getCert(s + "/certs/bananaStarterClient/client.pem"));
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

}
