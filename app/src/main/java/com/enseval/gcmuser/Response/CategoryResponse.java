package com.enseval.gcmuser.Response;

import com.enseval.gcmuser.Model.Kategori;

import java.util.ArrayList;

public class CategoryResponse {
    String status;
    ArrayList<Kategori> data;

    public CategoryResponse(String status, ArrayList<Kategori> data) {
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public ArrayList<Kategori> getData() {
        return data;
    }
}
