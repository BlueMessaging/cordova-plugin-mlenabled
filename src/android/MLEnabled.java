package com.bm.plugins;

import java.util.List;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.content.Context;
import android.location.Location; 
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;
import android.os.Bundle;

public class MLEnabled extends CordovaPlugin {
  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("check")) {      
      this.check(callbackContext);
      return true;
    }
    return false;
  }
  private void check(CallbackContext callbackContext) {
    Context context = this.cordova.getActivity().getApplicationContext();    
    Location location; 
    LocationManager locationManager;  
    String msj = "";
    boolean anyMock = false;
    int avalible = 0;
    try {
      locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);      
      if (android.os.Build.VERSION.SDK_INT >= 18) {
          List<String> providers = locationManager.getProviders(true);          
          Location locations[] = new Location[providers.size()];  
          for(String p: providers){
              msj += "\n\nProvider= " + p;
              LocationProvider lp = locationManager.getProvider(p);
              msj += "\naccuracy= " + lp.getAccuracy();
              msj += "\nrequireNetwork= " + lp.requiresNetwork();
              msj += "\nreqquireSatelite= " + lp.requiresSatellite();
              msj += "\nsupportAltitude= " + lp.supportsAltitude();
              msj += "\nsupportBearing= " + lp.supportsBearing();
              msj += "\nsupportSpeed= " + lp.supportsSpeed();
              boolean isEnabled = locationManager.isProviderEnabled(lp.getName());
              msj += "\nisEnabled= " + isEnabled;
              if(isEnabled){                  
                  location = locationManager.getLastKnownLocation(p);
                  if(location != null){
                      locations[avalible++] = location; 
                      msj += "\nIsFromMockProvider= " + location.isFromMockProvider();
                      if(location.isFromMockProvider()) anyMock = true;
                      msj += "\nlatitude= " + location.getLatitude() + " longitude= " + location.getLongitude();
                  }                  
              } 
          }
          if(!anyMock){
              mockFound:
                  for(int i = 0; i < avalible; i++){
                      for(int j = 0; j < avalible; j++){
                          if(locations[i].distanceTo(locations[j]) > 100){
                              anyMock = true;
                              break mockFound;
                          }
                      }
                  }
          }          
          msj = anyMock + ":" + msj;
      } else {
        msj += !Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
      }      
      callbackContext.success(msj);
    } catch (Exception e) {            
      callbackContext.error(e.getMessage());
    }
  }
  
}
