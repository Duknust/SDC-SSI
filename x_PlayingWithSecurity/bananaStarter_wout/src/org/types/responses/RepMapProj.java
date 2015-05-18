/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.types.responses;

import java.util.HashMap;
import org.classes.Project;
import org.types.Message;
import org.types.TypeOP;

/**
 *
 * @author duarteduarte
 */
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
