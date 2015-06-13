/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.types.responses;

import org.types.Message;
import org.types.TypeOP;

/**
 *
 * @author duarteduarte
 */
public class RepProj extends Message {

    public RepProj(int i) {
        this.type = TypeOP.REP_PROJ;
        this.value1 = i;
    }

    public RepProj(RepProj rap) {
        this.type = rap.getType();
        this.value1 = rap.getValue1();
    }

    @Override
    public RepProj clone() {
        return new RepProj(this);
    }

}