/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsl.types.requests;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.bsl.types.Message;
import org.bsl.types.TypeOP;

/**
 *
 * @author duarteduarte
 */
@XStreamAlias("reqnotifeuros")
public class ReqNotifEuros extends Message {

    public ReqNotifEuros(String username, String projectName, int euros) {
        this.type = TypeOP.NOTIFEUROS;
        this.string1 = username;
        this.string2 = projectName;
        this.value1 = euros;
    }

    public ReqNotifEuros(ReqNotifEuros rap) {
        this.type = rap.getType();
        this.string1 = rap.getString1();
        this.string2 = rap.getString2();
        this.value1 = rap.getValue1();
    }

    @Override
    public ReqNotifEuros clone() {
        return new ReqNotifEuros(this);
    }

}
