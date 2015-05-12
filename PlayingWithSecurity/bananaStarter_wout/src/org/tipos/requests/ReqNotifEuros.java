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
public class ReqNotifEuros extends Mensagem {

    public ReqNotifEuros(String username, String projectName, int euros) {
        this.tipo = TipoOP.NOTIFEUROS;
        this.string1 = username;
        this.string2 = projectName;
        this.valor1 = euros;
    }

    public ReqNotifEuros(ReqNotifEuros rap) {
        this.tipo = rap.getTipo();
        this.string1 = rap.getString1();
        this.string2 = rap.getString2();
        this.valor1 = rap.getValor1();
    }

    @Override
    public ReqNotifEuros clone() {
        return new ReqNotifEuros(this);
    }

}
