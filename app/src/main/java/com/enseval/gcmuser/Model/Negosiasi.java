package com.enseval.gcmuser.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Negosiasi implements Parcelable {
    private Barang barang;
    private int berat;
    private int hargaKonsumen;
    private int hargaSales;
    private int negoCount;
    private int harga_nego;
    private int harga_nego_2;
    private int harga_nego_3;
    private int harga_sales_1;
    private int harga_sales_2;
    private int harga_sales_3;
    private int idCart;
    private int history_id;
    private int qty;
    private boolean responSales;
    private String time_respon;
    private int harga_final;
    private String timestamp_respon;

    public Negosiasi(Barang barang, int hargaKonsumen, int hargaSales, int negoCount, int idCart, int history_id, int qty) {
        this.barang = barang;
        this.hargaKonsumen = hargaKonsumen;
        this.hargaSales = hargaSales;
        this.negoCount = negoCount;
        this.idCart = idCart;
        this.history_id = history_id;
        this.qty = qty;
    }

    public String getTimestamp_respon() {
        return timestamp_respon;
    }

    public Negosiasi(Barang barang, int berat, int hargaKonsumen, int hargaSales, int negoCount,
                     int harga_nego, int harga_nego_2, int harga_nego_3,
                     int harga_sales_1, int harga_sales_2, int harga_sales_3,
                     boolean responSales, int idCart, int history_id, int qty, String time_respon, int harga_final, String timestamp_respon) {
        this.barang = barang;
        this.berat = berat;
        this.hargaKonsumen = hargaKonsumen;
        this.hargaSales = hargaSales;
        this.negoCount = negoCount;
        this.harga_nego = harga_nego;
        this.harga_nego_2 = harga_nego_2;
        this.harga_nego_3 = harga_nego_3;
        this.harga_sales_1 = harga_sales_1;
        this.harga_sales_2 = harga_sales_2;
        this.harga_sales_3 = harga_sales_3;
        this.responSales = responSales;
        this.idCart = idCart;
        this.history_id = history_id;
        this.qty = qty;
        this.time_respon = time_respon;
        this.harga_final = harga_final;
        this.timestamp_respon = timestamp_respon;
    }

    public int getBerat() {
        return berat;
    }

    public String getTime_respon() {
        return time_respon;
    }

    public int getHarga_sales_1() {
        return harga_sales_1;
    }

    public int getHarga_sales_2() {
        return harga_sales_2;
    }

    public int getHarga_sales_3() {
        return harga_sales_3;
    }

    protected Negosiasi(Parcel in) {
        barang = in.readParcelable(Barang.class.getClassLoader());
        hargaKonsumen = in.readInt();
        hargaSales = in.readInt();
        negoCount = in.readInt();
        idCart = in.readInt();

    }

    public static final Creator<Negosiasi> CREATOR = new Creator<Negosiasi>() {
        @Override
        public Negosiasi createFromParcel(Parcel in) {
            return new Negosiasi(in);
        }

        @Override
        public Negosiasi[] newArray(int size) {
            return new Negosiasi[size];
        }
    };

    public Barang getBarang() {
        return barang;
    }

    public int getHargaKonsumen() {
        return hargaKonsumen;
    }

    public int getHargaSales() {
        return hargaSales;
    }

    public int getNegoCount() {
        return negoCount;
    }

    public int getHarga_final() {
        return harga_final;
    }

    public int getIdCart() {
        return idCart;
    }

    public int getHistory_id() {
        return history_id;
    }

    public int getQty() {
        return qty;
    }

    public boolean isResponSales() {
        return responSales;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getHarga_nego() {
        return harga_nego;
    }

    public int getHarga_nego_2() {
        return harga_nego_2;
    }

    public int getHarga_nego_3() {
        return harga_nego_3;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(barang, flags);
        dest.writeInt(hargaKonsumen);
        dest.writeInt(hargaSales);
        dest.writeInt(negoCount);
        dest.writeInt(idCart);
    }
}
