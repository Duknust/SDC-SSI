package org.bsl.services;

import java.io.IOException;
import java.util.ArrayList;
import org.bsl.classes.OosUser;
import org.bsl.classes.Project;
import org.bsl.types.Message;
import com.mongodb.client.MongoCollection;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bsl.classes.User;
import org.dao.ProjectDAO;
import org.dao.UserDAO;
import org.dao.connection.MongoConnection;
import org.dao.exceptions.GenericDAOException;

public class Server {

    public static ArrayBlockingQueue<Project> mpQueue = new ArrayBlockingQueue<>(20, true);

    public static MongoCollection uz;
    public static MongoCollection mp;
    public static ArrayList<OosUser> listOosUser;
    public static ArrayList<ClientHandler> clientHandlers;

    public static void pr(String s) {

        System.out.println(s);
    }

    public static void remove(OosUser u) {

        listOosUser.remove(u);
    }

    public static void sendToEverybody(Message p) {

        for (ClientHandler ch : clientHandlers) {
            ch.sendMsg(p);

        }

    }

    public static void sendToUser(Message p, String user) {

        for (ClientHandler ch : clientHandlers) {

            if (ch.getUser().equals(user)) {
                ch.sendToOwner(p);
            }

        }

    }

    public static void removeOosUser(OosUser s) {

        listOosUser.remove(s);

    }

    public static synchronized void addoos(OosUser s) {

        listOosUser.add(s);
    }

    public static synchronized void addUser(Socket o, String user) {

        for (OosUser s : listOosUser) {

            if (s.getSocket().equals(o)) {

                s.setUser(user);

            }

        }
    }

    public static void main(String[] args) {

        try {

            //mp = new HashMap<>();
            //uz = new HashMap<>();
            MongoConnection mc = new MongoConnection("projectWithSecurity");
            uz = mc.getCollection("users");
            mp = mc.getCollection("projects");
            clientHandlers = new ArrayList<>();

            ProjectDAO projectDAO = new ProjectDAO();
            for (Object obj : projectDAO.find(mp, null)) {
                Project project = (Project) obj;
                mpQueue.add(project);
            }

            listOosUser = new ArrayList<>();

            int port = 1337;
            System.out.println("Connection to port " + port + ", wait  ...");
            ServerSocket ss = null;

            try {
                ss = new ServerSocket(port);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Server is online: " + ss);

            Thread t = new Thread() {
//Isto é que mal começa uma thread nunca mais funca, o save só é feito quando há um disconnect
                @Override
                public void run() {
                    Scanner s = new Scanner(System.in);
                    String str;
                    boolean turnOff = false;
                    while (turnOff == false) {
                        pr("CA");
                        str = s.nextLine();
                        if (str.equals("sair")) {
                            turnOff = true;
                            //Server.sair();
                        }
                        if (str.equals("quit")) {
                            turnOff = true;
                            //Server.sair();
                            System.exit(0);
                        }
                    }
                }
            };
            t.start();
            while (true) {
                System.out.println("Waiting for clients...");

                Socket soc = null;
                try {
                    soc = ss.accept(); //fica à espera da ligação
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.out.println("Client accepted: " + soc);
                ClientHandler ch = new ClientHandler(soc);
                clientHandlers.add(ch);
                ch.start();
            }

        } catch (GenericDAOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static User getUser(String name) {
        User user = null;
        try {
            UserDAO udao = new UserDAO();
            user = udao.findOne(uz, name);
        } catch (GenericDAOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return user;
    }

    public static Map<String, Project> getMapProjects() {
        ProjectDAO pdao = new ProjectDAO();
        Map<String, Project> res = new HashMap<>();
        try {
            List<Project> li = pdao.find(mp, null);
            for (Project p : li) {
                res.put(p.getName(), p);
            }
        } catch (GenericDAOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    static Project getProject(String name) {
        ProjectDAO pdao = new ProjectDAO();
        Project res = null;
        try {
            res = pdao.findOne(mp, name);
        } catch (GenericDAOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    static void addProject(Project proj) {
        ProjectDAO pdao = new ProjectDAO();
        try {
            pdao.insert(mp, proj);
        } catch (GenericDAOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static int addEurosProj(String projectName, String string2, int value1) {
        ProjectDAO pdao = new ProjectDAO();
        int res;
        res = pdao.addEurosProj(mp, projectName, string2, value1);

        return res;
    }

    static boolean addUser(User user) {
        UserDAO udao = new UserDAO();
        boolean res = true;
        try {
            res = udao.insert(uz, user);
        } catch (GenericDAOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

}
