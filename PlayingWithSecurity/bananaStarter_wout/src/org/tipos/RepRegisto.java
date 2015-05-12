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
public class RepRegisto extends Mensagem {

    public void criaREPREGISTO(String nome, double n) {
        this.setTipo(TipoOP.REPREGISTO);
        this.setString1(nome);
        this.setValor1(n);
    }
}
