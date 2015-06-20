/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsl.types.responses;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.HashMap;
import org.bsl.classes.Project;
import org.bsl.types.Message;
import org.bsl.types.TypeOP;

/**
 *
 * @author duarteduarte
 */
@XStreamAlias("repmapproj")
public class RepMapProj extends Message {

    public RepMapProj(HashMap<String, Project> map) {
        this.type = TypeOP.REP_MAP_PROJ;
        this.mp = map;
    }

    public RepMapProj(RepMapProj rap) {
        this.type = rap.getType();
        this.mp = rap.getMp();
    }

    @Override
    public RepMapProj clone() {
        return new RepMapProj(this);
    }
}
