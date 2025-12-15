from __future__ import annotations

import os
import uuid
from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import or_
from sqlalchemy.orm import Session

from app.api.deps import get_db, get_current_user
from app.core.config import settings
from app.db.models import Chat, Message, Swipe, SwipeDirection, User, UserInterest
from app.schemas.chat import (
    ChatListItem,
    MessageItem,
    SendMessageRequest,
    SwipeRequest,
    SwipeResponse,
)

router = APIRouter(prefix="", tags=["chats"])


def _photo_url(photo_path: str) -> str:
    name = os.path.basename(photo_path)
    return f"{settings.public_base_url.rstrip('/')}/static/{name}"


def _pair(a: uuid.UUID, b: uuid.UUID) -> tuple[uuid.UUID, uuid.UUID]:
    return (a, b) if str(a) < str(b) else (b, a)


@router.post("/swipe", response_model=SwipeResponse)
def swipe(
    data: SwipeRequest,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if data.target_user_id == user.id:
        raise HTTPException(status_code=400, detail="Cannot swipe self")
    if data.direction not in {"left", "right"}:
        raise HTTPException(status_code=400, detail="Invalid direction")

    direction = SwipeDirection.right if data.direction == "right" else SwipeDirection.left

    existing = (
        db.query(Swipe)
        .filter(Swipe.user_id == user.id, Swipe.target_user_id == data.target_user_id)
        .first()
    )
    if existing:
        existing.direction = direction
        db.add(existing)
    else:
        db.add(Swipe(user_id=user.id, target_user_id=data.target_user_id, direction=direction))

    created_chat_id: uuid.UUID | None = None
    if direction == SwipeDirection.right:
        a, b = _pair(user.id, data.target_user_id)
        chat = db.query(Chat).filter(Chat.user_a_id == a, Chat.user_b_id == b).first()
        if not chat:
            chat = Chat(user_a_id=a, user_b_id=b)
            db.add(chat)
            db.flush()
        created_chat_id = chat.id

    db.commit()
    return SwipeResponse(created_chat_id=created_chat_id)


@router.get("/chats", response_model=list[ChatListItem])
def list_chats(user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    chats = (
        db.query(Chat)
        .filter(or_(Chat.user_a_id == user.id, Chat.user_b_id == user.id))
        .order_by(Chat.created_at.desc())
        .all()
    )

    out: list[ChatListItem] = []
    for c in chats:
        other_id = c.user_b_id if c.user_a_id == user.id else c.user_a_id
        other = db.get(User, other_id)
        if not other or not other.profile:
            continue
        last = (
            db.query(Message)
            .filter(Message.chat_id == c.id)
            .order_by(Message.created_at.desc())
            .first()
        )
        out.append(
            ChatListItem(
                chat_id=c.id,
                other_user_id=other_id,
                other_name=other.profile.name,
                other_photo_url=_photo_url(other.profile.photo_path),
                last_message=last.text if last else None,
                last_message_at=last.created_at if last else None,
            )
        )
    return out


@router.get("/chats/{chat_id}/messages", response_model=list[MessageItem])
def list_messages(chat_id: uuid.UUID, user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    chat = db.get(Chat, chat_id)
    if not chat or user.id not in {chat.user_a_id, chat.user_b_id}:
        raise HTTPException(status_code=404, detail="Chat not found")

    msgs = (
        db.query(Message)
        .filter(Message.chat_id == chat_id)
        .order_by(Message.created_at.asc())
        .all()
    )
    return [
        MessageItem(
            id=m.id,
            chat_id=m.chat_id,
            sender_id=m.sender_id,
            text=m.text,
            created_at=m.created_at,
        )
        for m in msgs
    ]


@router.post("/chats/{chat_id}/messages", response_model=MessageItem)
def send_message(
    chat_id: uuid.UUID,
    data: SendMessageRequest,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    chat = db.get(Chat, chat_id)
    if not chat or user.id not in {chat.user_a_id, chat.user_b_id}:
        raise HTTPException(status_code=404, detail="Chat not found")
    if not data.text.strip():
        raise HTTPException(status_code=400, detail="Empty message")

    m = Message(chat_id=chat_id, sender_id=user.id, text=data.text.strip())
    db.add(m)
    db.commit()
    db.refresh(m)
    return MessageItem(id=m.id, chat_id=m.chat_id, sender_id=m.sender_id, text=m.text, created_at=m.created_at)


