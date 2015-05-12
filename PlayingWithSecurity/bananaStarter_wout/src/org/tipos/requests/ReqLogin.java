/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tipos.requests;

import org.classes.Utilizador;
import org.tipos.Mensagem;
import org.tipos.TipoOP;

/**
 *
 * @author duarteduarte
 */
public class ReqLogin extends Mensagem {

    public ReqLogin(Utilizador userLogin) {
        this.tipo = TipoOP.REQLOGIN;
        this.user = userLogin;
    }

    public ReqLogin(ReqLogin rap) {
        this.tipo = rap.getTipo();
        this.proj = rap.getProj();
    }

    @Override
    public ReqLogin clone() {
        return new ReqLogin(this);
    }
}
