/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.types.requests;

import org.classes.User;
import org.types.Message;
import org.types.TypeOP;

/**
 *
 * @author duarteduarte
 */
public class ReqLogin extends Message {

    public ReqLogin(User userLogin) {
        this.type = TypeOP.REQ_LOGIN;
        this.user = userLogin;
    }

    public ReqLogin(ReqLogin rap) {
        this.type = rap.getType();
        this.proj = rap.getProj();
    }

    @Override
    public ReqLogin clone() {
        return new ReqLogin(this);
    }
}
