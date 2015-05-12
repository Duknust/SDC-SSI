/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tipos.responses;

import org.tipos.Mensagem;
import org.tipos.TipoOP;

/**
 *
 * @author duarteduarte
 */
public class RepProj extends Mensagem {

    public RepProj(int i) {
        this.tipo = TipoOP.REPPROJ;
        this.valor1 = i;
    }

    public RepProj(RepProj rap) {
        this.tipo = rap.getTipo();
        this.valor1 = rap.getValor1();
    }

    @Override
    public RepProj clone() {
        return new RepProj(this);
    }

}
