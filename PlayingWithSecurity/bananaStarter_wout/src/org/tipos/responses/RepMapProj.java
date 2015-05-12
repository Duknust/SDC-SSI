/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tipos.responses;

import java.util.HashMap;
import org.classes.Projecto;
import org.tipos.Mensagem;
import org.tipos.TipoOP;

/**
 *
 * @author duarteduarte
 */
public class RepMapProj extends Mensagem {

    public RepMapProj(HashMap<String, Projecto> map) {
        this.tipo = TipoOP.REPMAPPROJ;
        this.mp = map;
    }

    public RepMapProj(RepMapProj rap) {
        this.tipo = rap.getTipo();
        this.mp = rap.getMp();
    }

    @Override
    public RepMapProj clone() {
        return new RepMapProj(this);
    }
}
