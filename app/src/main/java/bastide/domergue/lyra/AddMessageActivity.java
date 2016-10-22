package bastide.domergue.lyra;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

    public void validateMessage(View view) {
        EditText messageEditText   = (EditText)findViewById(R.id.message);
        String message = messageEditText.getText().toString();
        Intent nextActivity = new Intent(getApplicationContext(), MapsActivity.class);
        nextActivity.putExtra("longitude", longitude).putExtra("latitude", latitude).putExtra("message", message);
        //On lance l'activité d'envoi de message
        startActivity(nextActivity);
    }
}
