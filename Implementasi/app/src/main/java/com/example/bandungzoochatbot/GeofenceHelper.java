package com.example.bandungzoochatbot;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

public class GeofenceHelper extends ContextWrapper {

    private float GEOFENCE_RADIUS = 50;
    PendingIntent pendingIntent;
    public GeofenceHelper(Context base) {
        super(base);
    }

    public GeofencingRequest setGeofencingRequest(Geofence geofence){
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    public Geofence setGeofence(String id, LatLng latLng){
        return new Geofence.Builder()
                .setRequestId(id)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }

    public PendingIntent getGeofencePendingIntent(Koleksi koleksi) {
        if (pendingIntent != null) {
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBot.class);
        intent.putExtra("NAMA_HEWAN", koleksi.getNama());
        intent.putExtra("DESKRIPSI", koleksi.getDeskripsi());
        return PendingIntent.getBroadcast(this, koleksi.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public String getError(Exception exception){
        if(exception instanceof ApiException){
            ApiException apiException = (ApiException) exception;
            switch (apiException.getStatusCode()){
                case GeofenceStatusCodes
                        .API_NOT_CONNECTED:
                    return "API NOT CONNECTED";
                case GeofenceStatusCodes
                        .GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE NOT AVAILABLE";
                case GeofenceStatusCodes
                        .GEOFENCE_REQUEST_TOO_FREQUENT:
                    return "GEOFENCE REQUEST TOO FREQUENT";
            }
        }
        return exception.getLocalizedMessage();
    }
}
