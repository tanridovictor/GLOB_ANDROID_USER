package com.enseval.gcmuser.API;

import com.enseval.gcmuser.Model.DataAkun.ModelDataAkun;
import com.enseval.gcmuser.Model.NotifAI.ModelNotif;
import com.enseval.gcmuser.Model.NotifFirebase.NotificationBody;
import com.enseval.gcmuser.Model.otp.ModelMessageID;
import com.enseval.gcmuser.Model.otp.ModelOTP;
import com.enseval.gcmuser.Response.BarangResponse;
import com.enseval.gcmuser.Response.CategoryResponse;
import com.google.gson.JsonObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface API {
    //request ke api select, langsung masuk model category
    @POST("/default/gcm_select")
    Call<CategoryResponse> requestCategory(@Body JSONRequest body);

    //request ke api select, langsung masuk model barang
    @POST("/default/gcm_select")
    Call<BarangResponse> requestBarang(@Body JSONRequest body);

    //request ke api select dlm bentuk JsonObject
    @POST("/default/gcm_select")
    Call<JsonObject> request(@Body JSONRequest body);

    //request ke api insert dlm bentuk JsonObject
    @POST("/default/gcm_insert")
    Call<JsonObject> requestInsert(@Body JSONRequest body);

    //api untuk kurs
    @GET("/latest")
    Call<JsonObject> requestKurs(@Query("base") String base);

    //post untuk OTP GMOS
    @POST("sendmessage/message/sendmessage")
    Call<ModelOTP> sendOTPValue(@Body HashMap<String, String> body);

    //get untuk OTP GMOS
    @POST("sendotp/message/getmessage")
    Call<ModelMessageID> getMessageInformation(@Body HashMap<String, String> body);

    @POST("External/sendNotification")
    Call<ModelNotif> sendNotifNegoPersen(@Body HashMap<String, String> body);

    @POST("External/resetPassword")
    Call<ModelDataAkun> sendDataAkun(@Body HashMap<String, String> body);

    @Headers({
            "Content-Type: application/json",
            "Authorization: key=AAAA6NuQ4as:APA91bG9s8KnXjq2LjuRtTBDxcBXfM-D3AHk5Lcpstlf6uMU1tmc-M9FHK78w0FXk7uGfwHn4isfg6KFJnJeuIgHyVASgch_jo1ATWab_eB9WF4ArQw22Xli9owTQwFnRL64-ERS5-0Z"
    })
    @POST("fcm/send")
    Call<ResponseBody> sendNotification(@Body NotificationBody body);


    @POST("data/status/otp")
    Call<JsonObject> reqpassavailable(@Body JSONRequest body);

    @POST("External/insertOTP")
    Call<JsonObject> reqsenddataakun(@Body JSONRequest body);

    @POST("External/sendNotificationTrx")
    Call<JsonObject> sendNotifTrx(@Body JSONRequestTransaksi body);
}