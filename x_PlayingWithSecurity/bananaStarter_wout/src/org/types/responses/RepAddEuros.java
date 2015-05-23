/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.types.responses;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.types.Message;
import org.types.TypeOP;

/**
 *
 * @author duarteduarte
 */
@XStreamAlias("repaddeuros")
public class RepAddEuros extends Message {

    public RepAddEuros(int i, int euros, String nome) {
        this.type = TypeOP.REP_ADD_EUROS;
        this.value1 = i;//1 OK _ 0 FALHOU
        this.value2 = euros;
        this.string1 = nome;
    }

    public RepAddEuros(RepAddEuros rap) {
        this.type = rap.getType();
        this.value1 = rap.getValue1();
        this.value2 = rap.getValue2();
        this.string1 = rap.getString1();
    }

    @Override
    public RepAddEuros clone() {
        return new RepAddEuros(this);
    }
}
