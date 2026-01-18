package com.agromarket.ampl_chat.utils;

import com.agromarket.ampl_chat.models.api.AgentResponse;
import com.agromarket.ampl_chat.models.api.CallResponse;
import com.agromarket.ampl_chat.models.api.CustomerListResponse;
import com.agromarket.ampl_chat.models.api.LoginRequest;
import com.agromarket.ampl_chat.models.api.LoginResponse;
import com.agromarket.ampl_chat.models.api.MessageListResponse;
import com.agromarket.ampl_chat.models.api.ProductListResponse;
import com.agromarket.ampl_chat.models.api.SendMessageRequest;
import com.agromarket.ampl_chat.models.api.SendMessageResponse;
import com.agromarket.ampl_chat.models.api.SendProductRequest;
import com.agromarket.ampl_chat.models.api.VendorRegisterResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @Multipart
    @POST("vendor/register")
    Call<VendorRegisterResponse> registerVendor(

            // -------- Account --------
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("password_confirmation") RequestBody passwordConfirmation,

            // -------- Firm --------
            @Part("firm_name") RequestBody firmName,
            @Part("gst_number") RequestBody gstNumber,
            @Part("license_type") RequestBody licenseType,
            @Part("fertilizer_license_no") RequestBody fertilizerLicense,
            @Part("seeds_license_no") RequestBody seedsLicense,
            @Part("pesticides_license_no") RequestBody pesticideLicense,

            // -------- Contact --------
            @Part("address") RequestBody address,
            @Part("phone_number") RequestBody phone,
            @Part("alternate_number") RequestBody alternatePhone,

            // -------- Files --------
            @Part MultipartBody.Part gst_doc,
            @Part MultipartBody.Part license_doc,
            @Part MultipartBody.Part aadhar_front_path,
            @Part MultipartBody.Part aadhar_back_path
    );

    @POST("logout")
    Call<Void> logout(@Header("Authorization") String token);

    @Headers("Accept: application/json")
    @GET("agent/customers")
    Call<CustomerListResponse> getAssignedCustomers(@Header("Authorization") String token);

    @GET("products")
    Call<ProductListResponse> getProducts(
            @Header("Authorization") String token,
            @Query("page") int page
    );

    @GET("messages/{user_id}")
    Call<MessageListResponse> getMessages(
            @Header("Authorization") String token,
            @Path("user_id") int customerId
    );

    @POST("messages/send")
    Call<SendMessageResponse> sendTextMessage(
            @Header("Authorization") String token,
            @Body SendMessageRequest body
    );

    @POST("messages/send-product")
    Call<SendMessageResponse> sendProductMessage(
            @Header("Authorization") String token,
            @Body SendProductRequest body
    );

    @Headers("Accept: application/json")
    @GET("customer/agent")
    Call<AgentResponse> getAssignedAgent(@Header("Authorization") String token);

    @POST("messages/seen/{user_id}")
    Call<Void> markSeen(
            @Header("Authorization") String token,
            @Path("user_id") int userId
    );

    @POST("calls/start")
    Call<CallResponse> startCall(
            @Header("Authorization") String token,
            @Body Map<String, Integer> body
    );

    @POST("calls/{id}/accept")
    Call<CallResponse> acceptCall(
            @Header("Authorization") String token,
            @Path("id") int callId
    );

    @POST("calls/{id}/reject")
    Call<Void> rejectCall(
            @Header("Authorization") String token,
            @Path("id") int callId
    );

    @POST("calls/{id}/end")
    Call<Void> endCall(
            @Header("Authorization") String token,
            @Path("id") int callId
    );
}