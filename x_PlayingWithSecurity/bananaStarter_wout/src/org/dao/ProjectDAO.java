/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bsl.classes.Project;
import org.bson.Document;
import org.dao.exceptions.GenericDAOException;

/**
 *
 * @author duarteduarte
 */
public class ProjectDAO extends GenericDAO<Project> {

    public int addEurosProj(MongoCollection mc, String projName, String nameDonator, int euros) {
        int res = 0;
        Project p = null;
        Map<String, Object> projInMap = new HashMap<>();
        projInMap.put("_id", projName);
        Document doc = new Document(projInMap);
        MongoCursor<Document> cursor = mc.find(doc).iterator();
        while (cursor.hasNext()) {
            Document proj = cursor.next();
            p = Project.fromDocument(proj);
        }

        if (p != null) {
            synchronized (p) {
                if (p.getUser().compareTo(nameDonator) == 0) //user=dono
                {
                    res = -1;
                } else {
                    p.addEuros(euros);
                    res = 1;
                }
            }
            doc = p.toDocument();
            //mp.updateOne(Filters.eq("_id", nomeProj), doc);
            mc.updateOne(Filters.eq("_id", projName), new Document("$set", new Document("pledged", p.getPledged() + "")));

        }

        return res;
    }

    @Override
    public boolean insert(MongoCollection mc, Project object) throws GenericDAOException {
        boolean res = false;

        if (mc.find(new BasicDBObject("_id", object.getName())).limit(1) != null) {

            try {
                mc.insertOne(object.toDocument());
                res = true;
            } catch (MongoWriteException e) {
                res = false;
            }
        }

        return res;
    }

    @Override
    public boolean delete(MongoCollection mc, Project object) throws GenericDAOException {
        boolean res = false;
        Project project = (Project) object;
        if (mc.deleteOne(new BasicDBObject("_id", project.getName())) != null) {
            res = true;
        }
        return res;
    }

    @Override
    public boolean update(MongoCollection mc, Project object) throws GenericDAOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Project findOne(MongoCollection mc, String object) throws GenericDAOException {
        Project res = null;
        MongoCursor<Document> cursor = mc.find(new BasicDBObject("_id", (String) object)).limit(1).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            res = Project.fromDocument(doc);
        }
        return res;
    }

    @Override
    public List<Project> find(MongoCollection mc, String object) throws GenericDAOException {
        List<Project> res = new ArrayList<>();
        MongoCursor<Document> cursor = mc.find().limit(10).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            Project proj = Project.fromDocument(doc);
            res.add(proj);
        }
        return res;
    }

}
