package com.enseval.gcmuser.Utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Helper {
    // =============================================================================================
    // CLASS MEMBERS
    // =============================================================================================

    private static String key = "gcm-e-commerce19";
    private static String iv = "19gcm-e-commerce";
    private static String decryptedString;
    private static String encryptedString;

    private static DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    public static void setDateFormat(DateFormat dateFormat) {
        Helper.dateFormat = dateFormat;
    }

    public static DateFormat getDateFormat() {
        return dateFormat;
    }

    public static boolean isOnline(Context c) {
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            showMessage(c, e.getMessage());
        }
        return false;
    }

    public static void showMessage(Context c, String msg) {

    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static boolean isStringEmpyt(String etText) {
        if (etText.trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isEmailValid(EditText email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email.getText().toString();

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isCompare(EditText etText, EditText ex) {
        String a = etText.getText().toString();
        String b = ex.getText().toString();
        if (a.equals(b)) {
            return false;
        } else {
            return true;
        }
    }

    public static String numberFormat(Number source) {
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String yourFormattedString = formatter.format(source);
        return yourFormattedString;
    }

   public static String numberFormat(String source) {
        int val = Integer.valueOf(source);
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String yourFormattedString = formatter.format(val);
        return yourFormattedString;
    }

    public static String formatKTP(String source) {
        String val = String.valueOf(source);
        DecimalFormat formatter = new DecimalFormat("##,##,##,######,####");
        String yourFormattedString = formatter.format(val);
        return yourFormattedString;
    }

    public static String convertStringToDateSimple(String str_date) {
        if(str_date == null) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date timeStamp = null;
        try {
            timeStamp = sdf.parse(str_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (timeStamp != null) {
            return convertSimpleDateToString(timeStamp);
        }else {
            return null;
        }
    }

    public static String convertSimpleDateToStringReturnTime(Date date){
        String timeAsString = "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        timeAsString = sdf.format(date);
        return timeAsString;
    }

    public static String convertSimpleDateToString(Date date){
        String dateAsString = "";

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String monthAsString = "";
        if (month == 0) monthAsString = "Jan";
        if (month == 1) monthAsString = "Feb";
        if (month == 2) monthAsString = "Mar";
        if (month == 3) monthAsString = "Apr";
        if (month == 4) monthAsString = "Mei";
        if (month == 5) monthAsString = "Jun";
        if (month == 6) monthAsString = "Jul";
        if (month == 7) monthAsString = "Agu";
        if (month == 8) monthAsString = "Sep";
        if (month == 9) monthAsString = "Okt";
        if (month == 10) monthAsString = "Nov";
        if (month == 11) monthAsString = "Des";

        dateAsString = day + "-" + monthAsString + "-" + year;

        return dateAsString;
    }

    public static String convertDateToString(Date date) {
        String dateAsString = "";

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String monthAsString = "";
        if (month == 0) monthAsString = "Januari";
        if (month == 1) monthAsString = "Februari";
        if (month == 2) monthAsString = "Maret";
        if (month == 3) monthAsString = "April";
        if (month == 4) monthAsString = "Mei";
        if (month == 5) monthAsString = "Juni";
        if (month == 6) monthAsString = "Juli";
        if (month == 7) monthAsString = "Agustus";
        if (month == 8) monthAsString = "September";
        if (month == 9) monthAsString = "Oktober";
        if (month == 10) monthAsString = "November";
        if (month == 11) monthAsString = "Desember";

        dateAsString = day + " " + monthAsString + " " + year;

        return dateAsString;
    }

    public static int convertStringToIntegerMonth(String month){
        int monthAsInt = 0;
        if (month.equalsIgnoreCase("Januari")) monthAsInt = 1;
        if (month.equalsIgnoreCase("Februari")) monthAsInt = 2;
        if (month.equalsIgnoreCase("Maret")) monthAsInt = 3;
        if (month.equalsIgnoreCase("April")) monthAsInt = 4;
        if (month.equalsIgnoreCase("Mei")) monthAsInt = 5;
        if (month.equalsIgnoreCase("Juni")) monthAsInt = 6;
        if (month.equalsIgnoreCase("Juli")) monthAsInt = 7;
        if (month.equalsIgnoreCase("Agustus")) monthAsInt = 8;
        if (month.equalsIgnoreCase("September")) monthAsInt = 9;
        if (month.equalsIgnoreCase("Oktober")) monthAsInt = 10;
        if (month.equalsIgnoreCase("November")) monthAsInt = 11;
        if (month.equalsIgnoreCase("Desember")) monthAsInt = 12;
        return monthAsInt;
    }
    public static String convertIntegerToMonthString(Integer month) {
        String monthAsString = "";
        if (month == 1) monthAsString = "Januari";
        if (month == 2) monthAsString = "Februari";
        if (month == 3) monthAsString = "Maret";
        if (month == 4) monthAsString = "April";
        if (month == 5) monthAsString = "Mei";
        if (month == 6) monthAsString = "Juni";
        if (month == 7) monthAsString = "Juli";
        if (month == 8) monthAsString = "Agustus";
        if (month == 9) monthAsString = "September";
        if (month == 10) monthAsString = "Oktober";
        if (month == 11) monthAsString = "November";
        if (month == 12) monthAsString = "Desember";
        return monthAsString;
    }


    public static String convertToDate(String str_date) {
        if(str_date == null) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date timeStamp = null;
        try {
            timeStamp = sdf.parse(str_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (timeStamp != null) {
            return convertSimpleDateToString(timeStamp);
        }else {
            return null;
        }
    }

    private static DecimalFormat kursIdr = (DecimalFormat) DecimalFormat.getCurrencyInstance();
    private static DecimalFormatSymbols formatIDR = new DecimalFormatSymbols();

    public static DecimalFormat getCurrencyFormat(){
        formatIDR.setCurrencySymbol("IDR ");
        formatIDR.setMonetaryDecimalSeparator(',');
        formatIDR.setGroupingSeparator('.');
        kursIdr.setDecimalFormatSymbols(formatIDR);
        return kursIdr;
    }

    public static char[] sendGenerateOTP(int length){
        String numbers = "0123456789";
        Random r=new Random();
        char[] otp=new char[length];
        for (int i =0; i<length; i++){
            otp[i]= numbers.charAt(r.nextInt(numbers.length()));
        }
        return otp;
    }

    public static char[] sendGenerateForgetPass(int length){
        String numbers = "0A1B2C3D4E5F6G7H8I9JKLMNOPQRSTUVWXYZ";
        Random r=new Random();
        char[] fp=new char[length];
        for (int i =0; i<length; i++){
            fp[i]= numbers.charAt(r.nextInt(numbers.length()));
        }
        return fp;
    }

    public static String DecryptPassword(String text) throws Exception {
        String key = "gcm-e-commerce19";
        String iv = "19gcm-e-commerce";

        Cipher cipher = Cipher.getInstance
                ("AES/CBC/PKCS5Padding"); //this parameters should not be changed
        byte[] keyBytes = new byte[16];
        byte[] ivBytes = iv.getBytes();
        byte[] b = key.getBytes("UTF-8");

        int len = b.length;
        if (len > keyBytes.length)
            len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] results = new byte[text.length()];
        try {

            results = cipher.doFinal(Base64.decode(text, Base64.DEFAULT));

        } catch (Exception e) {
            Log.wtf("DecryptPassword", e);
        }
        Log.wtf("Data", new String(results, "UTF-8"));
        return new String(results, "UTF-8"); // it returns the result as a String
    }

    public static String Encrypt(String text) throws Exception {
        String key = "gcm-e-commerce19";
        String iv = "19gcm-e-commerce";

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        byte[] keyBytes = new byte[16];
        byte[] ivBytes = iv.getBytes();
        byte[] b = key.getBytes("UTF-8");

        int len = b.length;
        if (len > keyBytes.length)
            len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));

        return new String(Base64.encodeToString(results, Base64.NO_WRAP));
    }

    /*public static String formatKTP(Number source) {
        DecimalFormat formatter = new DecimalFormat("##.##.##.######.####");
        String yourFormattedString = formatter.format(source);
        return yourFormattedString;
    }*/

}
