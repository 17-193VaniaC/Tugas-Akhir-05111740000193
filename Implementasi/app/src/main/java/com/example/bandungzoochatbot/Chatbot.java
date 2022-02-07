package com.example.bandungzoochatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;


//
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;


public class Chatbot extends AppCompatActivity {

    double PI = 3.141592653589793;
    double RADIUS = 6378160;

    List<Fasilitas> fasilitasList;
    List<Koleksi> koleksiList;

    private LocationManager locationManager;

    Toolbar toolbar;

    FloatingActionButton btnSend;
    private ImageButton btnMap;
    private ImageButton btnHelp;

    EditText editTextMsg;
    ListView listView;

    private double[] LatLong;
    private String HELP_MESSAGE="1. Tambahkan kata 'terdekat' pada pesan untuk menampilkan peta dan memperlihatkan satu fasilitas terdekat dengan lokasi anda\n" +
            "2. Fasilitas hanya bisa ditunjukan apabila nama fasilitas sama dengan yang terdata pada database";
//    private String HELP_FACILITY_LIST=
//            "\n- Kantin\n- Mushala dan toilet\n- Wahana permainan"+"\nContoh: Kantin terdekat di mana ya?";
    private String START_MESSAGE= "Halo Pengguna ZooSite! \n"+
            "- Tanyakan chatbot berbagai hal mengenai kebun binatang dengan mengirimkan pesan.\n"+
            "- Tambahkan kata 'terdekat' untuk melihat fasilitas terdekat pada peta. Contoh: Kantin terdekat\n" +
            "- Fasilitas yang ditanyakan mungkin tidak ditemukan karena tidak sama dengan nama yang terdata.";

    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);
        ListView mylistView;

        fasilitasList = new ArrayList<>();
        koleksiList = new ArrayList<>();

        LatLong = new double[2];

        btnSend = findViewById(R.id.btnSend);
        btnMap = findViewById(R.id.btnMap);
        btnHelp = findViewById(R.id.btnHelp);
        editTextMsg = findViewById(R.id.editTextMsg);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ChatAdapter(this, new ArrayList<ChatMessage>());
        listView.setAdapter(adapter);

        //Action bar
        toolbar = findViewById(R.id.toolbar_chatbot);
        setSupportActionBar(toolbar);

        // Connect to firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRefFasilitas = database.getReference("Fasilitas");
        DatabaseReference mtRefKoleksi = database.getReference("Koleksi");

        //Location GPS
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkLocationPermission();

        // Connection check + alert
        if (!isConnected(this)) {
            botsReply("Layanan ini membutuhkan internet. Harap periksa koneksi anda.");
        }
        botsReply(START_MESSAGE);
        // Chaquopy Python
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject pyTransModel = py.getModule("translate");
        PyObject pyModel = py.getModule("bot");

        myRefFasilitas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fasilitasList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Fasilitas fasilitas = data.getValue(Fasilitas.class);
                    String namafasilitas = data.child("nama").getValue(String.class);
                    fasilitasList.add(fasilitas);
                    Log.i("nama_fasilitas", namafasilitas);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.d("message", "Failed to read value.", error.toException());
            }
        });

        mtRefKoleksi.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                koleksiList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Koleksi fasilitas = data.getValue(Koleksi.class);
                    String namafasilitas = data.child("nama").getValue(String.class);
                    LatLng koleksi = new LatLng(fasilitas.getLatitude(), fasilitas.getLongitude());
                    koleksiList.add(fasilitas);
                    Log.i("nama_fasilitas", namafasilitas);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.d("message", "Failed to read value.", error.toException());
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMsg.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(Chatbot.this, "Masukan pertanyaan anda", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendMessage(message);
                if (isConnected(Chatbot.this)) {
                    if (isAskingLocation(message)) {
                        if(isLocationPermited()){
                        if (isExistinDatabase(fasilitasList, message)) {
                            if (!isGPSEnabled()) {
                                showGPSAlert();
                            } else {
                                checkLocationPermission();
                                locationManager.requestLocationUpdates(
                                        LocationManager.GPS_PROVIDER,
                                        1000, 0, new LocationListener() {
                                            @Override
                                            public void onLocationChanged(Location location) {
                                                if(location == null){
                                                    showLocationRequestFailed();
                                                }
                                                else {
                                                    LatLong[0] = location.getLatitude();
                                                    LatLong[1] = location.getLongitude();
                                                    ArrayList<Fasilitas> fasilitas1 = new ArrayList<>();
                                                    for (int i = 0; i < fasilitasList.size(); i++) {
                                                        if (message.toLowerCase().contains(fasilitasList.get(i).getNama().toLowerCase())) {
                                                            fasilitas1.add(fasilitasList.get(i));
                                                        }
                                                    }
                                                    locationManager.removeUpdates(this);
                                                    LatLng nearest_coordinate = getClosestFacility(fasilitas1, LatLong);
                                                    showFacilityinMap(nearest_coordinate);
                                                }

                                            }

                                            @Override
                                            public void onStatusChanged(String s, int i, Bundle bundle) {

                                            }

                                            @Override
                                            public void onProviderEnabled(String s) {

                                            }

                                            @Override
                                            public void onProviderDisabled(String s) {

                                            }
                                        }
                                );
                                botsReply("Menunjukan lokasi fasilitas pada peta...");
                            }}
                        else {
                            botsReply("Fasilitas tidak diketahui, silahkan tanya kembali");
                        }
                        }
                        else {
//                            checkBackgroundLocationPermission();
                            botsReply("Fitur ini membutuhkan izin akses lokasi. Harap izinkan aplikasi ini untuk mengakses lokasi.");
                        }
                    } else {
                        PyObject pyTranslate = pyTransModel.callAttr("trans_en", message);
                        String query = pyTranslate.toString();
                        PyObject pyAnswer = pyModel.callAttr("getAnswer", pyTranslate.toString());
                        botsReply(pyAnswer.toString());
                    }
                } else {
                    botsReply("Tidak dapat mendapatkan jawaban. Harap periksa koneksi anda.");
                }
                editTextMsg.setText("");
                listView.setSelection(adapter.getCount() - 1);
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected(Chatbot.this)) {
                    showThis("Tidak ada koneksi internet", "Tidak dapat membuka peta. Harap periksa koneksi internet anda.");
                }
                if(!isGPSEnabled()){
                    showThis("GPS tidak aktif","Harap periksa dan aktifkan lokasi anda.");
                }
                if(isLocationPermited()){
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                }
                else {
                    showThis("Izin Akses Lokasi", "Aplikasi ini membutuhkan akses lokasi. Untuk menggunakan Aplikasi ini, izinkan aplikasi ini mengakses lokasi.");
                }
            }
        });

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThis("Bantuan",HELP_MESSAGE);
            }
        });

    }

    private void botsReply(String message) {
        ChatMessage chatMessage = new ChatMessage(false, message);
        adapter.add(chatMessage);
    }

    private void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(true, message);
        adapter.add(chatMessage);
    }
    private void checkBackgroundLocationPermission() {
        if(Build.VERSION.SDK_INT > 28){
            if(ActivityCompat.checkSelfPermission(Chatbot.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Chatbot.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 44);
            }
        }
    }

    private void checkLocationPermission() {
        if(Build.VERSION.SDK_INT > 28){
            if(ActivityCompat.checkSelfPermission(Chatbot.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Chatbot.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 44);
            }
        }
        if (ActivityCompat.checkSelfPermission(Chatbot.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Chatbot.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 45);
        }
        if(ActivityCompat.checkSelfPermission(Chatbot.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Chatbot.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 46);
        }
    }

    private void showFacilityinMap(LatLng latLng) {
        Intent intent = new Intent(Chatbot.this, MapsActivity.class);
        intent.putExtra("COORDINATE_FACILITY", latLng);
        startActivity(intent);
    }

    private LatLng getClosestFacility(List<Fasilitas> fasilitas, double[] latLong){
        LatLng latLongFacility = null;
        if(fasilitas.size()==1){
            return latLongFacility = new LatLng(fasilitas.get(0).getLatitude(), fasilitas.get(0).getLongitude());
        }
        Long id = null;
        double dLat;
        double dLon;
        double closestDistance = 9999;
        double distance;
        for(int i=0;i<fasilitas.size();i++) {
            dLat = Math.toRadians(fasilitas.get(i).getLatitude() - latLong[0]);
            dLon = Math.toRadians(fasilitas.get(i).getLongitude() - latLong[1]);
            distance = RADIUS * 2 *
                    Math.asin(
                        Math.sqrt(
                            Math.pow(Math.sin(dLat/2),2) + Math.cos(Math.toRadians(latLong[0])) * Math.cos(Math.toRadians(fasilitas.get(i).getLatitude())) * Math.pow(Math.sin(dLon/2),2)));
            Log.i("JARAK", String.valueOf(distance));
//            botsReply(String.valueOf(distance);
            if(distance < closestDistance){
                latLongFacility = new LatLng(fasilitas.get(i).getLatitude(), fasilitas.get(i).getLongitude());
            }
        }
        return latLongFacility;
    }

    private Boolean isAskingLocation(String message){
        return message.toLowerCase().contains("terdekat");
    }

    private Boolean isExistinDatabase(List<Fasilitas> fasilitas, String query){
        for(int i=0;i<fasilitas.size();i++){
            if(query.toLowerCase().contains(fasilitas.get(i).getNama().toLowerCase())){
                Log.i("CEK_LOKASI", fasilitas.get(i).getNama());
                return true;
            }
        }
        return false;
    }

    private boolean isConnected(Chatbot chatbot){
        ConnectivityManager connectivityManager = (ConnectivityManager) chatbot.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(connectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(connectivityManager.TYPE_MOBILE);

        return (wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected());
    }

    private boolean isGPSEnabled(){
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private Boolean isLocationPermited(){
        if (ActivityCompat.checkSelfPermission(Chatbot.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
         return true;
        }
        return false;
    }

    private void showGPSAlert() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Aktifkan GPS");
        builder.setMessage("Fitur ini membutuhkan layanan GPS. Harap periksa kembali GPS anda.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLocationRequestFailed(){
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Gagal Mendapatkan Lokasi");
        builder.setMessage("Gagal mendapatkan lokasi anda, silahkan coba kembali");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showThis(String title, String content){
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

}
