/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.types.requests;

import org.types.Message;
import org.types.TypeOP;

/**
 *
 * @author duarteduarte
 */
public class ReqRegister extends Message {

    public ReqRegister(String username, String pass) {
        this.type = TypeOP.REQ_REGISTER;
        this.string1 = username;
        this.string2 = pass;
    }

    public ReqRegister(ReqRegister rap) {
        this.type = rap.getType();
        this.string1 = rap.getString1();
        this.string2 = rap.getString2();
    }

    @Override
    public ReqRegister clone() {
        return new ReqRegister(this);
    }
}
