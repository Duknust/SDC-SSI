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
public class RepAddEuros extends Mensagem {

    public RepAddEuros(int i, int euros, String nome) {
        this.tipo = TipoOP.REPADDEUROS;
        this.valor1 = i;//1 OK _ 0 FALHOU
        this.valor2 = euros;
        this.string1 = nome;
    }

    public RepAddEuros(RepAddEuros rap) {
        this.tipo = rap.getTipo();
        this.valor1 = rap.getValor1();
        this.valor2 = rap.getValor2();
        this.string1 = rap.getString1();
    }

    @Override
    public RepAddEuros clone() {
        return new RepAddEuros(this);
    }
}
