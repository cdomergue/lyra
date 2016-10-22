package bastide.domergue.lyra;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AddMessageActivity extends AppCompatActivity {

    private TextView coordinatesTextView;
    private double longitude, latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_message);
        longitude = getIntent().getExtras().getDouble("longitude");
        latitude = getIntent().getExtras().getDouble("latitude");
        coordinatesTextView = new TextView(this);
        coordinatesTextView =(TextView)findViewById(R.id.location_coordinates);
        coordinatesTextView.setText(": " + longitude + " " + latitude);
    }
}
