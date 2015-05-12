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
public class ReqReqRetry extends Mensagem {

    public ReqReqRetry(String username) {
        this.tipo = TipoOP.REQRETRY;
        this.string1 = username;
    }

    public ReqReqRetry(ReqReqRetry rap) {
        this.tipo = rap.getTipo();
        this.string1 = rap.getString1();
    }

    @Override
    public ReqReqRetry clone() {
        return new ReqReqRetry(this);
    }
}
