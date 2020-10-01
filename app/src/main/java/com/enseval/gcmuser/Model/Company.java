package com.enseval.gcmuser.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Company implements Parcelable {
    private int id;
    private String namaPerusahaan;
    private int tipeBisnis;
    private boolean checked;
    private ArrayList<Cart> listCart;

    public Company(int id, String namaPerusahaan){
        this.id = id;
        this.namaPerusahaan = namaPerusahaan;
    }

    public Company(int id, String namaPerusahaan, int tipeBisnis) {
        this.id = id;
        this.namaPerusahaan = namaPerusahaan;
        this.tipeBisnis = tipeBisnis;
    }

    public Company(int id, String namaPerusahaan, ArrayList<Cart> listCart, boolean checked) {
        this.id = id;
        this.namaPerusahaan = namaPerusahaan;
        this.listCart = listCart;
        this.checked = checked;
    }

    public Company(int id, String namaPerusahaan, int tipeBisnis, boolean checked) {
        this.id = id;
        this.namaPerusahaan = namaPerusahaan;
        this.checked = checked;
        this.tipeBisnis = tipeBisnis;
    }

    protected Company(Parcel in) {
        id = in.readInt();
        namaPerusahaan = in.readString();
        listCart = in.createTypedArrayList(Cart.CREATOR);
    }

    public static final Creator<Company> CREATOR = new Creator<Company>() {
        @Override
        public Company createFromParcel(Parcel in) {
            return new Company(in);
        }

        @Override
        public Company[] newArray(int size) {
            return new Company[size];
        }
    };

    public boolean isChecked() {
        return checked;
    }

    public int getId() {
        return id;
    }

    public String getNamaPerusahaan() {
        return namaPerusahaan;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getTipeBisnis() {
        return tipeBisnis;
    }

    public ArrayList<Cart> getListCart() {
        return listCart;
    }

    public void setListCart(ArrayList<Cart> listCart) {
        this.listCart = listCart;
    }

    public void addCart(Cart cart){
        this.listCart.add(cart);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(namaPerusahaan);
        dest.writeTypedList(listCart);
    }
}
