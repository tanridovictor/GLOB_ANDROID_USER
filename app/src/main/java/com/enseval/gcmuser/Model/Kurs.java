package com.enseval.gcmuser.Model;

public class Kurs {
    int id;
    float nominal;
    int company_id;

    public Kurs(int id, float nominal, int company_id) {
        this.id = id;
        this.nominal = nominal;
        this.company_id = company_id;
    }

    public int getId() {
        return id;
    }

    public float getNominal() {
        return nominal;
    }

    public int getCompany_id() {
        return company_id;
    }
}
