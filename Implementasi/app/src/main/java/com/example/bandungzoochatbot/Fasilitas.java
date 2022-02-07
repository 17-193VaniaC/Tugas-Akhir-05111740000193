package com.example.bandungzoochatbot;

import java.sql.Blob;
import java.sql.Time;

public class Fasilitas {
    private int Id;
    private String nama;
    private String latitude;
    private String longitude;
    private String deskripsi;
    private String jam_buka;
    private String jam_tutup;


    public Fasilitas(){}

    public Fasilitas(int id,String nama, String latitude, String longitude, String jam_buka, String jam_tutup){
        this.Id = id;
        this.nama = nama;
        this.latitude = latitude;
        this.longitude = longitude;
        this.jam_buka = jam_buka;
        this.jam_tutup = jam_tutup;

    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getJam_buka() {
        return jam_buka;
    }

    public void setJam_buka(String jam_buka) {
        this.jam_buka = jam_buka;
    }

    public String getJam_tutup() {
        return jam_tutup;
    }

    public void setJam_tutup(String jam_tutup) {
        this.jam_tutup = jam_tutup;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public Double getLatitude() {
        return Double.valueOf(latitude);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return Double.valueOf(longitude);
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String[] getCoordinate(){
        String[] coordinate= {this.latitude, this.longitude};
        return coordinate;
    }

}
