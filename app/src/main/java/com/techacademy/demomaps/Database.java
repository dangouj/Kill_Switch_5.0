package com.techacademy.demomaps;

import android.content.Context;

import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.datastore.DatastoreManager;
import com.cloudant.sync.datastore.DatastoreNotCreatedException;
import com.cloudant.sync.datastore.DocumentNotFoundException;
import com.cloudant.sync.datastore.DocumentRevision;
import com.cloudant.sync.replication.Replicator;
import com.cloudant.sync.replication.ReplicatorBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.TreeMap;

/**
 * Created by Dan on 09/08/2016.
 */
public class Database {

    private Context applicationContext;
    private Datastore ds;

    public Database(Context context) {
        applicationContext = context;
        initializePlayerDatastore();
    }

    private void initializePlayerDatastore() {

        // Set local path on device for datastore
        File path = applicationContext.getDir("datastores", Context.MODE_PRIVATE);
        DatastoreManager manager = new DatastoreManager(path.getAbsolutePath());

        ds = null;

        // Create local datastore
        try {
            ds = manager.openDatastore("datastore");
        } catch (DatastoreNotCreatedException e) {
            e.printStackTrace();
        }

        // Replicate from the remote cloud database to local datastore
        Replicator replicator = ReplicatorBuilder.pull().from(getCloudantURI()).to(ds).build();
        replicator.start();


    }

    // Create cloudant database URI using specified credentials
    private URI getCloudantURI() {
        // Cloudant database credentials
        String databaseName = "users_northyork";
        String databaseKey = "thearywhybromeargytoldne";
        String databasePassword = "f2156a14021b80e1cad20b1d25463fe8000aee0a";

        URI uri = null;

        try
        {
            // Generate database URI
            uri = new URI("https://" + databaseKey + ":" + databasePassword + "@eyeofthetiger.cloudant.com/" + databaseName);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        return uri;
    }

    public TreeMap<String, String> getPlayerLocations() {

        // Replicate from the remote cloud database to local datastore
        Replicator replicator = ReplicatorBuilder.pull().from(getCloudantURI()).to(ds).build();
        replicator.start();

        TreeMap<String, String> playersLocations = new TreeMap<String, String>();
        String fieldKeys[] = {"username", "latitude", "longitude"};

        for (String id : ds.getAllDocumentIds()) {

            DocumentRevision doc = null;

            if (!id.contains("_design/")) {
                try {
                    doc = ds.getDocument(id);
                } catch (DocumentNotFoundException e) {
                    e.printStackTrace();
                }
            }

            if (doc != null) {

                JSONObject jObj = null;

                try {
                    jObj = new JSONObject(doc.getBody().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    playersLocations.put(jObj.getString(fieldKeys[0]),
                                        jObj.getString(fieldKeys[1]) + ", " + jObj.getString(fieldKeys[2]));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        return playersLocations;
    }

    private void updateUserLocation() {

        // Set local path on device for datastore
        File path = applicationContext.getDir("datastores", Context.MODE_PRIVATE);
        DatastoreManager manager = new DatastoreManager(path.getAbsolutePath());

        Datastore ds = null;

        // Create local datastore
        try {
            ds = manager.openDatastore("datastore");
        } catch (DatastoreNotCreatedException e) {
            e.printStackTrace();
        }

        // Replicate from the remote cloud database to local datastore
        Replicator replicator = ReplicatorBuilder.push().from(ds).to(getCloudantURI()).build();
        replicator.start();
    }
}
