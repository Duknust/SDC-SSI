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
@XStreamAlias("reqproj")
public class ReqProj extends Message {

    public ReqProj(Project project) {
        this.type = TypeOP.REQ_PROJ;
        this.proj = project;
    }

    public ReqProj(ReqProj rap) {
        this.type = rap.getType();
        this.proj = rap.getProj();
    }

    @Override
    public ReqProj clone() {
        return new ReqProj(this);
    }

}
