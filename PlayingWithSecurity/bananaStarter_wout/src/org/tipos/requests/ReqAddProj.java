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
public class ReqAddProj extends Mensagem {

    public ReqAddProj(Projecto project) {
        this.tipo = TipoOP.ADDPROJECTO;
        this.proj = project;
    }

    public ReqAddProj(ReqAddProj rap) {
        this.tipo = rap.getTipo();
        this.proj = rap.getProj();
    }

    @Override
    public ReqAddProj clone() {
        return new ReqAddProj(this);
    }

}
