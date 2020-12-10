package com.enseval.gcmuser.API;

public class JSONRequest {
    public String query;

    public String username;
    public String kode;
    public String email;
    public String kode_verifikasi;
    public String tujuan;
    public String tipe;

    public String id_sales;
    public String company_id_seller;

    public JSONRequest(String query) {
        this.query = query;
    }

    public JSONRequest(String tujuan, String kode, String tipe) {
        this.tujuan = tujuan;
        this.kode = kode;
        this.tipe = tipe;
    }

    public JSONRequest(String tujuan, String kode, String username, String tipe) {
        this.tujuan = tujuan;
        this.kode = kode;
        this.username = username;
        this.tipe = tipe;
    }

    public JSONRequest(String id_sales, String company_id_seller) {
        this.id_sales = id_sales;
        this.company_id_seller = company_id_seller;
    }


    public String getQuery() {
        return query;
    }

    public String getEmail() {
        return email;
    }

    public String getKode_verifikasi() {
        return kode_verifikasi;
    }

    public String getUsername() {
        return username;
    }

    public String getKode() {
        return kode;
    }

    public String getTujuan() {
        return tujuan;
    }

    public String getId_sales() {
        return id_sales;
    }

    public String getCompany_id_seller() {
        return company_id_seller;
    }

    public String getTipe() {
        return tipe;
    }
}
