/*
 * Esta thread vai ler da socket os dados enviados pelo server, ex: actualizar dados...
 */
package org.bsl.client;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.swing.JOptionPane;
import org.bsl.client.Main;
import org.bsl.security.diffieHellman.DiffieHellman;
import org.bsl.security.diffieHellman.exceptions.MessageNotAuthenticatedException;
import org.bsl.services.ClientHandler;
import org.bsl.types.Message;
import org.bsl.types.TypeOP;
import org.bsl.types.requests.ReqLogin;

public class ThreadClientListener extends Thread {

    private SecretKey sessionKey;
    private byte[] iv;
    private BufferedReader fromServer;
    private BufferedWriter toServer;
    private DiffieHellman dh;
    private XStream serializer;
    private boolean connected = false;
    private final Boolean userLogin;

    public ThreadClientListener(SecretKey sessionKey, byte[] iv, BufferedReader fromServer, BufferedWriter toServer, Boolean readyToLogin) {
        this.sessionKey = sessionKey;
        this.iv = iv;
        this.fromServer = fromServer;
        this.toServer = toServer;
        this.dh = new DiffieHellman();
        this.serializer = new XStream(new StaxDriver());
        this.serializer.processAnnotations(Message.class);
        this.userLogin = readyToLogin;
    }

    @Override
    public void run() {

        Message p;
        TypeOP tp;
        Message login = new ReqLogin(Main.user);
        Main.sendPackage(login);
        while (true) {
            p = null;
            tp = TypeOP.NULL;
            System.out.println("Waiting for stuffs");
            p = receiveMsg();
            System.out.println("Stuffs received");
            if (p != null) {
                System.out.println("To " + p.toString());
                tp = p.getType();
                switch (tp) {
                    case ADD_PROJECT:
                        System.out.println("From " + tp);
                        Main.mapProjects.vals.put(p.getProj().getName(), p.getProj());
                        break;
                    case REP_NAME_PROJ:
                        System.out.println("Received " + tp + " " + p.getValue1());
                        Main.reqProjectInt = p.getValue1();
                        Main.mapProjects.setProjectsList(p.getMp());
                        synchronized (Main.inter) {
                            Main.inter.notifyAll();
                        }
                        break;
                    case REP_MAP_PROJ:
                        System.out.println("Received " + tp);
                        Main.mapProjects.setProjectsList(p.getMp());
                        Main.actMapInt = 1;
                        break;

                    case REP_LOGIN:
                        System.out.println("Received " + tp);
                        synchronized (Main.start) {
                            Main.reqLoginInt = p.getValue1();
                            //Main.notify_interface();
                            Main.start.notifyAll();
                        }
                        break;

                    case REP_PROJ:
                        System.out.println("Received " + tp);
                        Main.reqProjectInt = p.getValue1();
                        synchronized (Main.inter) {
                            Main.inter.notifyAll();
                        }
                        break;

                    case ACT_PROJECT:
                        System.out.println("Received " + tp);
                        Main.mapProjects.setProj(p.getProj());

//                        Main.notify_interface();
                        break;

                    case REP_ADD_EUROS:
                        System.out.println("Received " + tp);
                        if (p.getValue1() == 1) {
                            JOptionPane.showMessageDialog(null, "Offered with success " + p.getValue2() + "€ to Project " + p.getString1());
                        } else if (p.getValue1() == -1) {
                            JOptionPane.showMessageDialog(null, "You can't offer money to your own projects");
                        } else {
                            JOptionPane.showMessageDialog(null, "There was an error to offer in Project " + p.getString1());
                        }
                        break;

                    case NOTIFEUROS:
                        System.out.println("Received " + tp);

                        Main.inter.addNotif(p);

                        break;
                    case REP_REGISTER:
                        System.out.println("Received " + tp);

                        Main.reqRegister = p.getValue1();
                        synchronized (Main.start) {
                            Main.start.notifyAll();
                        }
                        break;

                    case REP_WRONG_LOGIN:
                        System.out.println("Received " + tp);
                        JOptionPane.showMessageDialog(null, p.getString1());
                        break;

                }

            }
        }
    }

    public Message receiveMsg() {
        Message msg = null;
        try {
            String b64 = fromServer.readLine();
            byte[] rcvMsg = Base64.getDecoder().decode(b64);

            byte[] clientMessageBytes = this.dh.decodeMac(sessionKey, rcvMsg, iv);

            String toBeParsed = new String(clientMessageBytes, "UTF-8");

            msg = (Message) this.serializer.fromXML(toBeParsed);

        } catch (IOException | MessageNotAuthenticatedException ex) {
            Logger.getLogger(ClientHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return msg;
    }

    public Message sendMsg(Message msg) {
        try {
            String message = this.serializer.toXML(msg);

            byte[] clientMessageBytes = dh.generateMAC(sessionKey, message.getBytes(), iv);
            toServer.write(Base64.getEncoder().encodeToString(clientMessageBytes) + "\n");
            toServer.flush();

        } catch (IOException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
