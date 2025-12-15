package com.tinkering.twinby.data

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @Multipart
    @POST("/auth/register")
    suspend fun register(
        @Part("login") login: RequestBody,
        @Part("password") password: RequestBody,
        @Part("name") name: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("age") age: RequestBody,
        @Part("about") about: RequestBody,
        @Part("interests") interestsCsv: RequestBody,
        @Part photo: MultipartBody.Part,
    ): TokenResponse

    @POST("/auth/login")
    suspend fun login(@Body req: LoginRequest): TokenResponse

    @GET("/me")
    suspend fun me(): ProfilePublic

    @Multipart
    @PUT("/me")
    suspend fun updateMe(
        @Part("name") name: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("age") age: RequestBody?,
        @Part("about") about: RequestBody?,
        @Part("interests") interestsCsv: RequestBody?,
        @Part photo: MultipartBody.Part?,
    ): ProfilePublic

    @GET("/feed")
    suspend fun feed(@Query("limit") limit: Int = 20): FeedResponse

    @POST("/swipe")
    suspend fun swipe(@Body req: SwipeRequest): SwipeResponse

    @GET("/chats")
    suspend fun chats(): List<ChatListItem>

    @GET("/chats/{chatId}/messages")
    suspend fun messages(@Path("chatId") chatId: String): List<MessageItem>

    @POST("/chats/{chatId}/messages")
    suspend fun sendMessage(@Path("chatId") chatId: String, @Body req: SendMessageRequest): MessageItem

    @Multipart
    @POST("/chats/{chatId}/attachments")
    suspend fun uploadChatAttachment(@Path("chatId") chatId: String, @Part file: MultipartBody.Part): AttachmentResponse

    @GET("/support/messages")
    suspend fun supportMessages(): List<SupportMessageItem>

    @POST("/support/messages")
    suspend fun sendSupport(@Body req: SendSupportMessageRequest): SendSupportMessageResponse
}

object ApiFactory {
    fun create(baseUrl: String, tokenStore: TokenStore): ApiService {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .addInterceptor(logger)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}


