package com.work.catch_camera;

public class MainData {

    private byte[] image;
    private String location;
    private String position;
    private String date;

    public MainData() {

    }

    public MainData(byte[] image, String location, String position, String date){
        this.image = image;
        this.location = location;
        this.position = position;
        this.date = date;
    }

    public byte[] getImage() {
        return image;
    }
    public void setImage(byte[] image) { this.image = image; }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

}
