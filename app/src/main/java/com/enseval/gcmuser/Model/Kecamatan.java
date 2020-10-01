package com.enseval.gcmuser.Model;

public class Kecamatan {
    private String idKecamatan;
    private String idKota;
    private String namaKecamatan;

    public Kecamatan(String idKecamatan, String idKota, String namaKecamatan) {
        this.idKecamatan = idKecamatan;
        this.idKota = idKota;
        this.namaKecamatan = namaKecamatan;
    }

    public String getIdKecamatan() {
        return idKecamatan;
    }

    public String getIdKota() {
        return idKota;
    }

    public String getNamaKecamatan() {
        return namaKecamatan;
    }
}