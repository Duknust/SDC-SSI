/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsl.types.responses;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.bsl.types.Message;
import org.bsl.types.TypeOP;

/**
 *
 * @author duarteduarte
 */
@XStreamAlias("repregister")
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
