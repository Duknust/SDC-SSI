/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsl.classes;

import java.net.Socket;
import javax.crypto.SecretKey;

public class OosUser {

    private String user;
    //ObjectOutputStream oos;
    private Socket socket;
    private SecretKey sessionKey;

    public OosUser(String user, Socket socket, SecretKey sessionKey) {
        this.user = user;
        this.socket = socket;
        this.sessionKey = sessionKey;
    }

    public OosUser() {
        this.user = "";
        this.socket = null;
        this.sessionKey = null;
    }

    public String getUser() {
        return user;
    }

    public synchronized void setUser(String user) {
        this.user = user;
    }

    public Socket getSocket() {
        return socket;
    }

    public synchronized void setSocket(Socket socket) {
        this.socket = socket;
    }

    public SecretKey getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(SecretKey sessionKey) {
        this.sessionKey = sessionKey;
    }

}
