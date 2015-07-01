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
@XStreamAlias("repwronglogin")
public class RepWrongLogin extends Message {

    public RepWrongLogin(String nome) {
        this.type = TypeOP.REP_WRONG_LOGIN;
        this.string1 = nome;
    }
}
