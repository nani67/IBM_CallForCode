package com.ibm.mysampleapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.datastore.DatastoreManager;
import com.cloudant.sync.datastore.DatastoreNotCreatedException;
import com.cloudant.sync.datastore.DocumentBody;
import com.cloudant.sync.datastore.DocumentBodyFactory;
import com.cloudant.sync.datastore.DocumentException;
import com.cloudant.sync.datastore.DocumentRevision;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class UserHealthRecordUpdate extends AppCompatActivity {


    Bitmap bitmapOne = null;
    Bitmap bitmapTwo = null;

    boolean clickedDiagBtn = false;
    boolean clickedRadiologyBtn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uhr);


        BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_SYDNEY);

        java.io.File path = getApplicationContext().getDir("datastores", android.content.Context.MODE_PRIVATE);
        final DatastoreManager manager = DatastoreManager.getInstance(path);

        Button btn1 = findViewById(R.id.userDiagnosisReport);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedDiagBtn = true;
                clickedRadiologyBtn = false;
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 3);
            }
        });


        Button btn2 = findViewById(R.id.userRadiologyImages);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedRadiologyBtn = true;
                clickedDiagBtn = false;
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 3);
            }
        });

        Button btn3 = findViewById(R.id.updationInitiation);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    String emailAddresss = null;

                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                    SharedPreferences.Editor editor = pref.edit();

                    int countVal = pref.getInt("uValue", 0);
                    emailAddresss = pref.getString("uName", null);

                    URI cloudantUri = new URI(getApplicationContext().getResources().getString(R.string.cloudantUrl) + "/personal_info/" + emailAddresss);

                    final EditText userName = findViewById(R.id.userAllergies);
                    final EditText userMobNo = findViewById(R.id.userIfUndergoneSurgery);
                    final EditText userDOB = findViewById(R.id.userBloodGroup);
                    final EditText userPlaceOfEmp = findViewById(R.id.userDiseases);
                    final EditText userOcc = findViewById(R.id.userSpecialEquipment);
                    final EditText userReligiousPreferences = findViewById(R.id.userVitalSigns);
                    final EditText userRelationsPhNos = findViewById(R.id.userMedications);
                    final EditText userEmail = findViewById(R.id.userImmunizationDates);

                    Datastore dsOne = manager.openDatastore("health_info");

                    DocumentRevision documentRevision = new DocumentRevision(emailAddresss + countVal);
                    DocumentBody documentBody = DocumentBodyFactory.create(new HashMap<String, String>() {{
                        put("UserAllergies", userName.getText().toString());
                        put("UserIfUndergoneSurgery", userMobNo.getText().toString());
                        put("UserBloodGroup", userDOB.getText().toString());
                        put("UserDiseases", userPlaceOfEmp.getText().toString());
                        put("UserSpecialEquipment", userOcc.getText().toString());
                        put("UserVitalSigns", userReligiousPreferences.getText().toString());
                        put("UserMedications", userRelationsPhNos.getText().toString());
                        put("UserImmunizationdates", userEmail.getText().toString());

                    }});
                    documentRevision.setBody(documentBody);
                    dsOne.createDocumentFromRevision(documentRevision);

                    editor.putInt("uValue", countVal + 1);
                    editor.apply();

                    Snackbar.make(view, "Updation successful! Thank you for your cooperation!", Snackbar.LENGTH_LONG)
                            .setAction("Done", null).show();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (DatastoreNotCreatedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==3 && resultCode == Activity.RESULT_OK && clickedDiagBtn) {
            Uri selectedImage = data.getData();
            try {
                bitmapOne = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(requestCode==3 && resultCode == Activity.RESULT_OK && clickedRadiologyBtn) {
            Uri selectedImage = data.getData();
            try {
                bitmapTwo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
