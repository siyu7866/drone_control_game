package importsdkdemo.dji.com.drone_control_game;

import java.util.Calendar;

public class Drone {
    private double latitude;
    private double longitude;
    private Calendar flyingDate;
    private int droneId;
    private String formatFlyingDate;
    private String address;

    public Drone() {
        droneId = -1;
        latitude = 0;
        longitude = 0;
        flyingDate = Calendar.getInstance();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double i) {
        latitude = i;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double i) {
        longitude = i;
    }

    public Calendar getFlyingDate() {
        return flyingDate;
    }

    public void setFlyingDate(Calendar c) {
        flyingDate = c;
    }

    public String getFormatFlyingDate() {
        return formatFlyingDate;
    }

    public void setFormatFlyingDate(String s) {
        formatFlyingDate = s;
    }

    public int getDroneId() {
        return droneId;
    }

    public void setDroneId(int i) {
        droneId = i;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String s) {
        address = s;
    }
}
