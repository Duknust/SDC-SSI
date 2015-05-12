/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tipos.requests;

import org.classes.Projecto;
import org.tipos.Mensagem;
import org.tipos.TipoOP;

/**
 *
 * @author duarteduarte
 */
public class ReqActProj extends Mensagem {

    public ReqActProj(Projecto project) {
        this.tipo = TipoOP.ACTPROJECTO; //operation code
        this.proj = project; //project to update
    }

    public ReqActProj(ReqActProj rap) {
        this.tipo = rap.getTipo();
        this.proj = rap.getProj();
    }

    @Override
    public ReqActProj clone() {
        return new ReqActProj(this);
    }

}
