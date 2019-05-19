package importsdkdemo.dji.com.drone_control_game;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import java.util.ArrayList;

import dji.sdk.products.Aircraft;

public class LocationListActivity extends AppCompatActivity {

    ArrayList<Drone> flyingData;
    ArrayList<Drone> locationLalLongs;
    FlyingDataAdapter flyingDataAdapter;
    FormattedAddressAdapter formattedAddressAdapter;
    //BaseComponent mBaseComponent;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_flightdata);

        initButtonConvertLocation();
        initButtonLatLong();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater();
        return true;
    }

    public void onResume() {
        super.onResume();

        DroneDataSource ds = new DroneDataSource(this);
        try {
            ds.open();
            flyingData = ds.getFlyingData();
            ds.close();
            ListView listFlightData = (ListView) findViewById(R.id.listFlightData);
            flyingDataAdapter = new FlyingDataAdapter(LocationListActivity.this, flyingData);
            listFlightData.setAdapter(flyingDataAdapter);
        }
        catch (Exception e) {
            Toast.makeText(this, "Error retrieving flying data", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        Aircraft aircraft = DJISimulatorApplication.getAircraftInstance();

        if (aircraft == null || !aircraft.isConnected()) {
            if (info != null && info.isConnected()) {
                //Toast.makeText(this, "Network connected", Toast.LENGTH_LONG).show();
                return true;
            } else {
                //Toast.makeText(this, "No network found", Toast.LENGTH_LONG).show();
                return false;
            }
            //Toast.makeText(this, "No drone found", Toast.LENGTH_LONG).show();
            //return false;
        } else {
            //Toast.makeText(this, "drone connected, and no network available", Toast.LENGTH_LONG).show();
            return false;
        }
        /*if (info != null && info.isConnected()) {
            Toast.makeText(this, "Network connected", Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(this, "No network found", Toast.LENGTH_LONG).show();
            return false;
        }*/
    }

    private void initButtonConvertLocation() {
        Button buttonConvertLocation = (Button) findViewById(R.id.buttonConvertLocation);
        buttonConvertLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    DroneDataSource ds = new DroneDataSource(LocationListActivity.this);
                    ds.open();
                    locationLalLongs = ds.getLalLongs();
                    ds.close();
                    GeoApiContext context = new GeoApiContext.Builder().apiKey("AIzaSyBh508HhgYxpr3U54qV_rik5ekuykKdbWI").build();

                    for (int i = 0; i < locationLalLongs.size(); i++) {
                        if (locationLalLongs.get(i).getLatitude() != 0) {
                            String convertedAddress = new String();
                            LatLng mLatLng = new LatLng(locationLalLongs.get(i).getLatitude(), locationLalLongs.get(i).getLongitude());
                            try {
                                GeocodingResult[] results =  GeocodingApi.reverseGeocode(context, mLatLng).await();
                                convertedAddress = results[0].formattedAddress;
                                //Drone d = new Drone();
                                //flyingData = ds.getFlyingData();
                                //Toast.makeText(LocationListActivity.this, results[0].formattedAddress, Toast.LENGTH_LONG).show();
                            } catch (final Exception e) {
                                //e.printStackTrace();
                            }
                            for (int j = 0; j < flyingData.size(); j++) {
                                if (flyingData.get(j).getLatitude() == locationLalLongs.get(i).getLatitude() &
                                        flyingData.get(j).getLongitude() == locationLalLongs.get(i).getLongitude()) {
                                    Drone d = new Drone();
                                    ds.open();
                                    d.setDroneId(flyingData.get(j).getDroneId());
                                    d.setFormatFlyingDate(flyingData.get(j).getFormatFlyingDate());
                                    d.setLatitude(flyingData.get(j).getLatitude());
                                    d.setLongitude(flyingData.get(j).getLongitude());
                                    d.setAddress(convertedAddress);
                                    ds.insertLocationAddress(d);
                                    ds.close();
                                }
                            }

                        }
                    }
                    try {
                        ds.open();
                        flyingData = ds.getFlyingData();
                        ds.close();
                        ListView listView = (ListView) findViewById(R.id.listFlightData);
                        formattedAddressAdapter = new FormattedAddressAdapter(LocationListActivity.this, flyingData);
                        listView.setAdapter(formattedAddressAdapter);
                    }
                    catch (Exception e) {
                        Toast.makeText(LocationListActivity.this, "Error retrieving flying data", Toast.LENGTH_LONG).show();
                    }

                    //Log.i("Address", addresses.get(0).getAddressLine(0));
                    //Toast.makeText(LocationListActivity.this, addresses.get(0).getAddressLine(0), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LocationListActivity.this, "Please connect to network to convert", Toast.LENGTH_LONG).show();
                }
                //isNetworkConnected();
            }
        });
    }

    private void initButtonLatLong() {
        Button buttonLatLong = (Button) findViewById(R.id.buttonLatLong);
        buttonLatLong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DroneDataSource ds = new DroneDataSource(LocationListActivity.this);
                try {
                    ds.open();
                    flyingData = ds.getFlyingData();
                    ds.close();
                    ListView listFlightData = (ListView) findViewById(R.id.listFlightData);
                    flyingDataAdapter = new FlyingDataAdapter(LocationListActivity.this, flyingData);
                    listFlightData.setAdapter(flyingDataAdapter);
                }
                catch (Exception e) {
                    Toast.makeText(LocationListActivity.this, "Error retrieving flying data", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
