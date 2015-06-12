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
@XStreamAlias("repproj")
public class RepProj extends Message {

    public RepProj(int i) {
        this.type = TypeOP.REP_PROJ;
        this.value1 = i;
    }

    public RepProj(RepProj rap) {
        this.type = rap.getType();
        this.value1 = rap.getValue1();
    }

    @Override
    public RepProj clone() {
        return new RepProj(this);
    }

}
