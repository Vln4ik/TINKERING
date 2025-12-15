package com.tinkering.twinby.data

data class TokenResponse(
    val access_token: String,
    val token_type: String,
)

data class LoginRequest(
    val login: String,
    val password: String,
)

data class ProfilePublic(
    val user_id: String,
    val name: String,
    val gender: String,
    val age: Int,
    val about: String,
    val photo_url: String,
    val interests: List<String>,
)

data class FeedResponse(
    val users: List<ProfilePublic>,
)

data class SwipeRequest(
    val target_user_id: String,
    val direction: String, // "left" | "right"
)

data class SwipeResponse(
    val created_chat_id: String?,
)

data class ChatListItem(
    val chat_id: String,
    val other_user_id: String,
    val other_name: String,
    val other_photo_url: String,
    val last_message: String?,
    val last_message_at: String?,
)

data class MessageItem(
    val id: String,
    val chat_id: String,
    val sender_id: String,
    val text: String,
    val created_at: String,
)

data class SendMessageRequest(
    val text: String,
)

data class AttachmentResponse(
    val url: String,
    val name: String,
    val mime: String?,
)

data class SupportMessageItem(
    val id: String,
    val role: String,
    val text: String,
    val created_at: String,
)

data class SendSupportMessageRequest(
    val text: String,
)

data class SendSupportMessageResponse(
    val user_message: SupportMessageItem,
    val assistant_message: SupportMessageItem,
)

object Interests {
    val LIST = listOf(
        "music",
        "sports",
        "coding",
        "movies",
        "travel",
        "art",
        "football",
        "reading",
    )
}


