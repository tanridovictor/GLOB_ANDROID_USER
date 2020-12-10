package com.enseval.gcmuser.Model;

public class Komplain {
    private int posissi;
    private int id_rb;
    private String complain;

    public Komplain(int posissi, int id_rb, String complain) {
        this.posissi = posissi;
        this.id_rb = id_rb;
        this.complain = complain;
    }

    public int getPosissi() {
        return posissi;
    }

    public int getId_rb() {
        return id_rb;
    }

    public String getComplain() {
        return complain;
    }

    public void setPosissi(int posissi) {
        this.posissi = posissi;
    }

    public void setId_rb(int id_rb) {
        this.id_rb = id_rb;
    }

    public void setComplain(String complain) {
        this.complain = complain;
    }
}
