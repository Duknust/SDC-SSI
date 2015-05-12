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
import org.classes.OOSUSER;
import org.classes.Projecto;
import org.classes.Utilizador;
import org.tipos.Mensagem;
import org.tipos.TipoOP;
import org.tipos.requests.ReqActProj;
import org.tipos.requests.ReqNotifEuros;
import org.tipos.responses.RepAddEuros;
import org.tipos.responses.RepLogin;
import org.tipos.responses.RepMapProj;
import org.tipos.responses.RepProj;
import org.tipos.responses.RepRegisto;

public class ServeCliente extends Thread {

    private Socket cli;
    public static ObjectOutputStream ous;
    private OOSUSER os;

    public ServeCliente(Socket s) {

        this.cli = s;
    }

    @Override
    public void run() {
        boolean lido;

        InputStream ins = null;
        OutputStream outs = null;
        Mensagem recebido = null;
        Mensagem resposta = null;
        lido = false;
        TipoOP tipo;
        String meunome = "";

        try {
            ins = cli.getInputStream();
            outs = cli.getOutputStream();
        } catch (IOException ex) {
            Logger.getLogger(ServeCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

        ObjectInputStream ois = null;
        ObjectOutputStream ous = null;

        try {
            ois = new ObjectInputStream(ins);
            ous = new ObjectOutputStream(outs);
        } catch (IOException ex) {
            Logger.getLogger(ServeCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

        os = new OOSUSER("", ous);
        Server.addoos(os);
        while (true) {

            recebido = null;
            resposta = null;
            tipo = TipoOP.NULL;

            try {
                System.out.println("/ESPERANDO PACOTES NO SERVER");
                recebido = (Mensagem) ois.readObject();
                System.out.println("|RECEBI PACOTES NO SERVER");

                if (recebido != null) {
                    tipo = recebido.getTipo();
                    //resposta = new Mensagem();
                    System.out.println("\\SERVER_RECEBI " + tipo + "\n");
                    switch (tipo) {
                        case NULL:
                            break;
                        case REQLOGIN:

                            //checka se o user recebido é igual ao do map
                            Utilizador contem = Server.getUtilizador(recebido.getUser().getNome());
                            if (contem != null) {//existe
                                if (contem.equals(recebido.getUser()) == true) {//checka user e pass
                                    //resposta.criaREPLOGIN(1);
                                    resposta = new RepLogin(1);
                                    //Mensagem act = new Mensagem();//Envia o map de projectos para encher o map do cliente
                                    HashMap<String, Projecto> map = new HashMap<>(Server.getMapProjectos(0));
                                    Mensagem act = new RepMapProj(map);
                                    //act.criaREPMAPPROJ(map);
                                    ous.writeObject(act);
                                    //Associa o OOS ao user
                                    meunome = contem.getNome();
                                    Server.adduser(ous, meunome);//adiciona ao map dos pares OOS/user

                                } else {
                                    resposta = new RepLogin(0);
                                }
                            } else {
                                resposta = new RepLogin(2);
                            }

                            System.out.println("SERVER VAI ENVIAR  " + resposta.getTipo() + resposta.getValor1());
                            try {
                                ous.writeObject(resposta);
                            } catch (IOException i) {
                                Logger.getLogger(ServeCliente.class.getName()).log(Level.SEVERE, null, i);
                            }
                            break;

                        case REQMAPPROJ:

                            HashMap<String, Projecto> map = new HashMap<>(Server.getMapProjectos(0));
                            resposta = new RepMapProj(map);

                            ous.writeObject(resposta);
                            break;

                        case REQPROJ:
                            String nome = recebido.getProj().getNome();
                            Projecto p = Server.getProjecto(nome);
                            if (p == null)//não existe no map, então posso criar um
                            {
                                Projecto proj = recebido.getProj();
                                Server.addProjecto(proj);

                                //resposta.criaREPPROJ(1);
                                resposta = new RepProj(1);
                                //Mensagem act = new Mensagem();
                                Mensagem act = new ReqActProj(Server.getProjecto(nome));
                                //act.criaACTPROJ();
                                Server.enviaparatodos(act);
                                //ous.writeObject(act);

                            } else {
                                resposta = new RepProj(0);
                                //resposta.criaREPPROJ(0);

                            }
                            ous.writeObject(resposta);

                            break;

                        case REQADDEUROS:

                            String nomeproj = recebido.getString1();
                            Projecto projreqadde = Server.getProjecto(nomeproj);
                            if (projreqadde != null)//se existe no map
                            {

                                int funca = Server.addEurosProj(nomeproj, recebido.getString2(), recebido.getValor1());//Adiciona euros e historico
                                if (funca > 0) {
                                    resposta = new RepAddEuros(1, recebido.getValor1(), nomeproj);
                                    //resposta.criaREPADDEUROS(1, , );
                                    //Mensagem p2 = new Mensagem();
                                    Mensagem p2 = new ReqActProj(projreqadde.clone());
                                    //p2.criaACTPROJ();
                                    Server.enviaparatodos(p2);
                                    ous.writeObject(resposta);
                                    //ous.writeObject(p2);
                                    //notificar o criador do projecto

                                    //Mensagem p3 = new Mensagem();
                                    Mensagem p3 = new ReqNotifEuros(meunome, nomeproj, recebido.getValor1());
                                    //p3.criaNOTIFEUROS(meunome, nomeproj, recebido.getValor1());
                                    Server.enviaparauser(p3, projreqadde.getUtilizador());
                                } else if (funca == 0) {// NÃO EXISTE
                                    resposta = new RepAddEuros(0, 0, nomeproj);
                                    //resposta.criaREPADDEUROS(0, 0, nomeproj);
                                    ous.writeObject(resposta);
                                } else {//-1 USER == DONO
                                    resposta = new RepAddEuros(-1, recebido.getValor1(), nomeproj);
                                    //resposta.criaREPADDEUROS(-1, recebido.getValor1(), nomeproj);
                                    ous.writeObject(resposta);
                                }

                            } else {
                                //resposta.criaREPADDEUROS(0, 0, nomeproj);
                                resposta = new RepAddEuros(0, 0, nomeproj);
                                ous.writeObject(resposta);
                            }

                            break;

                        case REQREGISTO:

                            String nomere = recebido.getString1();
                            String passre = recebido.getString2();
                            Utilizador user = new Utilizador(nomere, passre);
                            boolean registou = Server.addUtilizador(user);
                            if (registou == false)//nao existe
                            {
                                resposta = new RepRegisto(nomere, 1);
                                //resposta.criaREPREGISTO(nomere, 1);
                            } else {
                                resposta = new RepRegisto(nomere, 0);
                                //resposta.criaREPREGISTO(nomere, 0);
                            }

                            ous.writeObject(resposta);
                            break;

                        case REQRETRY:

                            String nomeretry = recebido.getString1();
                            Server.adduser(ous, nomeretry);
                            HashMap<String, Projecto> mapretry = new HashMap<>(Server.getMapProjectos(0));

                            resposta = new RepMapProj(mapretry);
                            //resposta.criaREPMAPPROJ(mapretry);

                            ous.writeObject(resposta);
                            break;

                    }

                    lido = true;
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
             String addP = "addProjecto|";
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

             Projecto p = new Projecto(nome.toString(), necessarioi, user.toString(), descricao.toString());
             Server.mp.addProjecto(p);

             }

             */
        }

        //   out.close();
    }

}
