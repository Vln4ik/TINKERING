package com.tinkering.twinby.data

import android.content.Context
import com.tinkering.twinby.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class Repository(private val tokenStore: TokenStore) {
    private val api: ApiService = ApiFactory.create(BuildConfig.API_BASE_URL, tokenStore)

    suspend fun loadToken(context: Context) = tokenStore.load(context)

    suspend fun login(context: Context, login: String, password: String) {
        val token = api.login(LoginRequest(login = login, password = password)).access_token
        tokenStore.setToken(context, token)
    }

    suspend fun register(
        context: Context,
        login: String,
        password: String,
        name: String,
        gender: String,
        age: Int,
        about: String,
        interests: List<String>,
        photoFile: File,
    ) {
        val media = "image/*".toMediaType()
        val photoPart = MultipartBody.Part.createFormData(
            name = "photo",
            filename = photoFile.name,
            body = photoFile.asRequestBody(media)
        )
        val token = api.register(
            login = login.toRequestBody(),
            password = password.toRequestBody(),
            name = name.toRequestBody(),
            gender = gender.toRequestBody(),
            age = age.toString().toRequestBody(),
            about = about.toRequestBody(),
            interestsCsv = interests.joinToString(",").toRequestBody(),
            photo = photoPart
        ).access_token
        tokenStore.setToken(context, token)
    }

    suspend fun logout(context: Context) {
        tokenStore.setToken(context, null)
    }

    suspend fun me(): ProfilePublic = api.me()

    suspend fun updateMe(
        name: String? = null,
        gender: String? = null,
        age: Int? = null,
        about: String? = null,
        interests: List<String>? = null,
        photoFile: File? = null,
    ): ProfilePublic {
        val media = "image/*".toMediaType()
        val photoPart = photoFile?.let {
            MultipartBody.Part.createFormData(
                name = "photo",
                filename = it.name,
                body = it.asRequestBody(media)
            )
        }
        return api.updateMe(
            name = name?.toRequestBody(),
            gender = gender?.toRequestBody(),
            age = age?.toString()?.toRequestBody(),
            about = about?.toRequestBody(),
            interestsCsv = interests?.joinToString(",")?.toRequestBody(),
            photo = photoPart
        )
    }

    suspend fun feed(limit: Int = 20): List<ProfilePublic> = api.feed(limit).users

    suspend fun swipe(targetUserId: String, direction: String): SwipeResponse =
        api.swipe(SwipeRequest(target_user_id = targetUserId, direction = direction))

    suspend fun chats(): List<ChatListItem> = api.chats()

    suspend fun messages(chatId: String): List<MessageItem> = api.messages(chatId)

    suspend fun sendMessage(chatId: String, text: String): MessageItem =
        api.sendMessage(chatId, SendMessageRequest(text = text))

    suspend fun uploadChatAttachment(chatId: String, file: File, mimeType: String? = null): AttachmentResponse {
        val media = (mimeType ?: "application/octet-stream").toMediaType()
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = file.name,
            body = file.asRequestBody(media)
        )
        return api.uploadChatAttachment(chatId, part)
    }

    suspend fun supportMessages(): List<SupportMessageItem> = api.supportMessages()

    suspend fun sendSupport(text: String): SendSupportMessageResponse =
        api.sendSupport(SendSupportMessageRequest(text = text))
}


