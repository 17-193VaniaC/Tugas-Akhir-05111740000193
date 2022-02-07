package com.example.bandungzoochatbot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getStringExtra("NAMA_HEWAN")!=null){
            Toast.makeText(context, intent.getStringExtra("NAMA_HEWAN")+"\n"+intent.getStringExtra("DESKRIPSI"), Toast.LENGTH_LONG).show();
            Log.i("INTENT_MAP", intent.getStringExtra("NAMA_HEWAN"));
        }
        else{
            Toast.makeText(context, "Failed to get data", Toast.LENGTH_SHORT).show();
        }
    }

}
