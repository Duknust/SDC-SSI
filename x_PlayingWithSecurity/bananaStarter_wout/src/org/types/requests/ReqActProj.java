/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.types.requests;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.classes.Project;
import org.types.Message;
import org.types.TypeOP;

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
