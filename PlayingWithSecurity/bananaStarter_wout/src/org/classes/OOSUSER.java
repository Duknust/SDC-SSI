/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.classes;

import java.io.ObjectOutputStream;

public class OosUser {

    String user;
    ObjectOutputStream oos;

    public OosUser(String user, ObjectOutputStream oos) {
        this.user = user;
        this.oos = oos;
    }

    public OosUser() {
        this.user = "";
        this.oos = null;
    }

    public String getUser() {
        return user;
    }

    public synchronized void setUser(String user) {
        this.user = user;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public synchronized void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }

}
