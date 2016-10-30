package bastide.domergue.lyra.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import bastide.domergue.lyra.contentProvider.MessagesContentProvider;
import bastide.domergue.lyra.R;
import bastide.domergue.lyra.fragment.BottomFragment;

import static bastide.domergue.lyra.contentProvider.MessagesContentProvider.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, BottomFragment.OnFragmentInteractionListener, LocationListener {

    private GoogleMap mMap;
    private LocationManager lm;
    private final int CODEMAPS = 42;
    private Criteria criteria;
    private String bestProvider;
    private LatLng latLng = new LatLng(0,0);
    private Map<LatLng, String> messages;
    public static Context context;

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
        MapsActivity.context = getApplicationContext();
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
           //On demande les permission de localisation
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
                    //Si on a la permission de localisation précise, on peut placer les marqueurs
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        setMarker();
                    } else {
                        //Sinon, on demande la permission de la localisation imprécise
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                CODEMAPS);
                    }
                }
                if (permission.equals(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //Si on a la permission de localisation imprécise, on peut placer les marqueurs
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        setMarker();
                    } else {
                        //Sinon, GTFO my application :D
                       return;
                    }
                }
            }
        }

    }

    /*
    *
    * On place les marqueurs sur la carte
     */
    private void setMarker() throws SecurityException {
        //On se connecte à la db
        MessagesContentProvider messagesContentProvider = new MessagesContentProvider();
        //On fait une requête, on stocke le résultat dans un curseur
        Cursor cursor = messagesContentProvider.query(Uri.parse("content://fr.esiea.lyra"),
                new String[] { KEY_POSITION_X, KEY_POSITION_Y,
                KEY_MESSAGE }, null, null, null);
        //On récupère la dernière localisation connue par le GPS
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        criteria = new Criteria();
        bestProvider = String.valueOf(lm.getBestProvider(criteria, true));
        //On demande une mise à jour de la localisation
        lm.requestLocationUpdates(bestProvider, 1000, 0, this);
        if(location != null){
            //On supprime les anciens marqueurs, si anciens marqueurs il y avait
            mMap.clear();
            //On créer l'objet latitude longitude
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
           //On positionne la caméra sur notre position
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //On place le curseur sur le premier résultat de la requête
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                //On ajoute le marqueur
                mMap.addMarker(new MarkerOptions().position(new LatLng(cursor.getDouble(cursor.getColumnIndex("key_position_y")), cursor.getDouble(cursor.getColumnIndex("key_position_x")))).title(cursor.getString(cursor.getColumnIndex("key_message"))));
                //On passe à la ligne suivante
                cursor.moveToNext();
            }
            //On ferme le curseur
            cursor.close();
        } else {
            lm.requestLocationUpdates(bestProvider, 1000, 0, this);
        }

    }

    //Quand on retourne sur la carte
    @Override
    protected void onResume() {
        super.onResume();
        //Cette condition est vraie seulement lorsque l'on reviens sur la carte après avoir ajouté un message.
        //On va donc ici ajouter le message à la base de données
        if(getIntent().getExtras() != null){
            //On récupère depuis l'intent la position du message
            double longitude = getIntent().getExtras().getDouble("longitude");
            double latitude = getIntent().getExtras().getDouble("latitude");
            //On récupère depuis l'intent le message
            String message = getIntent().getExtras().getString("message");
            //On prépare ces valeurs pour l'ajout à la base de données en les mettant dans un objet ContentValues
            ContentValues contentValues = new ContentValues();
            contentValues.put("key_position_x", longitude);
            contentValues.put("key_position_y", latitude);
            contentValues.put("key_message", message);
            MessagesContentProvider messagesContentProvider = new MessagesContentProvider();
            //On ajoute le message à la base de données
            messagesContentProvider.insert(Uri.parse("content://fr.esiea.lyra"), contentValues);
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

    //Lorsque l'on clique sur ajouter un message du fragment BottomFragment
    public void addMessage(View view) {
        Intent nextActivity = new Intent(getApplicationContext(), AddMessageActivity.class);
        //On envoi les coordonnées dans la prochaine activité pour l'ajout du message
        nextActivity.putExtra("longitude", latLng.longitude).putExtra("latitude", latLng.latitude);
        //On lance l'activité d'envoi de message
        startActivity(nextActivity);
    }
}
