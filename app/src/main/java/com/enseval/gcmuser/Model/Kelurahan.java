package com.enseval.gcmuser.Model;

public class Kelurahan {
    private String cityIdidKelurahan;
    private String idKecamatan;
    private String namaKelurahan;

    public Kelurahan(String cityIdidKelurahan, String idKecamatan, String namaKelurahan) {
        this.cityIdidKelurahan = cityIdidKelurahan;
        this.idKecamatan = idKecamatan;
        this.namaKelurahan = namaKelurahan;
    }

    public String getCityIdidKelurahan() {
        return cityIdidKelurahan;
    }

    public String getIdKecamatan() {
        return idKecamatan;
    }

    public String getNamaKelurahan() {
        return namaKelurahan;
    }
}
