package com.enseval.gcmuser.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Cart implements Parcelable {
    private int id;
    private Barang barang;
    private int qty;
    private String berat;
    private int hargaKonsumen;
    private int hargaSales;
    private int negoCount;
    private int id_shipto;
    private int id_billto;
    private int id_payment;
    private String payment_name;
    private Integer harga_final;
    private Integer history_nego_id;
    private String tgl_permintaan_kirim;
    private float ppn_seller;
    private String kota;
    private float ongkir;
    private boolean isChecked;
    private int company_id;
    private String nama_perusahaan;
    private String note;

    public Cart(int id, String note){
        this.id = id;
        this.note = note;
    }

    public Cart(int company_id, String nama_perusahaan, int id_shipto, String kota, float ongkir){
        this.company_id = company_id;
        this.nama_perusahaan = nama_perusahaan;
        this.id_shipto = id_shipto;
        this.kota = kota;
        this.ongkir = ongkir;
    }

    public Cart(int id, Barang barang, int qty, String berat, int hargaKonsumen, int hargaSales, int count, boolean isChecked) {
        this.id = id;
        this.barang = barang;
        this.qty = qty;
        this.berat = berat;
        this.hargaKonsumen = hargaKonsumen;
        this.hargaSales = hargaSales;
        this.negoCount = count;
        this.isChecked = isChecked;
    }

    public Cart(int id, Barang barang, int qty, String berat, boolean isChecked) {
        this.id = id;
        this.barang = barang;
        this.qty = qty;
        this.berat = berat;
        this.isChecked = isChecked;
    }

    public Cart(int id, Barang barang, int qty, String berat, int hargaKonsumen, int hargaSales,
                int count, boolean isChecked, int id_shipto, int id_billto, int id_payment, String payment_name, Integer harga_final, Integer history_nego_id, String tgl_permintaan_kirim, float ppn_seller) {
        this.id = id;
        this.barang = barang;
        this.qty = qty;
        this.berat = berat;
        this.hargaKonsumen = hargaKonsumen;
        this.hargaSales = hargaSales;
        this.negoCount = count;
        this.isChecked = isChecked;
        this.id_shipto = id_shipto;
        this.id_billto = id_billto;
        this.id_payment = id_payment;
        this.payment_name = payment_name;
        this.harga_final = harga_final;
        this.history_nego_id = history_nego_id;
        this.tgl_permintaan_kirim = tgl_permintaan_kirim;
        this.ppn_seller = ppn_seller;
    }

    public Cart(int id, Barang barang, int qty, String berat, boolean isChecked, int id_shipto, int id_billto, int id_payment, String payment_name, Integer harga_final, Integer history_nego_id, String tgl_permintaan_kirim, float ppn_seller) {
        this.id = id;
        this.barang = barang;
        this.qty = qty;
        this.berat = berat;
        this.isChecked = isChecked;
        this.id_shipto = id_shipto;
        this.id_billto = id_billto;
        this.id_payment = id_payment;
        this.payment_name = payment_name;
        this.harga_final = harga_final;
        this.history_nego_id = history_nego_id;
        this.tgl_permintaan_kirim = tgl_permintaan_kirim;
        this.ppn_seller = ppn_seller;
    }

    protected Cart(Parcel in) {
        id = in.readInt();
        barang = in.readParcelable(Barang.class.getClassLoader());
        qty = in.readInt();
        berat = in.readString();
        hargaKonsumen = in.readInt();
        hargaSales = in.readInt();
        negoCount = in.readInt();
        id_shipto = in.readInt();
        id_billto = in.readInt();
        id_payment = in.readInt();
        payment_name = in.readString();
        harga_final = in.readInt();
        history_nego_id = in.readInt();
        tgl_permintaan_kirim = in.readString();
        ppn_seller = in.readFloat();
        isChecked = in.readByte() != 0;
    }

    public static final Creator<Cart> CREATOR = new Creator<Cart>() {
        @Override
        public Cart createFromParcel(Parcel in) {
            return new Cart(in);
        }

        @Override
        public Cart[] newArray(int size) {
            return new Cart[size];
        }
    };

    public int getId() {
        return id;
    }

    public Barang getBarang() {
        return barang;
    }

    public int getQty() {
        return qty;
    }

    public int getHargaKonsumen() {
        return hargaKonsumen;
    }

    public int getHarga_final() {
        return harga_final;
    }

    public int getHistory_nego_id() {
        return history_nego_id;
    }

    public int getHargaSales() {
        return hargaSales;
    }

    public int getNegoCount() {
        return negoCount;
    }

    public void setBarang(Barang barang) {
        this.barang = barang;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public boolean isChecked() { return isChecked; }

    public void setChecked(boolean checked) { isChecked = checked; }

    public String getBerat() {
        return berat;
    }

    public int getId_shipto() { return id_shipto; }

    public String getPayment_name() {
        return payment_name;
    }

    public int getId_billto() { return id_billto; }

    public int getId_payment() { return id_payment; }

    public String getTgl_permintaan_kirim() {
        return tgl_permintaan_kirim;
    }

    public float getPpn_seller() {
        return ppn_seller;
    }

    public String getKota() {
        return kota;
    }

    public float getOngkir() {
        return ongkir;
    }

    public int getCompany_id() {
        return company_id;
    }

    public String getNote() {
        return note;
    }

    public String getNama_perusahaan() {
        return nama_perusahaan;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(barang, flags);
        dest.writeInt(qty);
        dest.writeString(berat);
        dest.writeInt(hargaKonsumen);
        dest.writeInt(hargaSales);
        dest.writeInt(negoCount);
        dest.writeInt(id_shipto);
        dest.writeInt(id_billto);
        dest.writeInt(id_payment);
        dest.writeString(payment_name);
        dest.writeInt(harga_final);
        dest.writeInt(history_nego_id);
        dest.writeString(tgl_permintaan_kirim);
        dest.writeFloat(ppn_seller);
        dest.writeByte((byte) (isChecked ? 1 : 0));
    }
}
