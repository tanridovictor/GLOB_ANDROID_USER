package com.enseval.gcmuser.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Barang implements Serializable, Parcelable {
    private String nama;
    private int id;
    @SerializedName("price")
    private double harga;
    @SerializedName("price_terendah")
    private double harga_terendah;
    private String foto;
    @SerializedName("category_id")
    private int categoryId;
    private String berat;
    private String volume;
    private String ex;
    private String deskripsi;
    @SerializedName("company_id")
    private int companyId;
    @SerializedName("nama_perusahaan")
    private String namaPerusahaan;
    private String alias;
    @SerializedName("jumlah_min_nego")
    private float minNego;
    @SerializedName("jumlah_min_beli")
    private float minBeli;
    private float persen_nego_1;
    private float persen_nego_2;
    private float persen_nego_3;
    @SerializedName("kurs")
    private float kursIdr;

    private String kode_barang;
    private String flag_foto;

    public Barang(String nama, int id, double harga, double harga_terendah, String berat, String foto, int categoryId, int companyId, String nama_perusahaan, String alias, float minNego, float minBeli, float persen_nego_1, float persen_nego_2, float persen_nego_3, float kursIdr, String kode_barang, String flag_foto) {
        this.nama = nama;
        this.id = id;
        this.harga = harga;
        this.harga_terendah = harga_terendah;
        this.berat = berat;
        this.foto = foto;
        this.categoryId = categoryId;
        this.companyId = companyId;
        this.namaPerusahaan = nama_perusahaan;
        this.alias=alias;
        this.minNego=minNego;
        this.minBeli=minBeli;
        this.persen_nego_1 = persen_nego_1;
        this.persen_nego_2 = persen_nego_2;
        this.persen_nego_3 = persen_nego_3;
        this.kursIdr = kursIdr;
        this.kode_barang = kode_barang;
        this.flag_foto = flag_foto;
    }

    public Barang(int companyId, String namaPerusahaan) {
        this.companyId = companyId;
        this.namaPerusahaan = namaPerusahaan;
    }

    public Barang(int id, String nama, String berat, String alias, double harga, String foto, int categoryId, String namaPerusahaan, int companyId, float kurs){
        this.id = id;
        this.nama = nama;
        this.berat = berat;
        this.alias = alias;
        this.harga = harga;
        this.foto = foto;
        this.categoryId = categoryId;
        this.namaPerusahaan = namaPerusahaan;
        this.companyId = companyId;
        this.kursIdr = kurs;
    }

    protected Barang(Parcel in) {
        nama = in.readString();
        id = in.readInt();
        harga = in.readDouble();
        harga_terendah = in.readDouble();
        foto = in.readString();
        categoryId = in.readInt();
        companyId = in.readInt();
        namaPerusahaan = in.readString();
        alias = in.readString();
        persen_nego_1 = in.readFloat();
        persen_nego_2 = in.readFloat();
        persen_nego_3 = in.readFloat();
        kursIdr = in.readFloat();
        kode_barang = in.readString();
        flag_foto = in.readString();
        //minNego = in.readInt();
    }

    public Barang(String nama, int id, double harga, int harga_terendah, String foto, int categoryId, String berat, String volume, String ex, String deskripsi, int companyId, String namaPerusahaan, String alias, float persen_nego_1, float persen_nego_2, float persen_nego_3, float kursIdr) {
        this.nama = nama;
        this.id = id;
        this.harga = harga;
        this.harga_terendah = harga_terendah;
        this.foto = foto;
        this.categoryId = categoryId;
        this.berat = berat;
        this.volume = volume;
        this.ex = ex;
        this.deskripsi = deskripsi;
        this.companyId = companyId;
        this.namaPerusahaan = namaPerusahaan;
        this.alias = alias;
        this.persen_nego_1 = persen_nego_1;
        this.persen_nego_2 = persen_nego_2;
        this.persen_nego_3 = persen_nego_3;
        this.kursIdr = kursIdr;
        //this.minNego = minNego;
    }

    public static final Creator<Barang> CREATOR = new Creator<Barang>() {
        @Override
        public Barang createFromParcel(Parcel in) {
            return new Barang(in);
        }

        @Override
        public Barang[] newArray(int size) {
            return new Barang[size];
        }
    };

    public Barang(String namaBarang, int idBarang, double harga, double harga_terendah, String foto, int categoryId, int companyId, String nama_perusahaan, String alias, float persen_nego_1, float persen_nego_2, float persen_nego_3, float kursIdr, String kode_barang, String flag_foto) {
        this.nama = namaBarang;
        this.id = idBarang;
        this.harga = harga;
        this.harga_terendah = harga_terendah;
        this.foto = foto;
        this.categoryId = categoryId;
        this.companyId = companyId;
        this.namaPerusahaan = nama_perusahaan;
        this.alias = alias;
        this.persen_nego_1 = persen_nego_1;
        this.persen_nego_2 = persen_nego_2;
        this.persen_nego_3 = persen_nego_3;
        this.kursIdr = kursIdr;
        this.kode_barang = kode_barang;
        this.flag_foto =flag_foto;
        //this.minNego = minNego;
    }

    public String getFlag_foto() {
        return flag_foto;
    }

    public float getMinBeli() {
        return minBeli;
    }

    public String getKode_barang() {
        return kode_barang;
    }

    public float getMinNego() {
        return minNego;
    }

    public String getNama() {
        return nama;
    }

    public int getId() {
        return id;
    }

    public double getHarga() {
        return harga;
    }

    public String getFoto() {
        return foto;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getBerat() {
        return berat;
    }

    public String getVolume() {
        return volume;
    }

    public String getEx() {
        return ex;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getNamaPerusahaan() {
        return namaPerusahaan;
    }

    public int getCompanyId() {
        return companyId;
    }

    public double getHarga_terendah() {
        return harga_terendah;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public float getPersen_nego_1() {
        return persen_nego_1;
    }

    public float getPersen_nego_2() {
        return persen_nego_2;
    }

    public float getPersen_nego_3() {
        return persen_nego_3;
    }

    public float getKursIdr() {
        return kursIdr;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nama);
        dest.writeInt(id);
        dest.writeDouble(harga);
        dest.writeDouble(harga_terendah);
        dest.writeString(foto);
        dest.writeInt(categoryId);
        dest.writeInt(companyId);
        dest.writeString(namaPerusahaan);
        dest.writeString(alias);
        dest.writeFloat(persen_nego_1);
        dest.writeFloat(persen_nego_2);
        dest.writeFloat(persen_nego_3);
        dest.writeFloat(kursIdr);
        dest.writeString(kode_barang);
        dest.writeString(flag_foto);
    }
}
