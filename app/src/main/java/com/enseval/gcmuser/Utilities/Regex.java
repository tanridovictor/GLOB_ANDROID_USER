package com.enseval.gcmuser.Utilities;

import java.util.regex.Pattern;

public class Regex {
    public static final Pattern emailPattern
            = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\."+
            "[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    public static final Pattern passwordPattern
            = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$");

    public static final Pattern usernamePattern
            = Pattern.compile("^[a-zA-Z0-9_.-]{8,}$");
}
