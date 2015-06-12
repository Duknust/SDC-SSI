package org.bsl.classes;

import java.util.HashMap;
import java.util.Map;
import org.bson.Document;

public class Project {

    private String name;//Nome
    private int goal;//Quanto precisa
    private int pledged;//Quanto tem ou com quanto ficou
    private String description;//Descricao do Project
    private String user;//ID do user que o criou

    //CONSTRUTORES
    public Project() {
        this.name = "";
        this.goal = 0;
        this.pledged = 0;
        this.user = "";
        this.description = "";

    }

    public Project(String name, int goal, int pledged, String user, String description) {
        this.name = name;
        this.goal = goal;
        this.pledged = pledged;
        this.user = user;
        this.description = description;
    }

    public Project(String name, int goal, String user, String description) {
        this.name = name;
        this.goal = goal;
        this.pledged = 0;
        this.user = user;
        this.description = description;
    }

    public Project(Project p) {
        this.name = p.getName();
        this.goal = p.getGoal();
        this.pledged = p.getPledged();
        this.user = p.getUser();
        this.description = p.getDescription();
    }

    //GETTERS AND SETTERS
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public int getPledged() {
        return pledged;
    }

    public void setPledged(int pledged) {
        this.pledged = pledged;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String printStatus() {
        return "Estado Actual:\n" + this.getPledged() + " € de " + this.getGoal() + " €";
    }

    public synchronized boolean addEuros(int euros) {//adiciona dinheiro se estiver NÃO-FINALIZADO _ LOCK
        boolean ret;
        this.pledged += euros;
        ret = true;
        return ret;

    }

    @Override
    public String toString() {
        return "Project{" + "Name=" + this.name + ", Goal=" + this.goal + ", Pledged=" + this.pledged + ", User=" + this.user + ", Description=" + this.description + '}';
    }

    public Project clone() {
        Project cl = new Project(this);

        return cl;
    }

    public boolean isFinanciated() {
        if (this.pledged >= this.goal) {
            return true;
        } else {
            return false;
        }
    }

    public static Project fromDocument(Document doc) {
        return new Project((String) doc.get("_id"), Integer.parseInt((String) doc.get("goal")), Integer.parseInt((String) doc.get("pledged")), (String) doc.get("username"), (String) doc.get("description"));
    }

    public Document toDocument() {
        Map<String, Object> userInMap = new HashMap<>();
        userInMap.put("_id", this.name);
        userInMap.put("username", this.user);
        userInMap.put("pledged", this.pledged + "");
        userInMap.put("description", this.description);
        userInMap.put("goal", this.goal + "");

        return new Document(userInMap);
    }
}
