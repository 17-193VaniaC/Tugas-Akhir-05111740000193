package com.example.bandungzoochatbot;

import java.sql.Blob;

public class Koleksi{
    private int id;
    private String nama;
    private String latitude;
    private String longitude;
    private String deskripsi;

    public Koleksi(){}
    public Koleksi(int id, String nama, String latitude, String longitude, Blob foto){
        this.id = id;
        this.nama = nama;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
