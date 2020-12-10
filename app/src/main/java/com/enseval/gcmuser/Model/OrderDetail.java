package com.enseval.gcmuser.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class OrderDetail implements Parcelable {
    private int id;
    @SerializedName("transaction_id")
    private String transactionId;
    @SerializedName("nama")
    private String namaBarang;
    @SerializedName("foto")
    private String fotoUrl;
    private String alias;
    private int qty;
    private long harga;
    private String berat;
    private String batchNumber;
    private String expDate;
    private int qtyDiterima;
    private int shipto_id;
    private int billto_id;
    private int payment_id;
    private String nama_perusahaan;
    private String tgl_permintaan_kirim;
    private int harga_final;
    private String notes_complain;
    private String note;
    private String flag_foto;
    private String barang_id;



    public OrderDetail(int id, String transactionId, String namaBarang, String fotoUrl, int qty, long harga, String berat, String alias, int shipto_id, int billto_id, int payment_id, String batchNumber, String expDate, int qtyDiterima, int harga_final, String note, String flag_foto, String barang_id) {
        this.id = id;
        this.transactionId = transactionId;
        this.namaBarang = namaBarang;
        this.fotoUrl = fotoUrl;
        this.qty = qty;
        this.berat = berat;
        this.harga = harga;
        this.alias = alias;
        this.shipto_id = shipto_id;
        this.billto_id=billto_id;
        this.payment_id=payment_id;
        this.batchNumber = batchNumber;
        this.expDate = expDate;
        this.qtyDiterima = qtyDiterima;
        this.harga_final = harga_final;
        this.note = note;
        this.flag_foto = flag_foto;
        this.barang_id = barang_id;
    }

    public OrderDetail(int id, String transactionId, String namaBarang, String fotoUrl, int qty, long harga, String berat, String alias, String notes_complain, String batchNumber, String expDate, int qtyDiterima, int harga_final, String note, String flag_foto) {
        this.id = id;
        this.transactionId = transactionId;
        this.namaBarang = namaBarang;
        this.fotoUrl = fotoUrl;
        this.qty = qty;
        this.berat = berat;
        this.harga = harga;
        this.alias = alias;
        this.notes_complain = notes_complain;
        this.batchNumber = batchNumber;
        this.expDate = expDate;
        this.qtyDiterima = qtyDiterima;
        this.harga_final = harga_final;
        this.note = note;
        this.flag_foto = flag_foto;
    }

    public OrderDetail(int id, String transactionId, String namaBarang, String fotoUrl, int qty, String berat, long harga, String batchNumber, String expDate, int qtyDiterima, String alias) {
        this.id = id;
        this.transactionId = transactionId;
        this.namaBarang = namaBarang;
        this.fotoUrl = fotoUrl;
        this.qty = qty;
        this.berat = berat;
        this.harga = harga;
        this.batchNumber = batchNumber;
        this.expDate = expDate;
        this.qtyDiterima = qtyDiterima;
        this.alias = alias;
    }

    protected OrderDetail(Parcel in) {
        id = in.readInt();
        transactionId = in.readString();
        namaBarang = in.readString();
        fotoUrl = in.readString();
        qty = in.readInt();
        berat = in.readString();
        harga = in.readLong();
        batchNumber = in.readString();
        expDate = in.readString();
        qtyDiterima = in.readInt();
        alias = in.readString();
    }

    public static final Creator<OrderDetail> CREATOR = new Creator<OrderDetail>() {
        @Override
        public OrderDetail createFromParcel(Parcel in) {
            return new OrderDetail(in);
        }

        @Override
        public OrderDetail[] newArray(int size) {
            return new OrderDetail[size];
        }
    };

    public String getTransactionId() {
        return transactionId;
    }

    public String getNama_perusahaan() {
        return nama_perusahaan;
    }

    public String getTgl_permintaan_kirim() {
        return tgl_permintaan_kirim;
    }

    public int getShipto_id() {
        return shipto_id;
    }

    public int getBillto_id() {
        return billto_id;
    }

    public int getPayment_id() {
        return payment_id;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public int getQty() {
        return qty;
    }

    public long getHarga() {
        return harga;
    }

    public String getBarang_id() {
        return barang_id;
    }

    public int getId() {
        return id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public String getExpDate() {
        return expDate;
    }

    public int getQtyDiterima() {
        return qtyDiterima;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getBerat() {
        return berat;
    }

    public String getAlias() {
        return alias;
    }

    public String getNote() {
        return note;
    }

    public String getNotes_complain() {
        return notes_complain;
    }

    public int getHarga_final() {
        return harga_final;
    }

    public String getFlag_foto() {
        return flag_foto;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(transactionId);
        dest.writeString(namaBarang);
        dest.writeString(fotoUrl);
        dest.writeInt(qty);
        dest.writeString(berat);
        dest.writeLong(harga);
        dest.writeString(batchNumber);
        dest.writeString(expDate);
        dest.writeInt(qtyDiterima);
        dest.writeString(alias);
    }
}
