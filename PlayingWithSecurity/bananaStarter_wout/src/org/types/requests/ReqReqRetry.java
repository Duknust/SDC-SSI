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
public class ReqReqRetry extends Message {

    public ReqReqRetry(String username) {
        this.type = TypeOP.REQ_RETRY;
        this.string1 = username;
    }

    public ReqReqRetry(ReqReqRetry rap) {
        this.type = rap.getType();
        this.string1 = rap.getString1();
    }

    @Override
    public ReqReqRetry clone() {
        return new ReqReqRetry(this);
    }
}
