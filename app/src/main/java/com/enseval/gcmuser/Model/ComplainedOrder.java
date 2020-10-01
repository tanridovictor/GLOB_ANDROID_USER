package com.enseval.gcmuser.Model;

public class ComplainedOrder {
    private OrderDetail orderDetail;
    private String jenisKomplain;
    private String catatan;

    public ComplainedOrder(OrderDetail orderDetail, String jenisKomplain, String catatan) {
        this.orderDetail = orderDetail;
        this.jenisKomplain = jenisKomplain;
        this.catatan = catatan;
    }

    public OrderDetail getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(OrderDetail orderDetail) {
        this.orderDetail = orderDetail;
    }

    public String getJenisKomplain() {
        return jenisKomplain;
    }

    public void setJenisKomplain(String jenisKomplain) {
        this.jenisKomplain = jenisKomplain;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }
}
