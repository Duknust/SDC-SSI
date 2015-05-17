package org.servicos;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.classes.OosUser;
import org.classes.Project;
import org.classes.User;
import org.types.Message;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import static org.servicos.Server.mp;
import org.types.requests.ReqActProj;
import org.types.requests.ReqNotifEuros;

public class Server {

    public static ArrayBlockingQueue<Project> mpQueue = new ArrayBlockingQueue<>(20, true);

    public static MongoCollection uz;
    public static MongoCollection mp;
    public static ArrayList<OosUser> listOosUser;

    public static void pr(String s) {

        System.out.println(s);
    }

    public static void remove(OosUser u) {

        listOosUser.remove(u);
    }

    public static void sendToEverybody(Message p) {

        for (OosUser s : listOosUser) {
            try {
                s.getOos().writeObject(((ReqActProj) p).clone());
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                pr("to all_" + ex.toString());
            }

        }

    }

    public static void sendToUser(Message p, String user) {

        for (OosUser s : listOosUser) {

            if (s.getUser().compareTo(user) == 0) {
                try {
                    s.getOos().writeObject(((ReqNotifEuros) p).clone());
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    pr("to user " + user + " _ " + ex.toString());
                }
            }

        }

    }

    public static void removeOosUser(OosUser s) {

        listOosUser.remove(s);

    }

    public static synchronized void addoos(OosUser s) {

        listOosUser.add(s);
    }

    public static synchronized void addUser(ObjectOutputStream o, String user) {

        for (OosUser s : listOosUser) {

            if (s.getOos() == o) {

                s.setUser(user);

            }

        }
    }

    public static synchronized boolean addUser(User u) {
        boolean res = false;

        if (uz.find(new BasicDBObject("username", u.getName())).limit(1) != null) {
            Map<String, Object> userInMap = new HashMap<>();
            userInMap.put("username", u.getName());
            userInMap.put("password", u.getPassword());
            Document doc = new Document(userInMap);
            try {
                uz.insertOne(doc);
                res = true;
            } catch (MongoWriteException e) {
                res = false;
            }
        }

        return res;
    }

    public static synchronized boolean addProject(Project p) {
        boolean res = false;

        if (mp.find(new BasicDBObject("_id", p.getName())).limit(1) != null) {

            try {
                uz.insertOne(p.toDocument());
                res = true;
            } catch (MongoWriteException e) {
                res = false;
            }
        }

        return res;
    }

    public static HashMap<String, Project> getMapProjects(int skip) {
        HashMap<String, Project> res = new HashMap<>();
        MongoCursor<Document> cursor = mp.find().skip(skip).limit(10).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            Project proj = Project.fromDocument(doc);
            res.put(proj.getName(), proj);
            mpQueue.add(proj);
        }

        return res;
    }

    public static User getUser(String name) {
        User res = null;
        MongoCursor<Document> cursor = uz.find(new BasicDBObject("_id", name)).limit(1).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            User user = new User((String) doc.get("_id"), (String) doc.get("password"));
            res = user;
        }
        return res;
    }

    /**
     * *
     *
     * @param projName
     * @param nameDonator
     * @param euros
     * @return -1 próprio projecto, 0 projecto nao existe, 1 sucesso
     */
    public static int addEurosProj(String projName, String nameDonator, int euros) {
        int res = 0;
        Project p = null;
        Map<String, Object> projInMap = new HashMap<>();
        projInMap.put("_id", projName);
        Document doc = new Document(projInMap);
        MongoCursor<Document> cursor = mp.find(doc).iterator();
        while (cursor.hasNext()) {
            Document proj = cursor.next();
            p = Project.fromDocument(proj);
        }

        if (p != null) {
            synchronized (p) {
                if (p.getUser().compareTo(nameDonator) == 0) //user=dono
                {
                    res = -1;
                } else {
                    p.addEuros(euros);
                    res = 1;
                }
            }
            doc = p.toDocument();
            //mp.updateOne(Filters.eq("_id", nomeProj), doc);
            mp.updateOne(Filters.eq("_id", projName), new Document("$set", new Document("pledged", p.getPledged() + "")));

        }

        return res;
    }

    public static Project getProject(String projName) {
        Project res = null;
        synchronized (mp) {
            for (Project p : mpQueue) {
                if (p.getName().equals(projName)) {
                    res = p;
                }
            }
            if (res == null) {
                MongoCursor<Document> cursor = mp.find(new Document("_id", projName)).limit(1).iterator();
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    Project proj = Project.fromDocument(doc);
                    res = proj;
                    mpQueue.add(res);
                }
            }
        }
        return res;
    }

    public static void main(String[] args) {

        //mp = new HashMap<>();
        //uz = new HashMap<>();
        MongoClient mongoClient = null;

        mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase("projectWithSecurity");
        uz = db.getCollection("users");
        mp = db.getCollection("projects");

        //abrir();
        //Utilizador u = new User("user", "pass");
        //uz.put(u.getNome(), u);
        //Utilizador u2 = new User("user2", "pass");
        //uz.put(u2.getNome(), u2);
        //Projecto p = new Project("CANETA", 100, 0, "user", "AAAAAAAA");
        //mp.put(p.getNome(), p);
        listOosUser = new ArrayList<>();

        //int port = Integer.parseInt(args[0]);
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

            new ClientHandler(soc).start();
        }

    }
    /*
     public synchronized static void sair() {
     ObjectOutputStream oos = null;
     pr("vou gravar");
     try {
     //ToDo: limpar cache e matar thread
     String fich = "ServerDados.sd";
     oos = new ObjectOutputStream(
     new FileOutputStream(fich));
     savefile s = new savefile(mp, uz);
     oos.writeObject(s);
     oos.flush();
     oos.close();

     } catch (IOException ex) {
     Logger.getLogger(Server.class
     .getName()).log(Level.SEVERE, null, ex);
     } finally {
     try {
     oos.close();

     } catch (IOException ex) {
     Logger.getLogger(Server.class
     .getName()).log(Level.INFO, null, ex);
     }
     }

     }

     private static void abrir() throws IOException {

     pr("vou abrir");
     String fich = "ServerDados.sd";
     savefile s = null;
     ObjectInputStream oos = null;
     try {
     oos = new ObjectInputStream(new FileInputStream(fich));

     s = (savefile) oos.readObject();
     } catch (StreamCorruptedException | EOFException | ClassNotFoundException | FileNotFoundException c) {
     pr(c.toString());
     }

     if (s != null) {
     Server.mp = new HashMap<>(s.getMp());
     Server.uz = new HashMap<>(s.getUz());
     oos.close();

     }

     }*/

    static class savefile implements Serializable {

        private HashMap<String, Project> mp;
        private HashMap<String, User> uz;

        public savefile(HashMap<String, Project> mp, HashMap<String, User> uz) {
            this.mp = mp;
            this.uz = uz;
        }

        public HashMap<String, Project> getMp() {
            return mp;
        }

        public void setMp(HashMap<String, Project> mp) {
            this.mp = mp;
        }

        public HashMap<String, User> getUz() {
            return uz;
        }

        public void setUz(HashMap<String, User> uz) {
            this.uz = uz;
        }
    }
}
