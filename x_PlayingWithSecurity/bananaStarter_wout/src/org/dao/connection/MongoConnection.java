/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dao.connection;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @author duarteduarte
 */
public class MongoConnection extends GenericConnection {

    private MongoClient mongoClient = null;
    private MongoDatabase db = null;

    public MongoConnection(String databaseName) {
        this.mongoClient = new MongoClient();
        this.db = this.mongoClient.getDatabase("projectWithSecurity");
    }

    public MongoCollection getCollection(String collectionName) {
        return this.db.getCollection(collectionName);
    }
}
