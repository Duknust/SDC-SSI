/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.types.requests;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.types.Message;
import org.types.TypeOP;

/**
 *
 * @author duarteduarte
 */
@XStreamAlias("reqaddeuros")
public class ReqAddEuros extends Message {

    public ReqAddEuros(int i, String projectName, String user) {
        this.type = TypeOP.REQ_ADD_EUROS;
        this.value1 = i; //euros increment
        this.string1 = projectName; //project name
        this.string2 = user; //bid's username
    }

    public ReqAddEuros(ReqAddEuros rap) {
        this.type = rap.getType();
        this.value1 = rap.getValue1();
        this.string1 = rap.getString1();
        this.string2 = rap.getString2();
    }

    @Override
    public ReqAddEuros clone() {
        return new ReqAddEuros(this);
    }
}
