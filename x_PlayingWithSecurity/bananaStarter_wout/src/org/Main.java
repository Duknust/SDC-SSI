package org;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.classes.Project;
import org.servicos.ThreadClientListener;
import org.types.HashMapObs;
import org.types.Message;
import org.types.requests.ReqMapProj;
import org.types.requests.ReqProj;
import org.types.requests.ReqReqRetry;
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
    public static OutputStream os;
    public static ObjectOutputStream oos;
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
        tcl = new ThreadClientListener(s);
        tcl.start();
    }

    public static void startSocket() throws IOException {
        s = new Socket("localhost", 1337);
        os = s.getOutputStream();
        oos = new ObjectOutputStream(os);
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
        //Interface
        //Fazer o connect com o socket
        try {
            startSocket();
            active = 1;
        } catch (IOException ex) {
            active = 0;
        }
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

            try {
                oos.writeObject(p);
                System.out.println("envia " + p.getType());
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }

        }

    }
    /*
     public static synchronized int getinccodU() {
     codUtilizadores++;
     return codUtilizadores - 1;
     }

     public static synchronized int getinccodP() {
     codProjectos++;
     return codProjectos - 1;
     }

     public static void teste() {
     Utilizador u1 = new Utilizador("USER1", "PASS");
     Utilizador u2 = new Utilizador("USER2", "PASS");
     Utilizador u3 = new Utilizador("USER3", "PASS");
     p1 = new Project("Caneta de Chocolate", 500, 1, 10);

     Timer timer = new Timer();
     //
     //        timer.schedule(new TimerTask() {
     //
     //            @Override
     //            public void run() {
     //              Calendar date1 = Calendar.getInstance();
     //              if(p1.isFinalizado()==false)//JA ACABOU ?
     //                  System.out.println("date1:" + date1.getTime() + "p1:" + p1.getFim().getTime() + "\n" + date1.before(p1.getFim().getTime()));
     //                if(date1.getTimeInMillis()>p1.getFim().getTimeInMillis()){//SE DATA FIM > ACTUAL -> ACABAR
     //                    p1.setFinalizado(true);
     //                    System.out.println("P1-ACABOU!");
     //                    System.out.println(p1.toString());
     //                    this.cancel();//PARA O TIMER
     //                }
     //            }
     //          }, 1000,1000);

     System.out.println(u1.toString());
     System.out.println(u2.toString());
     System.out.println(u3.toString());
     System.out.println(p1.toString());

     while (p1.isFinalizado() == false) {
     System.out.println(p1.isFinalizado());
     Scanner keyboard = new Scanner(System.in);
     System.out.println(p1.printStatus());
     System.out.println("Quanto?:");
     int myint = keyboard.nextInt();
     if (p1.addEuros(myint)) {
     System.out.println("Adicionas-te " + myint + " €");
     } else {
     System.out.println("P1 já finalizado");
     }
     }

     }*/
}
