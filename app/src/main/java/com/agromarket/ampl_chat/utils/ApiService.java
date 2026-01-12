package com.agromarket.ampl_chat.utils;

import com.agromarket.ampl_chat.models.api.AgentResponse;
import com.agromarket.ampl_chat.models.api.CustomerListResponse;
import com.agromarket.ampl_chat.models.api.LoginRequest;
import com.agromarket.ampl_chat.models.api.LoginResponse;
import com.agromarket.ampl_chat.models.api.MessageListResponse;
import com.agromarket.ampl_chat.models.api.ProductListResponse;
import com.agromarket.ampl_chat.models.api.SendMessageRequest;
import com.agromarket.ampl_chat.models.api.SendMessageResponse;
import com.agromarket.ampl_chat.models.api.SendProductRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

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
}