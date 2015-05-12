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
public class ReqProj extends Mensagem {

    public ReqProj(Projecto project) {
        this.tipo = TipoOP.REQPROJ;
        this.proj = project;
    }

    public ReqProj(ReqProj rap) {
        this.tipo = rap.getTipo();
        this.proj = rap.getProj();
    }

    @Override
    public ReqProj clone() {
        return new ReqProj(this);
    }

}
