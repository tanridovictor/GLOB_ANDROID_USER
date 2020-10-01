package com.enseval.gcmuser.Model;

public class Alamat {
    private String kelurahan;
    private String kecamatan;
    private String kota;
    private String provinsi;
    private String kodepos;
    private String notelp;
    private int company_id;
    private int id;
    private String alamat;
    private String nama_perusahaan;
    private String tgl_permintaan_kirim;
    private String shipto_active;
    private String billto_active;

    public Alamat(String kelurahan, String kecamatan, String kota, String provinsi, String kodepos, String alamat, int id, String shipto_active, String billto_active) {
        this.kelurahan = kelurahan;
        this.kecamatan = kecamatan;
        this.kota = kota;
        this.provinsi = provinsi;
        this.kodepos = kodepos;
        this.alamat = alamat;
        this.id = id;
        this.shipto_active = shipto_active;
        this.billto_active = billto_active;
    }

    public Alamat(int id, String nama_perusahaan, String alamat, String provinsi, String kota, String kecamatan, String kelurahan, String kodepos, String tgl_permintaan_kirim){
        this.id = id;
        this.nama_perusahaan = nama_perusahaan;
        this.alamat = alamat;
        this.provinsi = provinsi;
        this.kota = kota;
        this.kecamatan = kecamatan;
        this.kelurahan = kelurahan;
        this.kodepos = kodepos;
        this.tgl_permintaan_kirim = tgl_permintaan_kirim;
    }

    public Alamat(String alamat){
        this.alamat=alamat;
    }

    public int getId() {
        return id;
    }

    public String getKelurahan() {
        return kelurahan;
    }

    public String getKecamatan() {
        return kecamatan;
    }

    public String getKota() {
        return kota;
    }

    public String getProvinsi() {
        return provinsi;
    }

    public String getKodepos() {
        return kodepos;
    }

    public String getNotelp() {
        return notelp;
    }

    public int getCompany_id() {
        return company_id;
    }

    public String getShipto_active() {
        return shipto_active;
    }

    public String getBillto_active() {
        return billto_active;
    }

    public String getAlamat() {
        return alamat;
    }

    public String getNama_perusahaan() {
        return nama_perusahaan;
    }

    public String getTgl_permintaan_kirim() {
        return tgl_permintaan_kirim;
    }
}
