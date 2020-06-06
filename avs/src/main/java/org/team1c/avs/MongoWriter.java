package org.team1c.avs;

import java.util.HashMap;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;

import org.bson.Document;


public class MongoWriter {
    private String host;
    private int port;
    private MongoClient client;
    private MongoDatabase db;
    private MongoCollection<Document> collection;

    public MongoWriter(String host, int port, String database, String collection) {
        this.host = host;
        this.port = port;
        this.client = new MongoClient(host, port);
        this.db = this.client.getDatabase(database);
        this.collection = db.getCollection(collection);
    }

    public void insert(String file, double avgFaces) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("file", file);
        map.put("avgFaces", avgFaces);
        Document doc = new Document(map);
        this.collection.insertOne(doc);
    }

}