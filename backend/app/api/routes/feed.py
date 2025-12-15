from __future__ import annotations

import os
import uuid

from fastapi import APIRouter, Depends
from sqlalchemy import and_, not_, select
from sqlalchemy.orm import Session

from app.api.deps import get_db, get_current_user
from app.core.config import settings
from app.db.models import Profile, Swipe, User, UserInterest
from app.schemas.feed import FeedResponse
from app.schemas.profile import ProfilePublic
from app.services.reco_client import RecoClient

router = APIRouter(prefix="", tags=["feed"])


def _photo_url(photo_path: str) -> str:
    name = os.path.basename(photo_path)
    return f"{settings.public_base_url.rstrip('/')}/static/{name}"


@router.get("/feed", response_model=FeedResponse)
async def get_feed(
    limit: int = 20,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # Exclude already swiped users
    swiped_ids = {
        row[0]
        for row in db.execute(select(Swipe.target_user_id).where(Swipe.user_id == user.id)).all()
    }

    q = (
        db.query(User)
        .join(Profile, Profile.user_id == User.id)
        .filter(and_(User.id != user.id, not_(User.id.in_(swiped_ids))))
        .order_by(User.created_at.desc())
        .limit(limit * 3)
    )
    candidates = q.all()

    candidate_ids = [u.id for u in candidates]
    if settings.reco_service_url:
        try:
            ranked = await RecoClient(settings.reco_service_url).rank_candidates(user.id, candidate_ids)
            id_to_user = {u.id: u for u in candidates}
            candidates = [id_to_user[i] for i in ranked if i in id_to_user]
        except Exception:
            pass

    out: list[ProfilePublic] = []
    for u in candidates[:limit]:
        profile = u.profile
        interests = [x[0] for x in db.query(UserInterest.interest_key).filter(UserInterest.user_id == u.id).all()]
        out.append(
            ProfilePublic(
                user_id=u.id,
                name=profile.name,
                gender=profile.gender.value,
                age=profile.age,
                about=profile.about,
                photo_url=_photo_url(profile.photo_path),
                interests=interests,
            )
        )
    return FeedResponse(users=out)


