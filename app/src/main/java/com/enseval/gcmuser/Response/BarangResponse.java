package com.enseval.gcmuser.Response;

import com.enseval.gcmuser.Model.Barang;

import java.util.ArrayList;

public class BarangResponse {
    String status;
    ArrayList<Barang> data;

    public BarangResponse(String status, ArrayList<Barang> data) {
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public ArrayList<Barang> getData() {
        return data;
    }
}
