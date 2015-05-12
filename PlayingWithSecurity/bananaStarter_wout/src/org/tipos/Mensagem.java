/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tipos;

import java.io.Serializable;
import java.util.HashMap;
import org.classes.Projecto;
import org.classes.Utilizador;

public abstract class Mensagem implements Serializable {

    private TipoOP tipo;
    private Projecto proj;
    private Utilizador user;
    private HashMap<String, Projecto> mp;
    private String string1;
    private String string2;
    private double valor1;
    private double valor2;

    public Mensagem() {
        this.tipo = TipoOP.NULL;
        this.proj = null;
        this.user = null;
        this.mp = new HashMap<>();
        this.string1 = "";
        this.string2 = "";
        this.valor1 = -1;
        this.valor2 = -1;
    }

    public Mensagem(Mensagem p) {
        this.tipo = p.getTipo();
        this.proj = p.getProj();
        this.user = p.getUser();
        this.mp = p.getMp();
        this.string1 = p.getString1();
        this.string2 = p.getString2();
        this.valor1 = p.getValor1();
        this.valor2 = p.getValor2();
    }

    public Mensagem(TipoOP tipo, Projecto proj, Utilizador user, HashMap<String, Projecto> mp, int inteiro1, int inteiro2, String str, String str2) {
        this.tipo = tipo;
        this.proj = proj;
        this.user = user;
        this.mp.putAll(mp);

        this.string1 = str;
        this.string2 = str2;
        this.valor1 = inteiro1;
        this.valor2 = inteiro2;
    }

    public String getString1() {
        return string1;
    }

    public void setString1(String string1) {
        this.string1 = string1;
    }

    public Projecto getProj() {
        return proj;
    }

    public void setProj(Projecto proj) {
        this.proj = proj;
    }

    public Utilizador getUser() {
        return user;
    }

    public void setUser(Utilizador user) {
        this.user = user;
    }

    public HashMap<String, Projecto> getMp() {
        return mp;
    }

    public void setMp(HashMap<String, Projecto> mp) {
        this.mp.clear();
        this.mp.putAll(mp);
    }

    public TipoOP getTipo() {
        return tipo;
    }

    public void setTipo(TipoOP tipo) {
        this.tipo = tipo;
    }

    public double getValor1() {
        return valor1;
    }

    public void setValor1(double valor1) {
        this.valor1 = valor1;
    }

    public double getValor2() {
        return valor2;
    }

    public void setValor2(double valor2) {
        this.valor2 = valor2;
    }

    public String getString2() {
        return string2;
    }

    public void setString2(String string2) {
        this.string2 = string2;
    }

    //----------------
    public void criaADDPROJECTO(Projecto p) {
        this.tipo = TipoOP.ADDPROJECTO;
        this.proj = p.clone();
    }

    public void criaREQLOGIN(Utilizador login) {
        this.tipo = TipoOP.REQLOGIN;
        this.user = login;
    }

    public void criaREPLOGIN(double log) {
        this.setTipo(TipoOP.REPLOGIN);
        this.setValor1(log);
    }

    public void criaREQMAPPROJ() {
        this.tipo = TipoOP.REQMAPPROJ;
    }

    public void criaREPMAPPROJ(HashMap<String, Projecto> map) {
        this.setTipo(TipoOP.REPMAPPROJ);
        this.setMp(map);
    }

    public void criaREQPROJ(Projecto p) {
        this.setTipo(TipoOP.REQPROJ);
        this.setProj(p);
    }

    public void criaREPPROJ(double i) {
        this.setTipo(TipoOP.REPPROJ);
        this.setValor1(i);
    }

    public void criaREQADDEUROS(double i, String nome, String user) {
        this.setTipo(TipoOP.REQADDEUROS);
        this.setValor1(i);
        this.setString1(nome);
        this.setString2(user);
    }

    public void criaREPADDEUROS(double i, double euros, String nome) {
        this.setTipo(TipoOP.REPADDEUROS);
        this.setValor1(i);//1 OK _ 0 FALHOU
        this.setValor2(euros);
        this.setString1(nome);
    }

    public void criaACTPROJ(Projecto p) {
        this.setTipo(TipoOP.ACTPROJECTO);
        this.setProj(p);
    }

    public void criaNOTIFEUROS(String nome, String proj, double euros) {
        this.setTipo(TipoOP.NOTIFEUROS);
        this.setString1(nome);
        this.setString2(proj);
        this.setValor1(euros);
    }

    public void criaREQRETRY(String nome) {
        this.setTipo(TipoOP.REQRETRY);
        this.setString1(nome);
    }

    @Override
    public String toString() {
        return "Pacote{" + "tipo=" + tipo + ", proj=" + proj + ", user=" + user + ", mp=" + mp + ", inteiro1=" + valor1 + ", inteiro2=" + valor2 + ", string=" + '}';
    }

}
