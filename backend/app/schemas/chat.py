from __future__ import annotations

import uuid
from datetime import datetime

from pydantic import BaseModel


class ChatListItem(BaseModel):
    chat_id: uuid.UUID
    other_user_id: uuid.UUID
    other_name: str
    other_photo_url: str
    last_message: str | None
    last_message_at: datetime | None


class MessageItem(BaseModel):
    id: uuid.UUID
    chat_id: uuid.UUID
    sender_id: uuid.UUID
    text: str
    created_at: datetime


class SendMessageRequest(BaseModel):
    text: str


class SwipeRequest(BaseModel):
    target_user_id: uuid.UUID
    direction: str  # "left" | "right"


class SwipeResponse(BaseModel):
    created_chat_id: uuid.UUID | None = None


class AttachmentResponse(BaseModel):
    url: str
    name: str
    mime: str | None = None


