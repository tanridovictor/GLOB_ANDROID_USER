package com.enseval.gcmuser.API;

public class JSONRequestTransaksi {

    public String id_sales;
    public String company_id_seller;
    public String company_id_transaction;

    public JSONRequestTransaksi(String id_sales, String company_id_seller, String company_id_transaction) {
        this.id_sales = id_sales;
        this.company_id_seller = company_id_seller;
        this.company_id_transaction = company_id_transaction;
    }

    public String getId_sales() {
        return id_sales;
    }

    public String getCompany_id_seller() {
        return company_id_seller;
    }

    public String getCompany_id_transaction() {
        return company_id_transaction;
    }
}
