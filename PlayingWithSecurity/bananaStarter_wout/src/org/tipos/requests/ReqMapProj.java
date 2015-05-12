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
public class ReqMapProj extends Mensagem {

    public ReqMapProj() {
        this.tipo = TipoOP.REQMAPPROJ;
    }

    public ReqMapProj(ReqMapProj rap) {
        this.tipo = rap.getTipo();
    }

    @Override
    public ReqMapProj clone() {
        return new ReqMapProj(this);
    }
}
