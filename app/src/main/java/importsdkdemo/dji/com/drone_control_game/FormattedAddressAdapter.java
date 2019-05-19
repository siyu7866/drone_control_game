package importsdkdemo.dji.com.drone_control_game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FormattedAddressAdapter extends ArrayAdapter<Drone> {
    private ArrayList<Drone> items;
    private Context adapterContext;

    public FormattedAddressAdapter(Context context, ArrayList<Drone> items) {
        super(context, R.layout.list_item_formattedaddress, items);
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
                v = vi.inflate(R.layout.list_item_formattedaddress, null);
            }

            TextView textFormattedAddress = (TextView) v.findViewById(R.id.textFormattedAddress);
            TextView textDate = (TextView) v.findViewById(R.id.textDate);

            textFormattedAddress.setText(drones.getAddress());
            textDate.setText(drones.getFormatFlyingDate());
            //birthday.setText(DateFormat.format("MM/dd/yyyy", currentContact.getBirthday().getTimeInMillis()).toString());
        }
        catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
        return v;
    }
}
