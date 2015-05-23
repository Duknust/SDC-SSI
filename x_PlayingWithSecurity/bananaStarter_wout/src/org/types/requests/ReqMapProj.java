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
@XStreamAlias("reqmapproj")
public class ReqMapProj extends Message {

    public ReqMapProj() {
        this.type = TypeOP.REQ_MAP_PROJ;
    }

    public ReqMapProj(ReqMapProj rap) {
        this.type = rap.getType();
    }

    @Override
    public ReqMapProj clone() {
        return new ReqMapProj(this);
    }
}
