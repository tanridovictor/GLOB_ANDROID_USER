package com.enseval.gcmuser.Model;

public class AkunBuyer {
    private String nama;
    private String username;
    private String no_hp;
    private String email;
    private String status;

    public AkunBuyer(String nama, String username, String no_hp, String email, String status) {
        this.nama = nama;
        this.username = username;
        this.no_hp = no_hp;
        this.email = email;
        this.status = status;
    }

    public String getNama() {
        return nama;
    }

    public String getUsername() {
        return username;
    }

    public String getNo_hp() {
        return no_hp;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }
}
