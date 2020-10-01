package com.enseval.gcmuser.Model;

public class Provinsi {
    private String provinceId;
    private String provinceName;

    public Provinsi(String provinceId, String provinceName) {
        this.provinceId = provinceId;
        this.provinceName = provinceName;
    }

    public String getProvinceId() {
        return provinceId;
    }

    public String getProvinceName() {
        return provinceName;
    }
}
