/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsl.types.requests;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.bsl.classes.Project;
import org.bsl.types.Message;
import org.bsl.types.TypeOP;

/**
 *
 * @author duarteduarte
 */
@XStreamAlias("reqactproj")
public class ReqActProj extends Message {

    public ReqActProj(Project project) {
        this.type = TypeOP.ACT_PROJECT; //operation code
        this.proj = project; //project to update
    }

    public ReqActProj(ReqActProj rap) {
        this.type = rap.getType();
        this.proj = rap.getProj();
    }

    @Override
    public ReqActProj clone() {
        return new ReqActProj(this);
    }

}
