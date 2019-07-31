package com.ibm.mysampleapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity
{
    private java.net.URI cloudantUri;
    private DatastoreManager manager;

    public static String AddressVal = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View view1 = findViewById(R.id.registerView);
        final View view2 = findViewById(R.id.loginView);


        LocationManager locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationProvider provider =
                locationManager.getProvider(LocationManager.GPS_PROVIDER);


        final LocationListener listener = new LocationListener() {

            public void onLocationChanged(Location location) {
                // Bypass reverse-geocoding if the Geocoder service is not available on the
                // device. The isPresent() convenient method is only available on Gingerbread or above.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                    // Since the geocoding API is synchronous and may take a while.  You don't want to lock
                    // up the UI thread.  Invoking reverse geocoding in an AsyncTask.
                    (new ReverseGeocodingTask(getApplicationContext())).execute(new Location[] {location});
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };



        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.reliefweb.int/v1/reports?appname=testbeforecommit&limit=1";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonObject1 = (JSONArray) jsonObject.get("data");
                    JSONObject jsonObject2 = jsonObject1.getJSONObject(0);
                    JSONObject jsonArray = (JSONObject) jsonObject2.get("fields");

                    String valueOfResponse = jsonArray.getString("title");

//                    if(valueOfResponse.contains(AddressVal)) {
//                        startActivity(new Intent(MainActivity.this, OnlyHealthDetails.class));
//                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("onErrorResponse", error.toString());
            }
        }
        );

        queue.add(stringRequest);

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
                        editor.apply();

                        startActivity(new Intent(MainActivity.this, OnlyHealthDetails.class));


                        //startActivity(new Intent(MainActivity.this, UserHealthRecordUpdate.class));

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

                    editor.putInt("uValue", 0);

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





class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
    Context mContext;

    public ReverseGeocodingTask(Context context) {
        super();
        mContext = context;
    }

    @Override
    protected Void doInBackground(Location... params) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

        Location loc = params[0];
        List<Address> addresses = null;
        try {
            // Call the synchronous getFromLocation() method by passing in the lat/long values.
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            // Format the first line of address (if available), city, and country name.
            String addressText = String.format("%s, %s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getLocality(),
                    address.getCountryName());

            MainActivity.AddressVal = address.getLocality();
        }
        return null;
    }
}

