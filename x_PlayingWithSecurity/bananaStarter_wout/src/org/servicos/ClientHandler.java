/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.servicos;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.classes.OosUser;
import org.classes.Project;
import org.classes.User;
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

    private Socket cli;
    public static ObjectOutputStream ous;
    private OosUser os;

    public ClientHandler(Socket s) {

        this.cli = s;
    }

    @Override
    public void run() {
        boolean read = false;

        InputStream ins = null;
        OutputStream outs = null;
        Message received = null;
        Message response = null;
        TypeOP type;
        String myName = "";

        try {
            ins = cli.getInputStream();
            outs = cli.getOutputStream();
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
                    //resposta = new Message();
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

                                //resposta.criaREPPROJ(1);
                                response = new RepProj(1);
                                //Mensagem act = new Message();
                                Message act = new ReqActProj(Server.getProject(name));
                                //act.criaACTPROJ();
                                Server.sendToEverybody(act);
                                //ous.writeObject(act);

                            } else {
                                response = new RepProj(0);
                                //resposta.criaREPPROJ(0);

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
                                    //resposta.criaREPADDEUROS(1, , );
                                    //Mensagem p2 = new Message();
                                    Message p2 = new ReqActProj(projReqAddE.clone());
                                    //p2.criaACTPROJ();
                                    Server.sendToEverybody(p2);
                                    ous.writeObject(response);
                                    //ous.writeObject(p2);
                                    //notificar o criador do projecto

                                    //Mensagem p3 = new Message();
                                    Message p3 = new ReqNotifEuros(myName, projectName, received.getValue1());
                                    //p3.criaNOTIFEUROS(meunome, nomeproj, recebido.getValor1());
                                    Server.sendToUser(p3, projReqAddE.getUser());
                                } else if (work == 0) {// NÃO EXISTE
                                    response = new RepAddEuros(0, 0, projectName);
                                    //resposta.criaREPADDEUROS(0, 0, nomeproj);
                                    ous.writeObject(response);
                                } else {//-1 USER == DONO
                                    response = new RepAddEuros(-1, received.getValue1(), projectName);
                                    //resposta.criaREPADDEUROS(-1, recebido.getValor1(), nomeproj);
                                    ous.writeObject(response);
                                }

                            } else {
                                //resposta.criaREPADDEUROS(0, 0, nomeproj);
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
                                //resposta.criaREPREGISTO(nomere, 1);
                            } else {
                                response = new RepRegister(nameRe, 0);
                                //resposta.criaREPREGISTO(nomere, 0);
                            }

                            ous.writeObject(response);
                            break;

                        case REQ_RETRY:

                            String nameRetry = received.getString1();
                            Server.addUser(ous, nameRetry);
                            HashMap<String, Project> mapretry = new HashMap<>(Server.getMapProjects(0));

                            response = new RepMapProj(mapretry);
                            //resposta.criaREPMAPPROJ(mapretry);

                            ous.writeObject(response);
                            break;

                    }

                    read = true;
                }
//                System.out.println("3");
            } catch (IOException | RuntimeException | ClassNotFoundException ex) {
                //System.out.println("IOC_" + ex.toString());
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                Server.remove(os);
                //Server.sair();
                break;
                /* if (ex.toString().compareTo("java.net.SocketException: socket closed") == 0) {
                 //Server.apagasocket(cli);
                 //break;
                 } else if (ex.toString().compareTo("java.io.EOFException") == 0) {
                 //Server.apagasocket(cli);
                 // break;
                 }*/

            }
//            System.out.println("4");

            /*
             String addP = "addProject|";
             if (str.startsWith(addP)) {//USER|NOME|NECESSARIO|DESCRICAO
             char[] ca = str.toCharArray();
             int i;
             StringBuilder user = new StringBuilder();
             StringBuilder nome = new StringBuilder();
             StringBuilder necessario = new StringBuilder();
             int necessarioi;
             StringBuilder descricao = new StringBuilder();

             for (i = addP.length() + 1; ca[i] != '|'; i++) {
             user.append(ca[i]);
             }
             for (i = i + 1; ca[i] != '|'; i++) {
             nome.append(ca[i]);
             }
             for (i = 0; ca[i] != '|'; i++) {
             necessario.append(ca[i]);
             }
             necessarioi = Integer.parseInt(necessario.toString());
             for (i = i + 1; ca[i] != '|'; i++) {
             descricao.append(ca[i]);
             }

             Project p = new Project(nome.toString(), necessarioi, user.toString(), descricao.toString());
             Server.mp.addProject(p);

             }

             */
        }

        //   out.close();
    }

}
