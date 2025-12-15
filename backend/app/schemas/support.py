from __future__ import annotations

import uuid
from datetime import datetime

from pydantic import BaseModel


class SupportMessageItem(BaseModel):
    id: uuid.UUID
    role: str  # "user" | "assistant"
    text: str
    created_at: datetime


class SendSupportMessageRequest(BaseModel):
    text: str


class SendSupportMessageResponse(BaseModel):
    user_message: SupportMessageItem
    assistant_message: SupportMessageItem


