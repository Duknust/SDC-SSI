/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dao;

import com.mongodb.client.MongoCollection;
import java.util.List;
import org.dao.exceptions.GenericDAOException;

/**
 *
 * @author duarteduarte
 */
public abstract class GenericDAO<T> {

    public abstract boolean insert(MongoCollection mc, T object) throws GenericDAOException;

    public abstract boolean delete(MongoCollection mc, T object) throws GenericDAOException;

    public abstract boolean update(MongoCollection mc, T object) throws GenericDAOException;

    public abstract T findOne(MongoCollection mc, String object) throws GenericDAOException;

    public abstract List<T> find(MongoCollection mc, String object) throws GenericDAOException;

}
