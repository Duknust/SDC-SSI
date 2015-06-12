package org.bsl.client;

import org.bsl.security.diffieHellman.SignatureKeypairGenerator;
import org.bsl.security.diffieHellman.DiffieHellman;
import org.bsl.security.diffieHellman.Client;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.bsl.classes.Project;
import org.bsl.security.diffieHellman.exceptions.MessageNotAuthenticatedException;
import org.bsl.services.ClientHandler;
import org.bsl.types.HashMapObs;
import org.bsl.types.Message;
import org.bsl.types.requests.ReqMapProj;
import org.bsl.types.requests.ReqProj;
import org.bsl.types.requests.ReqReqRetry;
import org.view.Start;
import org.view.InterfaceSD;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
public class Main {

    public static String format;
    public static HashMapObs mapProjects;
    public static Socket s;
    //public static OutputStream os;
    //public static ObjectOutputStream oos;
    public static String userloggedIn = "";
    public static Thread reqProjThread;
    public static Thread initDataThread;
    public static int reqProjectInt;
    public static int reqLoginInt;//0 Pass errada - 1 OK - 2 Não existe esse user
    public static int reqProjectNameInt;
    public static int actMapInt;
    public static int reqRegister;
    public static int active;
    public static ThreadClientListener tcl;
    public static InterfaceSD inter;
    public static Start start;

    private static PublicKey serverPublicKey;

    public static void notify_interface() {

        inter.notify();
    }

    public static synchronized void submitProject(String name, int goal, String description) throws IOException {

        Project p = new Project(name, goal, userloggedIn, description);

        //Mensagem pa = new Message();
        Message pa = new ReqProj(p.clone());
        //pa.criaREQPROJ(p.clone());
        sendPackage(pa);

        try {

            while (Main.reqProjectInt == -1) {
                synchronized (Main.inter) {
                    Main.inter.wait();
                }
            }
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        }

        if (reqProjectInt == 1) {
            JOptionPane.showMessageDialog(null, "Project was published");
            mapProjects.insertProject(p.clone());
        } else {
            JOptionPane.showMessageDialog(null, "There is a project with the same name");
        }

        Main.reqProjectInt = -1;

        //mapProjectos.insertProject(p);
    }

    public static void startMapProjects() {
        //Login feito entao actualizar os maps....

        //synchronized (initDataThread) {
        //Pedir os maps
        //Mensagem p = new Message();
        Message p = new ReqMapProj();
        //p.criaREQMAPPROJ();
        sendPackage(p);
//        initDataThread = new Thread();
//        initDataThread.start();
        try {
            while (Main.actMapInt == 0) {
                synchronized (Main.inter) {
                    Main.inter.wait();
                }
            }
            Main.actMapInt = 0;

        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        }
        // }
    }

    public static void startInterfaceSD() {

        inter = new InterfaceSD();
        inter.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        inter.setVisible(true);
        Main.startMapProjects();

    }

    public static void startThreadClient() {
        //Iniciar a thread de leitura do socket
        tcl = new ThreadClientListener(sessionKey, iv, fromServer, toServer);
        tcl.start();
    }

    public static void startSocket() throws IOException {
        s = new Socket("localhost", 1337);
        //os = s.getOutputStream();
        //oos = new ObjectOutputStream(os);
    }

