/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.security.diffiehellman.exceptions;

/**
 *
 * @author duarteduarte
 */
public class MessageNotAuthenticatedException extends Exception {

    public MessageNotAuthenticatedException(String wrong_mac) {
    }

}
