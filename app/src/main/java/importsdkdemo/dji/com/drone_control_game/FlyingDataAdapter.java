package importsdkdemo.dji.com.drone_control_game;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class FlyingDataAdapter extends ArrayAdapter<Drone> {
    private ArrayList<Drone> items;
    private Context adapterContext;

    public FlyingDataAdapter(Context context, ArrayList<Drone> items) {
        super(context, R.layout.list_item_location, items);
        adapterContext = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        try {
            Drone drones = items.get(position);

            if (v == null) {
                LayoutInflater vi = (LayoutInflater)
                        adapterContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item_location, null);
            }

            TextView txtLatitude = (TextView) v.findViewById(R.id.textLatitude);
            TextView txtLongitude = (TextView) v.findViewById(R.id.textLongitude);
            TextView txtFlyingDate = (TextView) v.findViewById(R.id.textDate);

            txtLatitude.setText("Latitude: " + String.valueOf(drones.getLatitude()));
            txtLongitude.setText("Longitude: " + String.valueOf(drones.getLongitude()));
            txtFlyingDate.setText(drones.getFormatFlyingDate());
            //birthday.setText(DateFormat.format("MM/dd/yyyy", currentContact.getBirthday().getTimeInMillis()).toString());
        }
        catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
        return v;
    }
}
