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
@XStreamAlias("replogin")
public class RepLogin extends Message {

    public RepLogin(int log) {
        this.type = TypeOP.REP_LOGIN;
        this.value1 = log;
    }

    public RepLogin(RepLogin rap) {
        this.type = rap.getType();
        this.value1 = rap.getValue1();
    }

    @Override
    public RepLogin clone() {
        return new RepLogin(this);
    }

}
