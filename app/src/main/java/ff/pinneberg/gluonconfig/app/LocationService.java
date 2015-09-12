package ff.pinneberg.gluonconfig.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service {
    public static final String BROADCAST_ACTION = "ff.pinneberg.gluonconfig.locationChanged";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public locationListener listener;
    public Location previousBestLocation = null;

    SharedPreferences sp;


    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    public LocationService(){
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        sp = PreferenceManager.getDefaultSharedPreferences(getApplication());

        if(sp.getBoolean("automatic_location",false)) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            listener = new locationListener();
            long min_time_distance = Long.parseLong(sp.getString("check_new_position", "10"))*60000;
            float min_location_distance = Float.parseFloat(sp.getString("min_distance","10"));
            if(sp.getBoolean("use_gps",false)){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, min_time_distance, min_location_distance, listener);
            }
            if(sp.getBoolean("use_wifi",false)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, min_time_distance, min_location_distance, listener);
            }
            if(sp.getBoolean("use_passive_location",false)){
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, min_time_distance, min_location_distance, listener);
            }


        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }



    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }



    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(listener);
    }




    public class locationListener implements LocationListener
    {

        public void onLocationChanged(final Location loc)
        {
            Log.i(getClass().getSimpleName(),"Location Changed");
            if(isBetterLocation(loc, previousBestLocation)) {

                Core.sshHelper.executeCommandThread(MainActivity.gluon_set + "gluon-node-info.@location[0].latitude=" + loc.getLatitude());
                Core.sshHelper.executeCommandThread(MainActivity.gluon_set + "gluon-node-info.@location[0].longitude=" + loc.getLongitude());

            }
        }

        public void onProviderDisabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
        }


        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

    }
}
