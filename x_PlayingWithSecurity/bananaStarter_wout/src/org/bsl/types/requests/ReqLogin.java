/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsl.types.requests;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.bsl.classes.User;
import org.bsl.types.Message;
import org.bsl.types.TypeOP;

/**
 *
 * @author duarteduarte
 */
@XStreamAlias("reqlogin")
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
