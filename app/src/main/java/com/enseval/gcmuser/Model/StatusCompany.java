package com.enseval.gcmuser.Model;

import java.util.ArrayList;

public class StatusCompany {
    private String status;
    private ArrayList<Company> listCompany;

    public StatusCompany(String status, ArrayList<Company> listCompany) {
        this.status = status;
        this.listCompany = listCompany;
    }

    public String getStatus() {
        return status;
    }

    public ArrayList<Company> getListCompany() {
        return listCompany;
    }

    public void addCompany(Company company){
        this.listCompany.add(company);
    }
}
