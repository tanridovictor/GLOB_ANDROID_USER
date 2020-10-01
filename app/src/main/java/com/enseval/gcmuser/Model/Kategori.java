package com.enseval.gcmuser.Model;

import java.io.Serializable;

public class Kategori implements Serializable {
    private int id;
    private String nama;

    public Kategori(int id, String nama) {
        this.id = id;
        this.nama = nama;
    }

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }
}
