from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_db, get_current_user
from app.db.models import SupportMessage, User
from app.schemas.support import (
    SendSupportMessageRequest,
    SendSupportMessageResponse,
    SupportMessageItem,
)
from app.services.gigachat_client import GigaChatClient

router = APIRouter(prefix="/support", tags=["support"])


@router.get("/messages", response_model=list[SupportMessageItem])
def list_support_messages(user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    msgs = (
        db.query(SupportMessage)
        .filter(SupportMessage.user_id == user.id)
        .order_by(SupportMessage.created_at.asc())
        .all()
    )
    return [SupportMessageItem(id=m.id, role=m.role, text=m.text, created_at=m.created_at) for m in msgs]


@router.post("/messages", response_model=SendSupportMessageResponse)
def send_support_message(
    data: SendSupportMessageRequest,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    text = (data.text or "").strip()
    if not text:
        raise HTTPException(status_code=400, detail="Empty message")

    user_msg = SupportMessage(user_id=user.id, role="user", text=text)
    db.add(user_msg)
    db.flush()

    answer = GigaChatClient().ask_support(text)
    assistant_msg = SupportMessage(user_id=user.id, role="assistant", text=answer.text)
    db.add(assistant_msg)
    db.commit()

    db.refresh(user_msg)
    db.refresh(assistant_msg)

    return SendSupportMessageResponse(
        user_message=SupportMessageItem(
            id=user_msg.id, role=user_msg.role, text=user_msg.text, created_at=user_msg.created_at
        ),
        assistant_message=SupportMessageItem(
            id=assistant_msg.id,
            role=assistant_msg.role,
            text=assistant_msg.text,
            created_at=assistant_msg.created_at,
        ),
    )


