/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsl.classes;

import java.util.Objects;

public class UserEuros implements Comparable<UserEuros> {

    String name;
    int euros;

    public UserEuros(String name, int eur) {
        this.name = name;
        this.euros = eur;
    }

    UserEuros(UserEuros u1) {
        this.name = u1.getName();
        this.euros = u1.getEuros();
    }

    @Override
    public String toString() {
        return name + " : " + euros;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEuros() {
        return euros;
    }

    public void setEuros(int euros) {
        this.euros = euros;
    }

    @Override
    public int compareTo(UserEuros o) {
        if (this.euros < o.getEuros()) {
            return 1;
        } else if (this.euros > o.getEuros()) {
            return -1;
        } else {
            return this.equals(o) ? 0 : -1;
        }
    }

    public synchronized void inceuros(int euros) {
        this.euros += euros;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserEuros other = (UserEuros) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
}
