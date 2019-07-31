package com.ibm.mysampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.datastore.DatastoreManager;
import com.cloudant.sync.datastore.DatastoreNotCreatedException;
import com.cloudant.sync.datastore.DocumentBody;
import com.cloudant.sync.datastore.DocumentNotFoundException;
import com.cloudant.sync.datastore.DocumentRevision;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnlyHealthDetails extends AppCompatActivity {

    URI cloudantUri;
    DatastoreManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_only_health_details);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String apple = pref.getString("uName", null);

        BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_SYDNEY);

        java.io.File path = getApplicationContext().getDir("datastores", android.content.Context.MODE_PRIVATE);
        manager = DatastoreManager.getInstance(path);

        TextView finalText = findViewById(R.id.displayDetails);

        try {
            cloudantUri = new URI(getApplicationContext().getResources().getString(R.string.cloudantUrl) + "/personal_info/" + apple);

            Datastore userDetails = null;
            Datastore personalInfo = manager.openDatastore("personal_info_immutable");
            userDetails = manager.openDatastore("health_info");

            DocumentRevision a = personalInfo.getDocument(apple);

            DocumentBody documentBody = a.getBody();
            Map<String, Object> hashMap = documentBody.asMap();

            Object[] mango = hashMap.values().toArray();

            Set<String> keyOfPersonalInfo = hashMap.keySet();
            Object[] keyArrayOfPersonalInfo = keyOfPersonalInfo.toArray();

            String appendingForPersonalInfo = "";
            String healthOfUserString = "";

            for(int i=0;i<mango.length; i++) {
                appendingForPersonalInfo += keyArrayOfPersonalInfo[i] + ":" + mango[i].toString() + "\n\n";
            }


            int countVal = pref.getInt("uValue", 0);

            for (int i = 0; i < countVal ; i++) {
                DocumentRevision b = userDetails.getDocument(apple + i);

                DocumentBody documentBody2 = b.getBody();
                Map<String, Object> hashMap2 = documentBody2.asMap();

                Object[] mango2 = hashMap2.values().toArray();
                healthOfUserString += "Document ID: " + countVal + "\n\n";
                Object[] keysOFHealth = hashMap2.keySet().toArray();

                for(int j=0;j < keysOFHealth.length; j++) {
                    healthOfUserString += keysOFHealth[j] + ": " + mango2[j].toString() + "\n";
                }

                healthOfUserString += "\n\n\n";

            }

            finalText.setText(appendingForPersonalInfo + "\n\n\n" + healthOfUserString);


        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (DatastoreNotCreatedException e) {
            e.printStackTrace();
        } catch (DocumentNotFoundException e) {
            e.printStackTrace();
        }

    }
}
