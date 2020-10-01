package com.enseval.gcmuser.Model;

public class User {
    private int userId;
    private int companyId;
    private int tipeBisnis;

    public User(int userId, int companyId, int tipeBisnis) {
        this.userId = userId;
        this.companyId = companyId;
        this.tipeBisnis = tipeBisnis;
    }

    public int getUserId() {
        return userId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public int getTipeBisnis() {
        return tipeBisnis;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public void setTipeBisnis(int tipeBisnis) {
        this.tipeBisnis = tipeBisnis;
    }
}
