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
public class RepRegisto extends Mensagem {

    public RepRegisto(String username, int n) {
        this.tipo = TipoOP.REPREGISTO;
        this.string1 = username;
        this.valor1 = n;
    }

    public RepRegisto(RepRegisto rap) {
        this.tipo = rap.getTipo();
        this.string1 = rap.getString1();
        this.valor1 = rap.getValor1();
    }

    @Override
    public RepRegisto clone() {
        return new RepRegisto(this);
    }
}
