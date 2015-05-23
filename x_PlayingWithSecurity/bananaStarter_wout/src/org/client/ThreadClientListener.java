/*
 * Esta thread vai ler da socket os dados enviados pelo server, ex: actualizar dados...
 */
package org.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import javax.swing.JOptionPane;
import org.client.Main;
import org.types.Message;
import org.types.TypeOP;

public class ThreadClientListener extends Thread {

    private Socket cli;

    public ThreadClientListener(Socket s) {
        this.cli = s;
    }

    @Override
    public void run() {
        InputStream ins = null;

        try {
            ins = cli.getInputStream();
            // System.out.println("3");
        } catch (IOException i) {
            System.out.println("IOE_" + i.toString());
        }

//        System.out.println("4");
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(ins);
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        Message p;
        TypeOP tp;
        while (true) {
            p = null;
            tp = TypeOP.NULL;
            try {
                System.out.println("Waiting for stuffs");
                p = (Message) ois.readObject();
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

                            //Main.notify_interface();
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

                    }

                }
//                System.out.println("3");
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("IOC_" + ex.toString());
                Main.stopConnection();
                break;
            }
        }

        //   out.close();
    }
}
