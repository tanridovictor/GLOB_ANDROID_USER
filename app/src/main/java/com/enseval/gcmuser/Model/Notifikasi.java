package com.enseval.gcmuser.Model;

public class Notifikasi {
    private int id;
    private String nama_barang;
    private String seller_nama;
    private String read_flag;
    private String date;
    private String source;
    private String status;
    private String buyer_nama;
    private int barang_id;
    private String timestamp_kirim;

//    public Notifikasi(int barang_id, String barang_nama, String seller_nama, String read_flag, String date, String source, String status, String buyer_nama) {
//        this.barang_id = barang_id;
//        this.barang_nama = barang_nama;
//        this.seller_nama = seller_nama;
//        this.read_flag = read_flag;
//        this.date = date;
//        this.source = source;
//        this.status = status;
//        this.buyer_nama = buyer_nama;
//    }


    public Notifikasi(String barang_nama, String seller_nama, String date, String status, String buyer_nama, int barang_id, String timestamp_kirim) {
        this.nama_barang = barang_nama;
        this.seller_nama = seller_nama;
        this.date = date;
        this.status = status;
        this.buyer_nama = buyer_nama;
        this.barang_id = barang_id;
        this.timestamp_kirim = timestamp_kirim;
    }

    public int getBarang_id() {
        return barang_id;
    }

    public String getStatus() {
        return status;
    }

    public String getBuyer_nama() {
        return buyer_nama;
    }

    public int getId() {
        return id;
    }

    public String getBarang_nama() {
        return nama_barang;
    }

    public String getSeller_nama() {
        return seller_nama;
    }

    public String getRead_flag() {
        return read_flag;
    }

    public String getDate() {
        return date;
    }

    public String getSource() {
        return source;
    }

    public String getTimestamp_kirim() {
        return timestamp_kirim;
    }
}
