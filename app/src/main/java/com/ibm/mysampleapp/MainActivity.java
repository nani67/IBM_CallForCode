package com.ibm.mysampleapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudant.mazha.Document;
import com.cloudant.sync.datastore.DocumentBody;
import com.cloudant.sync.datastore.DocumentBodyFactory;
import com.cloudant.sync.datastore.DocumentException;
import com.cloudant.sync.datastore.DocumentNotFoundException;
import com.cloudant.sync.datastore.DocumentRevision;
import com.google.android.material.snackbar.Snackbar;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;

import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.datastore.DatastoreManager;
import com.cloudant.sync.datastore.DatastoreNotCreatedException;


import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity
{
    private java.net.URI cloudantUri;
    private DatastoreManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View view1 = findViewById(R.id.registerView);
        final View view2 = findViewById(R.id.loginView);


        BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_SYDNEY);

        java.io.File path = getApplicationContext().getDir("datastores", android.content.Context.MODE_PRIVATE);
        manager = DatastoreManager.getInstance(path);


        TextView textView = findViewById(R.id.registerTextView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view1.setVisibility(View.VISIBLE);
                view2.setVisibility(View.INVISIBLE);
            }
        });



        final Button loginBtn =  findViewById(R.id.button2);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText loginID = findViewById(R.id.editText);
                EditText pass = findViewById(R.id.editText2);



                try {
                    cloudantUri = new java.net.URI(getApplicationContext().getResources().getString(R.string.cloudantUrl) + "/personal_info/" + loginID.getText().toString());

                    Datastore userDetails = manager.openDatastore("personal_info_loginCred");

                    List<String> apple = userDetails.getAllDocumentIds();

                    for (String z: apple) {
                        Log.d("Document ID: ", z);

                    }


                    DocumentRevision a = userDetails.getDocument(loginID.getText().toString());
                    DocumentBody documentBody = a.getBody();
                    Map<String, Object> hashMap = documentBody.asMap();
                    if(hashMap.containsValue(pass.getText().toString())) {

                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                        SharedPreferences.Editor editor = pref.edit();

                        editor.putString("uName", loginID.getText().toString());

                        startActivity(new Intent(MainActivity.this, UserHealthRecordUpdate.class));

                    } else {
                        Snackbar.make(view, "Login credentials incorrect", Snackbar.LENGTH_LONG).show();
                    }

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (DatastoreNotCreatedException e) {
                    e.printStackTrace();
                } catch (DocumentNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });




        Button submitBtn = findViewById(R.id.registrationInitiation);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    final EditText userEmail = findViewById(R.id.userEmail);

                    cloudantUri = new java.net.URI(getApplicationContext().getResources().getString(R.string.cloudantUrl) + "/personal_info/" + userEmail.getText().toString());

                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                    SharedPreferences.Editor editor = pref.edit();

                    editor.putString("uName", userEmail.getText().toString());

                    final EditText userName = findViewById(R.id.userName);
                    final EditText userMobNo = findViewById(R.id.userMobileNumber);
                    final EditText userDOB = findViewById(R.id.userDateOfBirth);
                    final EditText userPlaceOfEmp = findViewById(R.id.userPlaceOfEmployment);
                    final EditText userOcc = findViewById(R.id.userOccupation);
                    final EditText userReligiousPreferences = findViewById(R.id.userReligiousPreference);
                    final EditText userRelationsPhNos = findViewById(R.id.userRelativesPhoneNo);
                    final EditText userRelEmail = findViewById(R.id.userRelativesEmailID);
                    final EditText userGovtIdType = findViewById(R.id.userGovernmentIDType);
                    final EditText userGovtIdVal = findViewById(R.id.userGovernmentIDNo);
                    final EditText insuranceComName = findViewById(R.id.userInsuranceCompanyName);
                    final EditText insurancePolicyNo = findViewById(R.id.userInsurancePolicyNo);
                    final EditText password = findViewById(R.id.userPassword);

                    Datastore dsOne = manager.openDatastore("personal_info_loginCred");
                    Datastore dsTwo = manager.openDatastore("personal_info_immutable");

                    DocumentRevision documentRevision = new DocumentRevision(userEmail.getText().toString());
                    DocumentBody documentBody = DocumentBodyFactory.create(new HashMap<String, String>() {{
                        put("UserName",userName.getText().toString());
                        put("UserMobileNumber",userMobNo.getText().toString());
                        put("UserDOB",userDOB.getText().toString());
                        put("UserPlaceOfEmployment",userPlaceOfEmp.getText().toString());
                        put("UserOccupation",userOcc.getText().toString());
                        put("UserReligiousPreference",userReligiousPreferences.getText().toString());
                        put("UserRelationsNumbers",userRelationsPhNos.getText().toString());
                        put("UserEmailID",userEmail.getText().toString());
                        put("UserRelationsEmailID",userRelEmail.getText().toString());
                        put("UserGovernmentIDType",userGovtIdType.getText().toString());
                        put("UserGovernmentIDValue",userGovtIdVal.getText().toString());
                        put("InsuranceCompanyName",insuranceComName.getText().toString());
                        put("InsuranceCompanyValue",insurancePolicyNo.getText().toString());

                    }});

                    documentRevision.setBody(documentBody);
                    dsTwo.createDocumentFromRevision(documentRevision);

                    DocumentRevision documentRevision2 = new DocumentRevision(userEmail.getText().toString());
                    DocumentBody documentBody2 = DocumentBodyFactory.create(new HashMap<String, String>() {{
                        put("UserName",userName.getText().toString());
                        put("UserMobileNumber",userMobNo.getText().toString());
                        put("UserEmailID",userEmail.getText().toString());
                        put("UserPassword", password.getText().toString());

                    }});
                    documentRevision2.setBody(documentBody2);
                    dsOne.createDocumentFromRevision(documentRevision2);

                    startActivity(new Intent(MainActivity.this, UserHealthRecordUpdate.class));

                } catch (java.net.URISyntaxException e) {
                    android.util.Log.e("TAG", e.getMessage(), e);
                } catch (DatastoreNotCreatedException e) {
                    android.util.Log.e("TAG", e.getMessage(), e);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }

            }
        });




    }
    

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
