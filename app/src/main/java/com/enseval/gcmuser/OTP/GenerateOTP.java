package com.enseval.gcmuser.OTP;

import java.util.Random;

public class GenerateOTP {
    public static char[] sendGenerateOTP(int length){
        String numbers = "0123456789";
        Random r=new Random();
        char[] otp=new char[length];
        for (int i =0; i<length; i++){
            otp[i]= numbers.charAt(r.nextInt(numbers.length()));
        }
        return otp;
    }
}