    public static void stopConnection() {
        try {
            Main.inter.addNotif("-----------------------------------------");
        } catch (NullPointerException n) {
        }
        Main.active = 0;
        Main.stopThread();
        Main.stopSocket();
        JOptionPane j = null;
        int showOptionDialog = 0;//SIM
        while (showOptionDialog == 0) {
            showOptionDialog = j.showOptionDialog(Main.inter, "Connection was lost! Try again?", "Erro", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            if (showOptionDialog == 0) {//SIM
                Main.retryConnection();
            }

            if (Main.active == 1) {
                break;
            }
        }

        if (Main.active != 1) {

            System.exit(-1);
        }

    }

    private static void stopThread() {

        if (Main.tcl.isAlive()) {
            Main.tcl.interrupt();
        }
    }

    private static void stopSocket() {
        try {
            Main.s.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void retryConnection() {
        try {
            Main.startSocket();
            Main.active = 1;
        } catch (IOException ex) {
            Main.active = 0;
        }

        if (Main.active == 1)//esta ligado
        {
            Main.startThreadClient();
            //Mensagem p = new Message();
            Message p = new ReqReqRetry(userloggedIn);
            //p.criaREQRETRY(Main.userloggedIn);
            Main.sendPackage(p);

        }
    }
    private static byte[] iv;
    private static BufferedWriter toServer = null;
    private static BufferedReader fromServer = null;
    private static DiffieHellman dh;
    private static SecretKey sessionKey;

    public void addProject(Project p) {
        mapProjects.vals.put(p.getName(), p);
    }

    public static void main(String[] args) {
        reqProjectInt = -1;
        reqLoginInt = -1;
        reqProjectNameInt = -1;
        reqProjectInt = -1;
        actMapInt = -1;
        reqRegister = -1;
        s = connectServer("localhost", 1337);
        active = 1;
        if (active == 1) {
            startThreadClient();
        }

        //inicializar os dados
        format = "dd/MM/yyyy HH:mm:ss";

        mapProjects = new HashMapObs();

        start = new Start();
        start.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        start.setVisible(true);
    }

    public static void sendPackage(Message p) {//So envia pedidos sem dados

        if (p != null) {
            sendMsg(p);
            System.out.println("envia " + p.getType());

        }

    }

    public static Socket connectServer(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);

            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            toServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            dh = new DiffieHellman();
            KeyPair mineKeys = dh.generateKeyPair(true);

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
            KeyPair signatureKeys = SignatureKeypairGenerator.fromCertAndKey(s + "/certs/bananaStarterClient/client.pem", s + "/certs/bananaStarterClient/clientkey.der");

            //to Server (client writes first)
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(signatureKeys.getPrivate());
            sig.update(mineKeys.getPublic().getEncoded());
            byte[] signatureBytes = sig.sign();
            toServer.write(Base64.getEncoder().encodeToString(mineKeys.getPublic().getEncoded()) + "\n");
            toServer.write(Base64.getEncoder().encodeToString(signatureBytes) + "\n");
            toServer.flush();

            if (!socket.isConnected() || socket.isClosed()) {
                System.err.println("Unable to connect\n");
                return null;
            }

            //from Server (now server writes)
            String dhPubKey = fromServer.readLine().trim();
            String dhPubKeySignature = fromServer.readLine().trim();
            byte[] dhBytes = Base64.getDecoder().decode(dhPubKey);
            byte[] dhSignedBytes = Base64.getDecoder().decode(dhPubKeySignature);

            sig.initVerify(SignatureKeypairGenerator.getCert(s + "/certs/bananaStarterServer/server.pem"));
            sig.update(dhBytes);
            boolean validDHSig = sig.verify(dhSignedBytes);
            if (!validDHSig) {
                toServer.close();
                socket.close();
                return null;
            }

            KeyFactory keyFactDH = KeyFactory.getInstance("DH");
            X509EncodedKeySpec ks = new X509EncodedKeySpec(dhBytes);

            PublicKey otherPublicKey = keyFactDH.generatePublic(ks);
            sessionKey = dh.getSessionKey(mineKeys.getPrivate(), otherPublicKey);

            iv = Base64.getDecoder().decode(fromServer.readLine());

            //Path currentRelativePath = Paths.get("");
            //String s = currentRelativePath.toAbsolutePath().toString();
            //KeyPair signatureKeys = SignatureKeypairGenerator.fromCertAndKey(s + "/certs/bananaStarterClient/client.pem", s + "/certs/bananaStarterClient/clientkey.der");
            //read server public key (sig)
            KeyFactory keyFactRSA = KeyFactory.getInstance("RSA");
            PublicKey signatureServerPublicKey = keyFactRSA.generatePublic(
                    new X509EncodedKeySpec(
                            Base64.getDecoder().decode(
                                    fromServer.readLine())));

            //write to server publickey
            toServer.write(Base64.getEncoder().encodeToString(
                    new X509EncodedKeySpec(signatureKeys.getPublic().getEncoded()).getEncoded()) + "\n");
            toServer.flush();

            //create signature
            sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(signatureKeys.getPrivate());

            sig.update(signatureKeys.getPublic().getEncoded());
            sig.update(signatureServerPublicKey.getEncoded());

            //write signature of client public key and server public key
            toServer.write(Base64.getEncoder().encodeToString(dh.encryptMessage(sessionKey, sig.sign(), iv)) + "\n");
            toServer.flush();

            //read server signature
            byte[] sigServer = dh.decypherMessage(sessionKey, Base64.getDecoder().decode(fromServer.readLine()), iv);

            Signature sigFromServer = Signature.getInstance("SHA1withRSA");
            sigFromServer.initVerify(signatureServerPublicKey);
            sigFromServer.update(signatureServerPublicKey.getEncoded());
            sigFromServer.update(signatureKeys.getPublic().getEncoded());
            if (!sigFromServer.verify(sigServer)) {
                toServer.close();
                fromServer.close();
                System.err.println("[CLIENT] Wrong signature");
                System.exit(0);
            }

            serverPublicKey = signatureServerPublicKey;

        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return socket;
    }

    public static Message sendMsg(Message msg) {
        try {
            XStream serializer = new XStream(new StaxDriver());
            serializer.processAnnotations(Message.class);
            String message = serializer.toXML(msg);

            byte[] clientMessageBytes = dh.generateMAC(sessionKey, message.getBytes(), iv);
            toServer.write(Base64.getEncoder().encodeToString(clientMessageBytes) + "\n");
            toServer.flush();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Message receiveMsg() {
        Message msg = null;
        try {
            XStream serializer = new XStream(new StaxDriver());

            String b64 = fromServer.readLine();
            byte[] rcvMsg = Base64.getDecoder().decode(b64);

            byte[] clientMessageBytes = this.dh.decodeMac(sessionKey, rcvMsg, iv);

            String toBeParsed = new String(clientMessageBytes, "UTF-8");

            msg = (Message) serializer.fromXML(toBeParsed);

        } catch (IOException | MessageNotAuthenticatedException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg;
    }
}
