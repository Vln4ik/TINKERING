from __future__ import annotations

import os

from fastapi import Request
from fastapi import APIRouter, Depends, File, Form, UploadFile
from fastapi import HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_db, get_current_user
from app.core.config import settings
from app.db.models import Gender, Profile, User, UserInterest
from app.schemas.profile import INTERESTS_LIST, ProfilePublic
from app.utils.images import save_upload

router = APIRouter(prefix="/me", tags=["me"])


def _photo_url(request: Request, photo_path: str) -> str:
    name = os.path.basename(photo_path)
    base = str(request.base_url).rstrip("/")
    return f"{base}/static/{name}"


@router.get("", response_model=ProfilePublic)
def get_me(request: Request, user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    profile = user.profile
    interests = [ui.interest_key for ui in db.query(UserInterest).filter(UserInterest.user_id == user.id).all()]
    return ProfilePublic(
        user_id=user.id,
        name=profile.name,
        gender=profile.gender.value,
        age=profile.age,
        about=profile.about,
        photo_url=_photo_url(request, profile.photo_path),
        interests=interests,
    )


@router.put("", response_model=ProfilePublic)
def update_me(
    request: Request,
    name: str | None = Form(default=None, max_length=64),
    gender: Gender | None = Form(default=None),
    age: int | None = Form(default=None, ge=18, le=99),
    about: str | None = Form(default=None),
    interests: str | None = Form(default=None, description="Comma-separated interest keys"),
    photo: UploadFile | None = File(default=None),
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    profile: Profile = user.profile
    if name is not None:
        profile.name = name
    if gender is not None:
        profile.gender = gender
    if age is not None:
        profile.age = age
    if about is not None:
        profile.about = about
    if photo is not None:
        profile.photo_path = save_upload(settings.upload_dir, photo)

    if interests is not None:
        keys = [x.strip() for x in interests.split(",") if x.strip()]
        if any(k not in INTERESTS_LIST for k in keys):
            raise HTTPException(status_code=400, detail="Unknown interest in list")
        db.query(UserInterest).filter(UserInterest.user_id == user.id).delete()
        db.add_all([UserInterest(user_id=user.id, interest_key=k) for k in set(keys)])

    db.add(profile)
    db.commit()

    interests_out = [ui.interest_key for ui in db.query(UserInterest).filter(UserInterest.user_id == user.id).all()]
    return ProfilePublic(
        user_id=user.id,
        name=profile.name,
        gender=profile.gender.value,
        age=profile.age,
        about=profile.about,
        photo_url=_photo_url(request, profile.photo_path),
        interests=interests_out,
    )


