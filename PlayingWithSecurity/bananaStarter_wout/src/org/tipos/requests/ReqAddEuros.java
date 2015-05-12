/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tipos.requests;

import org.tipos.Mensagem;
import org.tipos.TipoOP;

/**
 *
 * @author duarteduarte
 */
public class ReqAddEuros extends Mensagem {

    public ReqAddEuros(int i, String projectName, String user) {
        this.tipo = TipoOP.REQADDEUROS;
        this.valor1 = i; //euros increment
        this.string1 = projectName; //project name
        this.string2 = user; //bid's username
    }

    public ReqAddEuros(ReqAddEuros rap) {
        this.tipo = rap.getTipo();
        this.valor1 = rap.getValor1();
        this.string1 = rap.getString1();
        this.string2 = rap.getString2();
    }

    @Override
    public ReqAddEuros clone() {
        return new ReqAddEuros(this);
    }
}
