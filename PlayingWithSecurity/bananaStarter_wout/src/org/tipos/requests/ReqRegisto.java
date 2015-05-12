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
public class ReqRegisto extends Mensagem {

    public ReqRegisto(String username, String pass) {
        this.tipo = TipoOP.REQREGISTO;
        this.string1 = username;
        this.string2 = pass;
    }

    public ReqRegisto(ReqRegisto rap) {
        this.tipo = rap.getTipo();
        this.string1 = rap.getString1();
        this.string2 = rap.getString2();
    }

    @Override
    public ReqRegisto clone() {
        return new ReqRegisto(this);
    }
}
