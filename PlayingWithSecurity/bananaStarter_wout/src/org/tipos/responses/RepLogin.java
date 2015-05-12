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
public class RepLogin extends Mensagem {

    public RepLogin(int log) {
        this.tipo = TipoOP.REPLOGIN;
        this.valor1 = log;
    }

    public RepLogin(RepLogin rap) {
        this.tipo = rap.getTipo();
        this.valor1 = rap.getValor1();
    }

    @Override
    public RepLogin clone() {
        return new RepLogin(this);
    }

}
