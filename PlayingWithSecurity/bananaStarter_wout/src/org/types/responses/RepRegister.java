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
public class RepRegister extends Message {

    public RepRegister(String username, int n) {
        this.type = TypeOP.REP_REGISTER;
        this.string1 = username;
        this.value1 = n;
    }

    public RepRegister(RepRegister rap) {
        this.type = rap.getType();
        this.string1 = rap.getString1();
        this.value1 = rap.getValue1();
    }

    @Override
    public RepRegister clone() {
        return new RepRegister(this);
    }
}
