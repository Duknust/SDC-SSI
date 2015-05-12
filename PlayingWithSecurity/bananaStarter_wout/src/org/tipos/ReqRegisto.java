/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tipos;

/**
 *
 * @author duarteduarte
 */
public class ReqRegisto extends Mensagem {

    public void criaREQREGISTO(String nome, String pass) {
        this.setTipo(TipoOP.REQREGISTO);
        this.setString1(nome);
        this.setString2(pass);
    }

}
