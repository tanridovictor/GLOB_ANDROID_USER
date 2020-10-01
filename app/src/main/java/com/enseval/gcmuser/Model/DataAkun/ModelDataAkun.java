package com.enseval.gcmuser.Model.DataAkun;

import android.support.annotation.NonNull;

public class ModelDataAkun {
    private String password;
    private String email_receiver;
    private String username;

    @NonNull
    @Override
    public String toString() {
        return "post{" +
                "password = '" +password+ '\'' +
                ", email_receiver = " +email_receiver+ '\'' +
                ", username = " +username+ '\'' +
                '}';
    }
}
