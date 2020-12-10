package com.enseval.gcmuser.API;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL_SELECT = "https://2jn9dctkcb.execute-api.ap-southeast-1.amazonaws.com/"; //url api select
    private static final String BASE_URL_INSERT = "https://jrs65kbpo5.execute-api.ap-southeast-1.amazonaws.com/"; //url api insert
    private static final String BASE_URL_KURS = "https://api.exchangeratesapi.io/"; //url api kurs

    private static final String BASE_URL_GLOB_API = "https://glob.co.id/";

    private static RetrofitClient mInstance;
    private static RetrofitClient mInstance2;
    private static RetrofitClient mInstance3;
    private static RetrofitClient mInstance4;
    private Retrofit retrofit;

    private RetrofitClient(String type) {
        String BASE_URL = null;
        if(type.equals("SELECT")){
            BASE_URL = BASE_URL_SELECT;
        }
        else if (type.equals("INSERT")){
            BASE_URL =  BASE_URL_INSERT;
        }
        else if(type.equals("KURS")){
            BASE_URL = BASE_URL_KURS;
        }
        else if(type.equals("GLOB")){
            BASE_URL = BASE_URL_GLOB_API;
        }
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitClient("SELECT");
        }
        return mInstance;
    }

    public static synchronized RetrofitClient getInstance2() {
        if (mInstance2 == null) {
            mInstance2 = new RetrofitClient("INSERT");
        }
        return mInstance2;
    }

    public static synchronized RetrofitClient getInstanceKurs() {
        if (mInstance3 == null) {
            mInstance3 = new RetrofitClient("KURS");
        }
        return mInstance3;
    }

    public static synchronized RetrofitClient getInstanceGLOB() {
        if (mInstance4 == null) {
            mInstance4 = new RetrofitClient("GLOB");
        }
        return mInstance4;
    }

    public API getApi() {
        return retrofit.create(API.class);
    }

    private static Retrofit retrofitOTP = null;
    public static final String BASE_URL_OTP = "https://www.emos.id/";
    public static final String BASE_USER_ID = "GMOS001";
    public static final String BASE_KEY_ID = "z25k4at3jzob718iqceofgor6a1tbm";

    public static Retrofit getRetrofitOTP(String baseURL){
            if (retrofitOTP==null) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor)
                        .connectTimeout(30, TimeUnit.MINUTES)
                        .readTimeout(30, TimeUnit.MINUTES)
                        .build();

                retrofitOTP = new Retrofit.Builder()
                        .baseUrl(baseURL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofitOTP;
    }

    public static API getAPIService() {

        return RetrofitClient.getRetrofitOTP(BASE_URL_OTP).create(API.class);
    }

    public static final String BASE_URL = "https://fcm.googleapis.com/";
    private static Retrofit retrofitfcm = null;

    public static Retrofit getClient() {
        if (retrofitfcm==null) {
            retrofitfcm = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitfcm;
    }

    public static final String BASE_URL_NOTIF = "https://glob.co.id/";
    private static Retrofit retrofitNotif = null;

    public static Retrofit getNotif(String baseUrlNotif){
        if (retrofitNotif == null){
            retrofitNotif = new Retrofit.Builder()
                    .baseUrl(baseUrlNotif)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return  retrofitNotif;
    }

    public static API getNotifService(){
        return RetrofitClient.getNotif(BASE_URL_NOTIF).create(API.class);
    }
}