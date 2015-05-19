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
