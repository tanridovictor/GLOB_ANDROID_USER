package com.enseval.gcmuser.Model;

import com.google.gson.annotations.SerializedName;

public class Order {
    @SerializedName("id_transaction")
    private String transactionId;
    private String status;
    @SerializedName("create_date")
    private String createDate;
    @SerializedName("update_date")
    private String updateDate;
    private long total;
    @SerializedName("status_payment")
    private String statusPayment;
    @SerializedName("ongkos_kirim")
    private double ongkir;
    private Long harga_final;
    private float ppn_seller;

    public Order(String transactionId, String status, String createDate, String updateDate, long total, double ongkir, float ppn_seller) {
        this.transactionId = transactionId;
        this.status = status;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.total = total;
        this.ongkir = ongkir;
        this.ppn_seller = ppn_seller;
    }

    public Order(String transactionId, String status, String createDate, String updateDate, long total, double ongkir, Long harga_final, float ppn_seller) {
        this.transactionId = transactionId;
        this.status = status;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.total = total;
        this.ongkir = ongkir;
        this.harga_final = harga_final;
        this.ppn_seller = ppn_seller;
    }

    public Order(String transactionId, String status, String createDate, String updateDate, long total) {
        this.transactionId = transactionId;
        this.status = status;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.total = total;
    }

    public Order(String transactionId, String status, String statusPayment, String createDate, String updateDate, long total) {
        this.transactionId = transactionId;
        this.status = status;
        this.statusPayment = statusPayment;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.total = total;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStatus() {
        return status;
    }

    public String getCreateDate() {
        return createDate;
    }

    public long getTotal() {
        return total;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public double getOngkir() {
        return ongkir;
    }

    public String getStatusPayment() {
        return statusPayment;
    }

    public Long getHarga_final() {
        return harga_final;
    }

    public float getPpn_seller() {
        return ppn_seller;
    }
}
