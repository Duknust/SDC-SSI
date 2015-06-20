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
@XStreamAlias("reqaddproj")
public class ReqAddProj extends Message {

    public ReqAddProj(Project project) {
        this.type = TypeOP.ADD_PROJECT;
        this.proj = project;
    }

    public ReqAddProj(ReqAddProj rap) {
        this.type = rap.getType();
        this.proj = rap.getProj();
    }

    @Override
    public ReqAddProj clone() {
        return new ReqAddProj(this);
    }

}
