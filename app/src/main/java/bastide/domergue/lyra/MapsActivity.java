package bastide.domergue.lyra;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, BottomFragment.OnFragmentInteractionListener, LocationListener {

    private GoogleMap mMap;
    private LocationManager lm;
    private final int CODEMAPS = 42;
    private Criteria criteria;
    private String bestProvider;
    private LatLng latLng = new LatLng(0,0);
    private Map<LatLng, String> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // On créer le fragment
        Fragment bottomFragment = new BottomFragment();
        // On lui passe les arguments de l'intent qui a déclencher le oncreate
        bottomFragment.setArguments(getIntent().getExtras());
        //Ajout du fragment au layout
        getSupportFragmentManager().beginTransaction().add(R.id.map, bottomFragment).commit();
        //Création de la liste des messages
        messages = new HashMap<>();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    CODEMAPS);
            return;
        }
       setMarker();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODEMAPS) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        setMarker();
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                CODEMAPS);
                    }
                }
                if (permission.equals(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        setMarker();
                    } else {
                       return;
                    }
                }
            }
        }

    }

    private void setMarker() throws SecurityException {
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        criteria = new Criteria();
        bestProvider = String.valueOf(lm.getBestProvider(criteria, true));
        lm.requestLocationUpdates(bestProvider, 1000, 0, this);
        if(location != null){
            mMap.clear();
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
           // mMap.addMarker(new MarkerOptions().position(latLng).title(getResources().getString(R.string.yourPosition)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            for (Map.Entry<LatLng, String> entry : messages.entrySet())
            {
                mMap.addMarker(new MarkerOptions().position(entry.getKey()).title(entry.getValue()));
            }
        } else {
            lm.requestLocationUpdates(bestProvider, 1000, 0, this);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent().getExtras() != null){
            double longitude = getIntent().getExtras().getDouble("longitude");
            double latitude = getIntent().getExtras().getDouble("latitude");
            String message = getIntent().getExtras().getString("message");
            messages.put(new LatLng(latitude, longitude), message);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onLocationChanged(Location location) {
        setMarker();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void addMessage(View view) {
        Intent nextActivity = new Intent(getApplicationContext(), AddMessageActivity.class);
        //On envoi les coordonnées dans la prochaine activité pour l'ajout du message
        nextActivity.putExtra("longitude", latLng.longitude).putExtra("latitude", latLng.latitude);
        //On lance l'activité d'envoi de message
        startActivity(nextActivity);
    }
}
