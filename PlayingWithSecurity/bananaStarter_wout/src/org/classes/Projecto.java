package org.classes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import org.bson.Document;
import org.servicos.Server;

public class Projecto implements Serializable {

    private String Nome;//Nome
    private double Necessario;//Quanto precisa
    private double Actual;//Quanto tem ou com quanto ficou
    private String Descricao;//Descricao do Projecto
    private String Utilizador;//ID do Utilizador que o criou
    private TreeSet<UserEuros> ListaBids;//ID do Utilizador que o criou

    //CONSTRUTORES
    public Projecto() {
        this.Nome = "";
        this.Necessario = 0;
        this.Actual = 0;
        this.Utilizador = "";
        this.Descricao = "";
        this.ListaBids = new TreeSet<>();

    }

    public Projecto(String Nome, double Necessario, double Final, String Utilizador, /*boolean Finalizado,*/ String Descricao/*, Calendar Inicio, Calendar Fim*/) {
        this.Nome = Nome;
        this.Necessario = Necessario;
        this.Actual = Final;
        this.Utilizador = Utilizador;
        this.Descricao = Descricao;
        this.ListaBids = new TreeSet<>();
    }

    public Projecto(String Nome, double Necessario, String Utilizador, String Descricao/*, int fim*/) {
        this.Nome = Nome;
        this.Necessario = Necessario;
        this.Actual = 0;
        this.Utilizador = Utilizador;
        this.Descricao = Descricao;
        this.ListaBids = new TreeSet<>();
    }

    public Projecto(Projecto p) {
        this.Nome = p.getNome();
        this.Necessario = p.getNecessario();
        this.Actual = p.getActual();
        this.Utilizador = p.getUtilizador();
        this.Descricao = p.getDescricao();
        this.ListaBids = p.getListaBids();
    }

    //GETTERS AND SETTERS
    public String getNome() {
        return Nome;
    }

    public void setNome(String Nome) {
        this.Nome = Nome;
    }

    public double getNecessario() {
        return Necessario;
    }

    public void setNecessario(double Necessario) {
        this.Necessario = Necessario;
    }

    public double getActual() {
        return Actual;
    }

    public void setActual(double Actual) {
        this.Actual = Actual;
    }

    public String getUtilizador() {
        return Utilizador;
    }

    public void setUtilizador(String Utilizador) {
        this.Utilizador = Utilizador;
    }

    public String getDescricao() {
        return Descricao;
    }

    public void setDescricao(String Descricao) {
        this.Descricao = Descricao;
    }

    public TreeSet<UserEuros> getListaBids() {
        return new TreeSet<>(ListaBids);
    }

    public void setListaBids(TreeSet<UserEuros> ListaBids) {
        this.ListaBids = ListaBids;
    }

    public String printEstado() {
        return "Estado Actual:\n" + this.getActual() + " € de " + this.getNecessario() + " €";
    }

    public synchronized boolean addeuros(double euros) {//adiciona dinheiro se estiver NÃO-FINALIZADO _ LOCK
        boolean ret;
        this.Actual += euros;
        Server.mp.updateOne(new Document("name", this.getNome()), new Document("$inc", euros));
        ret = true;
        return ret;

    }

    @Override
    public String toString() {
        return "Projecto{" + "Nome=" + Nome + ", Necessario=" + Necessario + ", Final=" + Actual + ", Utilizador=" + Utilizador /*S+ ", Finalizado=" + Finalizado*/ + ", Descricao=" + Descricao /*+ ", Inicio=" + formatter.format(Inicio.getTime()) + ", Fim=" + formatter.format(Fim.getTime())*/ + '}';
    }

    public Projecto clone() {
        Projecto cl = new Projecto(this);

        return cl;
    }

    public boolean isFinanciado() {
        if (this.Actual >= this.Necessario) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized void addhistorico(String nome, double euros) {
        boolean userJaContribuiu = false;
        UserEuros u1 = null;
        for (UserEuros u : ListaBids) {
            if (u.getNome().equals(nome)) {
                u1 = new UserEuros(u);
                u1.inceuros(euros);
                ListaBids.remove(u);
                ListaBids.add(u1);
                userJaContribuiu = true;
                break;
            }
        }

        if (!userJaContribuiu) {
            u1 = new UserEuros(nome, euros);
            ListaBids.add(u1);
        }
    }

    public static Projecto fromDocument(Document doc) {
        return new Projecto((String) doc.get("_id"), (double) doc.get("goal"), (double) doc.get("pledged"), (String) doc.get("username"), (String) doc.get("description"));
    }

    public Document toDocument() {
        Map<String, Object> userInMap = new HashMap<>();
        userInMap.put("_id", this.Nome);
        userInMap.put("goal", this.Necessario);
        userInMap.put("pledged", this.Actual);
        userInMap.put("username", this.Utilizador);
        userInMap.put("bids", this.ListaBids.toArray());
        userInMap.put("description", this.Descricao);
        return new Document(userInMap);
    }
}
