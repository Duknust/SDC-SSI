package org.servicos;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.classes.OOSUSER;
import org.classes.Projecto;
import org.classes.Utilizador;
import org.tipos.Mensagem;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import static org.servicos.Server.mp;
import org.tipos.requests.ReqActProj;
import org.tipos.requests.ReqNotifEuros;

public class Server {

    public static ArrayBlockingQueue<Projecto> mpQueue = new ArrayBlockingQueue<>(20, true);

    public static MongoCollection uz;
    public static MongoCollection mp;
    public static ArrayList<OOSUSER> listaoosuser;

    public static void pr(String s) {

        System.out.println(s);
    }

    public static void remove(OOSUSER u) {

        listaoosuser.remove(u);
    }

    public static void enviaparatodos(Mensagem p) {

        for (OOSUSER s : listaoosuser) {
            try {
                s.getOos().writeObject(((ReqActProj) p).clone());
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                pr("para todos_" + ex.toString());
            }

        }

    }

    public static void enviaparauser(Mensagem p, String user) {

        for (OOSUSER s : listaoosuser) {

            if (s.getUser().compareTo(user) == 0) {
                try {
                    s.getOos().writeObject(((ReqNotifEuros) p).clone());
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    pr("para user " + user + " _ " + ex.toString());
                }
            }

        }

    }

    public static void apagaoosuser(OOSUSER s) {

        listaoosuser.remove(s);

    }

    public static synchronized void addoos(OOSUSER s) {

        listaoosuser.add(s);
    }

    public static synchronized void adduser(ObjectOutputStream o, String user) {

        for (OOSUSER s : listaoosuser) {

            if (s.getOos() == o) {

                s.setUser(user);

            }

        }
    }

    public static synchronized boolean addUtilizador(Utilizador u) {
        boolean res = false;

        if (uz.find(new BasicDBObject("username", u.getNome())).limit(1) != null) {
            Map<String, Object> userInMap = new HashMap<>();
            userInMap.put("username", u.getNome());
            userInMap.put("password", u.getPass());
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

    public static synchronized boolean addProjecto(Projecto p) {
        boolean res = false;

        if (mp.find(new BasicDBObject("_id", p.getNome())).limit(1) != null) {

            try {
                uz.insertOne(p.toDocument());
                res = true;
            } catch (MongoWriteException e) {
                res = false;
            }
        }

        return res;
    }

    public static HashMap<String, Projecto> getMapProjectos(int skip) {
        HashMap<String, Projecto> res = new HashMap<>();
        MongoCursor<Document> cursor = mp.find().skip(skip).limit(10).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            Projecto proj = Projecto.fromDocument(doc);
            res.put(proj.getNome(), proj);
            mpQueue.add(proj);
        }

        return res;
    }

    public static Utilizador getUtilizador(String nome) {
        Utilizador res = null;
        MongoCursor<Document> cursor = uz.find(new BasicDBObject("_id", nome)).limit(1).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            Utilizador user = new Utilizador((String) doc.get("_id"), (String) doc.get("password"));
            res = user;
        }
        return res;
    }

    /**
     * *
     *
     * @param nomeProj
     * @param nomeUserDoador
     * @param euros
     * @return -1 próprio projecto, 0 projecto nao existe, 1 sucesso
     */
    public static int addEurosProj(String nomeProj, String nomeUserDoador, int euros) {
        int res = 0;
        Projecto p = null;
        Map<String, Object> projInMap = new HashMap<>();
        projInMap.put("_id", nomeProj);
        Document doc = new Document(projInMap);
        MongoCursor<Document> cursor = mp.find(doc).iterator();
        while (cursor.hasNext()) {
            Document proj = cursor.next();
            p = Projecto.fromDocument(proj);
        }

        if (p != null) {
            synchronized (p) {
                if (p.getUtilizador().compareTo(nomeUserDoador) == 0) //user=dono
                {
                    res = -1;
                } else {
                    p.addeuros(euros);
                    p.addhistorico(nomeUserDoador, euros);
                    res = 1;
                }
            }
            doc = p.toDocument();
            mp.updateOne(new Document("_id", nomeProj), doc);
        }

        return res;
    }

    public static Projecto getProjecto(String nomeProj) {
        Projecto res = null;
        synchronized (mp) {
            for (Projecto p : mpQueue) {
                if (p.getNome().equals(nomeProj)) {
                    res = p;
                }
            }
            if (res == null) {
                MongoCursor<Document> cursor = mp.find(new Document("_id", nomeProj)).limit(1).iterator();
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    Projecto proj = Projecto.fromDocument(doc);
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
        //Utilizador u = new Utilizador("user", "pass");
        //uz.put(u.getNome(), u);
        //Utilizador u2 = new Utilizador("user2", "pass");
        //uz.put(u2.getNome(), u2);
        //Projecto p = new Projecto("CANETA", 100, 0, "user", "AAAAAAAA");
        //mp.put(p.getNome(), p);
        listaoosuser = new ArrayList<>();

        //int port = Integer.parseInt(args[0]);
        int port = 1337;
        System.out.println("A ligar à porta " + port + ", espere  ...");
        ServerSocket ss = null;

        try {
            ss = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Servidor iniciado: " + ss);

        Thread t = new Thread() {
//Isto é que mal começa uma thread nunca mais funca, o save só é feito quando há um disconnect
            @Override
            public void run() {
                Scanner s = new Scanner(System.in);
                String str;
                boolean sair = false;
                while (sair == false) {
                    pr("CA");
                    str = s.nextLine();
                    if (str.equals("sair")) {
                        sair = true;
                        //Server.sair();
                    }
                    if (str.equals("quit")) {
                        sair = true;
                        //Server.sair();
                        System.exit(0);
                    }
                }
            }
        };
        t.start();
        while (true) {
            System.out.println("À espera de Clientes ...");

            Socket soc = null;
            try {
                soc = ss.accept(); //fica à espera da ligação
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("Cliente aceite: " + soc);

            new ServeCliente(soc).start();
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

        private HashMap<String, Projecto> mp;
        private HashMap<String, Utilizador> uz;

        public savefile(HashMap<String, Projecto> mp, HashMap<String, Utilizador> uz) {
            this.mp = mp;
            this.uz = uz;
        }

        public HashMap<String, Projecto> getMp() {
            return mp;
        }

        public void setMp(HashMap<String, Projecto> mp) {
            this.mp = mp;
        }

        public HashMap<String, Utilizador> getUz() {
            return uz;
        }

        public void setUz(HashMap<String, Utilizador> uz) {
            this.uz = uz;
        }
    }
}
