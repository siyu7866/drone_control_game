package importsdkdemo.dji.com.drone_control_game;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.Calendar;

public class DroneDataSource {

    private SQLiteDatabase database;
    private DroneDBHelper dbHelper;

    public DroneDataSource(Context context) {
        dbHelper = new DroneDBHelper(context);
    }

    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void insertFlightData(Drone d) {
        //boolean didSucceed = false;
        try {
            ContentValues initialValues = new ContentValues();
            int month = d.getFlyingDate().get(Calendar.MONTH) + 1;
            initialValues.put("flyingdate", String.valueOf(d.getFlyingDate().get(Calendar.DAY_OF_MONTH) + "/" + month + "/" +
            d.getFlyingDate().get(Calendar.YEAR)));
            initialValues.put("latitude", d.getLatitude());
            initialValues.put("longitude", d.getLongitude());

            database.insert("drone", null, initialValues);
            //didSucceed = database.insert("drone", null, initialValues) > 0;
        }
        catch (Exception e) {

        }
        //return didSucceed;
    }

    public void insertLocationAddress(Drone d) {
        try {
            int rowId = d.getDroneId();
            ContentValues updatedValues = new ContentValues();
            updatedValues.put("flyingdate", d.getFormatFlyingDate());
            updatedValues.put("latitude", d.getLatitude());
            updatedValues.put("longitude", d.getLongitude());
            updatedValues.put("address", d.getAddress());

            database.update("drone", updatedValues, "_id=" + rowId, null);
        }
        catch (Exception e) {

        }
    }

    public ArrayList<Drone> getFlyingData() {
        ArrayList<Drone> flyingData = new ArrayList<Drone>();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM drone";
            cursor = database.rawQuery(query, null);

            Drone d;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                d = new Drone();
                //Calendar calendar = Calendar.getInstance();
                //calendar.setTimeInMillis(Long.valueOf(cursor.getString(0)));
                //d.setFlyingDate(calendar);
                d.setDroneId(cursor.getInt(0));
                d.setFormatFlyingDate(cursor.getString(1));
                d.setLatitude(cursor.getDouble(2));
                d.setLongitude(cursor.getDouble(3));
                d.setAddress(cursor.getString(4));
                flyingData.add(d);
                cursor.moveToNext();
            }
            cursor.close();
        }
        catch (Exception e) {
            flyingData = new ArrayList<Drone>();
        }
        return flyingData;
    }

    public ArrayList<Drone> getLalLongs() {
        ArrayList<Drone> locationLalLongs = new ArrayList<Drone>();
        Cursor cursor = null;

        try {
            String query = "SELECT DISTINCT latitude, longitude FROM drone";
            cursor = database.rawQuery(query, null);

            Drone d;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                d = new Drone();
                //if (d.getLatitude() = null) {
                    //cursor.moveToNext();
                //} else {
                    d.setLatitude(cursor.getDouble(0));
                    d.setLongitude(cursor.getDouble(1));
                    locationLalLongs.add(d);
                    cursor.moveToNext();
                //}
            }
            cursor.close();
        }
        catch (Exception e) {
            locationLalLongs = new ArrayList<Drone>();
        }
        return locationLalLongs;
    }
}
