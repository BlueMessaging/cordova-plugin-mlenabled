package com.bm.plugins;

import java.util.List;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;
import android.text.TextUtils;

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
        LocationManager locationManager;
        Location location;
        boolean anyMock = true, locationOff = false;
        int avalibleProviders = 0, avalibleLocations = 0;
        int sdk_int = android.os.Build.VERSION.SDK_INT;
        String msj = "";
        try {
            msj = "\nAPI_LEVEL=" + sdk_int;
            /* Check if location it's turned on */
            if (sdk_int < android.os.Build.VERSION_CODES.KITKAT) {
                String prov = Settings.Secure.getString(this.cordova.getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                locationOff = TextUtils.isEmpty(prov);
                msj += "\nLOCATION_PROVIDERS_ALLOWED=empty";
            }
            else {
                int modeResult = Settings.Secure.getInt(this.cordova.getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);
                locationOff = Settings.Secure.LOCATION_MODE_OFF == modeResult;
                msj += "\nLOCATION_MODE=" + modeResult;
            }
            if (locationOff) {
                callbackContext.success("off:" + msj);
                return;
            }
            locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= 18) {// Jelly Bean 4.3.x
                List<String> providers = locationManager.getProviders(true);
                Location locations[] = new Location[providers.size()];
                for (String p : providers) {
                    msj += "\n\bProvider= " + p;
                    LocationProvider lp = locationManager.getProvider(p);
                    msj += "\naccuracy= " + lp.getAccuracy();
                    msj += "\nrequireNetwork= " + lp.requiresNetwork();
                    msj += "\nreqquireSatelite= " + lp.requiresSatellite();
                    msj += "\nsupportAltitude= " + lp.supportsAltitude();
                    msj += "\nsupportBearing= " + lp.supportsBearing();
                    msj += "\nsupportSpeed= " + lp.supportsSpeed();
                    boolean isEnabled = locationManager.isProviderEnabled(lp.getName());
                    msj += "\nisEnabled= " + isEnabled;
                    if (isEnabled) {
                        location = locationManager.getLastKnownLocation(p);
                        // anyMock &= location != null;
                        if (location != null) {
                            locations[avalibleProviders++] = location;
                            msj += "\nIsFromMockProvider= " + location.isFromMockProvider();
                            anyMock &= location.isFromMockProvider();
                            msj += "\nlatitude= " + location.getLatitude() + " longitude= " + location.getLongitude();
                            avalibleLocations++;
                        }
                        else {
                            msj += "\nNo location";
                        }
                    }
                }
                if (avalibleLocations == 0) {
                    msj = "nolocation:" + msj;
                }
                else {
                    msj = anyMock + ":" + msj;
                }
            }
            else {
                msj = !Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0") + ":" + msj;
            }
            callbackContext.success(msj);
        }
        catch (Exception e) {
            callbackContext.error("Exception\n" + e.getMessage());
        }
    }

}
