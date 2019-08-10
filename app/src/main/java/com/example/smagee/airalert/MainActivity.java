package com.example.smagee.airalert;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements DownloadCallback<String> {
  private FusedLocationProviderClient mFusedLocationClient;
  // Keep a reference to the NetworkFragment, which owns the AsyncTask object
  // that is used to execute network ops.
  private NetworkFragment mNetworkFragment;
  private boolean mDownloading;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    Log.d("main", "main start");
    mNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), "https://www.google.com");
    getAirReading(findViewById(R.layout.activity_main));
  }

  public void getAirReading(View view) {
    Log.d("main", "getAirReading");
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
      != PackageManager.PERMISSION_GRANTED) {
        // Permission is not granted
        Log.d("main", "no permission");
      ActivityCompat.requestPermissions(this,
        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
        Constants.MY_PERMISSIONS_REQUEST_FINE_LOCATION);

    } else {
      doLocationRequest();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         String permissions[], int[] grantResults) {
    switch (requestCode) {
      case Constants.MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          doLocationRequest();
        } else {
          //TODO: Handle permission denied
        }
        return;
      }

      // other 'case' lines to check for other
      // permissions this app might request.
    }
  }

  private void doLocationRequest() {
//    writeOutput(R.string.loading_label);
    writeOutput("Loading...");
    mFusedLocationClient.getLastLocation()
      .addOnSuccessListener(this, new OnSuccessListener<Location>() {
        @Override
        public void onSuccess(Location location) {
          // Got last known location. In some rare situations this can be null.
          if (location != null) {
            if (!mDownloading && mNetworkFragment != null) {
              // Execute the async download.
              mNetworkFragment.startDownload(location);
              mDownloading = true;
            } else {
              //TODO: handle case more elegantly
              writeOutput("already downloading");
            }
          } else {
            //TODO: handle case more elegantly
            writeOutput("location is null");
          }
        }
      })
      .addOnFailureListener(this, new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          e.printStackTrace();
        }
      });
  }

  @Override
  public void updateFromDownload(String result) {
    // Update your UI here based on result of download.
    Log.d("result", result);
    try {
      JSONObject res = new JSONObject(result);
      JSONObject pollution = res.getJSONObject("data").getJSONObject("current").getJSONObject("pollution");
      int val = pollution.getInt("aqius");
      int catNum = val / 50;
      String paramName = pollution.getString("ParameterName");
      String output = paramName + ": " + Integer.toString(val);

//      JSONArray jsonArray = new JSONArray(result);
//      JSONObject reading = jsonArray.getJSONObject(1);
//      String paramName = reading.getString("ParameterName");
//      int val = reading.getInt("AQI");
//      int catNum = reading.getJSONObject("Category").getInt("Number");
//      int lastUpdate = reading.getInt("HourObserved");
//      String output = paramName + ": " + Integer.toString(val) + " when: " + Integer.toString(lastUpdate);
      writeOutput(output);
      setLayoutBackground(catNum);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setLayoutBackground(int categoryNum) {
    String color = Constants.AIR_COLORS[categoryNum - 1];
    ColorStateList cl = getListForColor(color);
    ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.main_layout);
    layout.setBackgroundTintList(cl);
  }

  private ColorStateList getListForColor(String color) {
    int[][] states = new int[][] {
      new int[] { android.R.attr.state_enabled}
    };
    int [] colors = new int[] {
      Color.parseColor(color)
    };
    return new ColorStateList(states, colors);
  }

  private void writeOutput(String output) {
    TextView status = (TextView) findViewById(R.id.statusView);
    status.setText(output);
  }

  @Override
  public NetworkInfo getActiveNetworkInfo() {
    ConnectivityManager connectivityManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    return networkInfo;
  }

  @Override
  public void onProgressUpdate(int progressCode, int percentComplete) {
    switch(progressCode) {
      // You can add UI behavior for progress updates here.
      case Progress.ERROR:
//            ...
        break;
      case Progress.CONNECT_SUCCESS:
//            ...
        break;
      case Progress.GET_INPUT_STREAM_SUCCESS:
        Log.d("progress","success");
        break;
      case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
//            ...
        break;
      case Progress.PROCESS_INPUT_STREAM_SUCCESS:
//            ...
        break;
    }
  }

  @Override
  public void finishDownloading() {
    mDownloading = false;
    if (mNetworkFragment != null) {
      mNetworkFragment.cancelDownload();
    }
  }
}
