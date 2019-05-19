package importsdkdemo.dji.com.drone_control_game;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DroneDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dronedata.db";
    private static final int DATABASE_VERSION = 2;

    private static final String CREATE_TABLE_CONTACT =
            "create table drone (_id integer primary key autoincrement, "
                    + "flyingdate text, "
                    + "latitude text, longitude text, "
                    + "address text);";

    public DroneDBHelper (Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CONTACT);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        //Log.w(ContactDBHelper.class.getName(),
        //"Upgrading database from version" + oldVersion + "to" + newVersion + ", which will destroy all old data");
        //db.execSQL("DROP TABLE IF EXISTS contact");
        //onCreate(db);
    }

}
