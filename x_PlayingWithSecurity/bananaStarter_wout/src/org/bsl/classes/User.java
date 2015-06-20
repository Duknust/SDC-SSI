package org.bsl.classes;

public class User {

    private String name;
    private String password;

    //Constructores
    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public User(User user) {
        this.name = user.getName();
        this.password = user.getPassword();
    }

    //GETTERS e SETTERS
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    //FX
    public boolean login(String password) {

        if (this.password.equals(password)) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (this.name.compareTo(other.getName()) != 0) {
            return false;
        }
        if (this.password.compareTo(other.getPassword()) != 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "User{" + "Name=" + name + ", Password=" + password + '}';
    }
}
