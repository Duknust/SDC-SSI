package org.types;

import org.classes.Project;
import java.util.*;
import java.io.*;

public class HashMapObs extends Observable implements Serializable {

    public HashMap<String, Project> vals = new HashMap<>();

    public HashMapObs() {
        vals = new HashMap<String, Project>();
    }

    public HashMap<String, Project> getProjectsList() {
        return vals;
    }

    public void setProjectsList(HashMap<String, Project> projectsList) {
        this.vals.clear();
        this.vals.putAll(projectsList);
        setChanged();

        notifyObservers();
    }

    public void insertProject(Project pr) {

        vals.put(pr.getName(), pr);

        setChanged();

        notifyObservers();

    }

    public void addEurosProj(String name, int euros) {

        if (vals.containsKey(name)) {//se existe projecto
            boolean ret = vals.get(name).addEuros(euros);//contém LOCK
            if (ret) {
                setChanged();
                notifyObservers();
            }
        }

    }

    public String getUserProject(String name) {

        if (vals.containsKey(name)) {
            return vals.get(name).getUser();
        } else {
            return "";
        }

    }

    public HashMap<String, Project> getByName(String name, boolean finalized) {
        HashMap<String, Project> pesq = new HashMap<>();

        for (Project p : this.vals.values()) {
            if ((p.getName().contains(name) || p.getDescription().contains(name)) && p.isFinanciated() == finalized) {
                pesq.put(p.getName(), p.clone());
            }

        }

        return pesq;
    }

    public void setProj(Project pr) {

        synchronized (this.vals) {
            if (pr != null) {
                vals.put(pr.getName(), pr);
                setChanged();
                notifyObservers();
            }

        }

    }

    public void saveObj(String fich) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(fich));
        oos.writeObject(this);
        oos.flush();
        oos.close();
    }

    public void saveTxt(String fich) throws IOException {
        PrintWriter pw = new PrintWriter(fich);
        pw.print(this);
        pw.flush();
        pw.close();

    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("List of Projects:\n");
        Iterator i = vals.values().iterator();
        Project p;
        while (i.hasNext()) {
            p = (Project) i.next();
            s.append(p.toString());
        }
        return s.toString();
    }

    public HashMap<String, Project> getActives() {

        HashMap<String, Project> result = new HashMap<>();
        for (Project p : vals.values()) {
            if (p.isFinanciated() == false) {
                result.put(p.getName(), p.clone());
            }
        }
        return result;
    }

    public HashMap<String, Project> getFinalized() {

        HashMap<String, Project> result = new HashMap<>();
        for (Project p : vals.values()) {
            if (p.isFinanciated() == true) {
                result.put(p.getName(), p.clone());
            }
        }
        return result;
    }
}
