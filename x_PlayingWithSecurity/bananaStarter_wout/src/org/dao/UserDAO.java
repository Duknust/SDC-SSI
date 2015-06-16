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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bsl.classes.User;
import static org.bsl.services.Server.uz;
import org.bson.Document;
import org.dao.exceptions.GenericDAOException;

/**
 *
 * @author duarteduarte
 */
public class UserDAO extends GenericDAO<User> {

    @Override
    public boolean insert(MongoCollection mc, User object) throws GenericDAOException {
        boolean res = false;

        if (mc.find(new BasicDBObject("_id", object.getName())).limit(1) != null) {
            Map<String, Object> userInMap = new HashMap<>();
            userInMap.put("_id", object.getName());
            userInMap.put("password", object.getPassword());
            Document doc = new Document(userInMap);
            try {
                mc.insertOne(doc);
                res = true;
            } catch (MongoWriteException e) {
                res = false;
            }
        }

        return res;
    }

    @Override
    public boolean delete(MongoCollection mc, User object) throws GenericDAOException {
        boolean res = false;
        if (mc.deleteOne(new BasicDBObject("_id", object.getName())) != null) {
            res = true;
        }
        return res;
    }

    @Override
    public boolean update(MongoCollection mc, User object) throws GenericDAOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User findOne(MongoCollection mc, String object) throws GenericDAOException {
        User res = null;
        MongoCursor<Document> cursor = mc.find(new BasicDBObject("_id", object)).limit(1).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            User user = new User((String) doc.get("_id"), (String) doc.get("password"));
            res = user;
        }
        return res;
    }

    @Override
    public List<User> find(MongoCollection mc, String object) throws GenericDAOException {
        List<User> res = new ArrayList<>();
        MongoCursor<Document> cursor = uz.find(new BasicDBObject("_id", object)).limit(10).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            User user = new User((String) doc.get("_id"), (String) doc.get("password"));
            res.add(user);
        }
        return res;
    }

}
