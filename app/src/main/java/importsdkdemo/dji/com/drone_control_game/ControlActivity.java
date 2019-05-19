package importsdkdemo.dji.com.drone_control_game;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.model.LocationCoordinate2D;
import dji.common.remotecontroller.GPSData;
import dji.common.util.CommonCallbacks;
import dji.keysdk.FlightControllerKey;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.thirdparty.afinal.core.AsyncTask;

public class ControlActivity extends AppCompatActivity {

    private static final String TAG = ControlActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;
    private static BaseComponent mComponent;
    private Handler mHandler;

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private FlightController mFlightController;
    private FlightControllerState mFlightControllerSate;
    //private FlightControlData mFlightControlData;

    private void initFlightController() {

        Aircraft aircraft = DJISimulatorApplication.getAircraftInstance();
        if (aircraft == null || !aircraft.isConnected()) {
            showToast("Disconnected");
            mFlightController = null;
            return;
        } else {
            mFlightController = aircraft.getFlightController();
            mFlightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
            mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
            mFlightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
            mFlightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
            showToast("Product Connected");
        }
    }


    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        //updateTitleBar();
        initFlightController();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        setContentView(R.layout.activity_control);

        //Initialize DJI SDK Manager
        mHandler = new Handler(Looper.getMainLooper());
        initTakeoff();
        initLand();
        initForwardButton();
        initBackwardButton();
        initRightButton();
        initLeftButton();
        initRecordLocationButton();
        initButtonConnect();
        initButtonFlyingRecord();

    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    showToast("registering, pls wait...");
                    DJISDKManager.getInstance().registerApp(ControlActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                showToast("Register Success");
                                DJISDKManager.getInstance().startConnectionToProduct();
                            } else {
                                showToast("Register sdk fails, please check the bundle id and network connection!");
                            }
                            Log.v(TAG, djiError.getDescription());
                        }

                        @Override
                        public void onProductDisconnect() {
                            Log.d(TAG, "onProductDisconnect");
                            showToast("Product Disconnected");
                            notifyStatusChange();

                        }
                        @Override
                        public void onProductConnect(BaseProduct baseProduct) {
                            Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                            //showToast("Product Connected");
                            notifyStatusChange();

                        }
                        @Override
                        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                      BaseComponent newComponent) {

                            if (newComponent != null) {
                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                        notifyStatusChange();
                                    }
                                });
                            }
                            Log.d(TAG,
                                    String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                            componentKey,
                                            oldComponent,
                                            newComponent));

                        }
                    });
                }
            });
        }
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void initTakeoff() {
        Button buttonTakeoff = (Button) findViewById(R.id.buttonTakeOff);
        buttonTakeoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            showToast(djiError.getDescription());
                        } else {
                            showToast("Take off Success");
                        }
                    }
                });
            }
        });
    }

    private void initLand() {
        Button buttonLand = (Button) findViewById(R.id.buttonLand);
        buttonLand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlightController.startLanding(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            showToast(djiError.getDescription());
                        } else {
                            showToast("Land Success");
                        }
                    }
                });
            }
        });
    }

    private void initForwardButton() {
        ImageButton imgButtonForward = (ImageButton) findViewById(R.id.buttonForward);
        imgButtonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlightController != null) {
                    mFlightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            } else {
                                showToast("Virtual Stick Success");
                            }
                        }
                    });
                    mFlightController.sendVirtualStickFlightControlData(new FlightControlData(0,10f,0,0), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            } else {
                                showToast("Forward Success");
                            }
                        }
                    });
                }
            }
        });
    }

    private void initBackwardButton() {
        ImageButton imgButtonBackward = (ImageButton) findViewById(R.id.buttonBackward);
        imgButtonBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlightController != null) {
                    mFlightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            } else {
                                showToast("Virtual Stick Success");
                            }
                        }
                    });
                    mFlightController.sendVirtualStickFlightControlData(new FlightControlData(0,-10f,0,0), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            } else {
                                showToast("Backward Success");
                            }
                        }
                    });
                }
            }
        });
    }

    private void initRightButton() {
        ImageButton imgButtonRight = (ImageButton) findViewById(R.id.buttonRight);
        imgButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlightController != null) {
                    mFlightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            } else {
                                showToast("Virtual Stick Success");
                            }
                        }
                    });
                    mFlightController.sendVirtualStickFlightControlData(new FlightControlData(10f,0,0,0), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            } else {
                                showToast("Right Success");
                            }
                        }
                    });
                }
            }
        });
    }

    private void initLeftButton() {
        ImageButton imgButtonLeft = (ImageButton) findViewById(R.id.buttonLeft);
        imgButtonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlightController != null) {
                    mFlightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            } else {
                                showToast("Virtual Stick Success");
                            }
                        }
                    });
                    mFlightController.sendVirtualStickFlightControlData(new FlightControlData(-10f,0,0,0), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            } else {
                                showToast("Forward Success");
                            }
                        }
                    });
                }
            }
        });
    }

    private void initRecordLocationButton() {
        Button buttonRecordLocation = (Button) findViewById(R.id.buttonRecordLocation);
        buttonRecordLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mFlightControllerSate.getAircraftLocation().getAltitude();
                DroneDataSource ds = new DroneDataSource(ControlActivity.this);
                Calendar flyingDate = Calendar.getInstance();
                mFlightControllerSate = mFlightController.getState();
                //List<Address> addresses = null;
                //Geocoder geocoder = new Geocoder(ControlActivity.this);
                ds.open();
                Drone d = new Drone();
                d.setFlyingDate(flyingDate);
                //Log.e("day", flyingDate.get(Calendar.d))
                d.setLatitude(mFlightControllerSate.getAircraftLocation().getLatitude());
                d.setLongitude(mFlightControllerSate.getAircraftLocation().getLongitude());
                ds.insertFlightData(d);
                ds.close();
                //Toast.makeText(ControlActivity.this, "Latitude: " + String.valueOf(d.getLatitude()) + "Longitude: "
                //+ String.valueOf(d.getLongitude()), Toast.LENGTH_LONG).show();
                //Toast.makeText(ControlActivity.this, addresses.get(0).getAddressLine(0), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initButtonConnect() {
        Button buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFlightController();
            }
        });
    }

    private void initButtonFlyingRecord() {
        Button buttonFlyingRecord = (Button) findViewById(R.id.buttonFlyingRecord);
        buttonFlyingRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ControlActivity.this, LocationListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
