from __future__ import annotations

import os
import logging

from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
from sqlalchemy.orm import Session

from app.api.deps import get_db
from app.core.security import create_access_token, hash_password, verify_password
from app.db.models import Gender, Profile, User, UserInterest
from app.schemas.auth import LoginRequest, TokenResponse
from app.schemas.profile import INTERESTS_LIST
from app.utils.images import save_upload
from app.core.config import settings

router = APIRouter(prefix="/auth", tags=["auth"])
logger = logging.getLogger(__name__)


@router.post("/register", response_model=TokenResponse)
def register(
    login: str = Form(..., min_length=3, max_length=64),
    password: str = Form(..., min_length=6, max_length=128),
    name: str = Form(..., max_length=64),
    gender: Gender = Form(...),
    age: int = Form(..., ge=18, le=99),
    about: str = Form(...),
    interests: str = Form(..., description="Comma-separated interest keys"),
    photo: UploadFile = File(...),
    db: Session = Depends(get_db),
):
    if db.query(User).filter(User.login == login).first():
        raise HTTPException(status_code=400, detail="Login already used")

    interest_keys = [x.strip() for x in interests.split(",") if x.strip()]
    if not interest_keys:
        raise HTTPException(status_code=400, detail="Interests required")
    if any(k not in INTERESTS_LIST for k in interest_keys):
        raise HTTPException(status_code=400, detail="Unknown interest in list")

    photo_path = save_upload(settings.upload_dir, photo)
    if not os.path.exists(photo_path):
        raise HTTPException(status_code=500, detail="Photo upload failed")

    try:
        user = User(login=login, password_hash=hash_password(password))
        user.profile = Profile(name=name, gender=gender, age=age, about=about, photo_path=photo_path)
        db.add(user)
        db.flush()

        db.add_all([UserInterest(user_id=user.id, interest_key=k) for k in set(interest_keys)])
        db.commit()
    except IntegrityError as e:
        db.rollback()
        logger.exception("Registration integrity error")
        # Most common reasons: login conflict, missing interests seed, FK/constraint failures
        raise HTTPException(status_code=400, detail="Registration failed (data constraint).")
    except SQLAlchemyError:
        db.rollback()
        logger.exception("Registration db error")
        raise HTTPException(status_code=500, detail="Registration failed (db error).")

    return TokenResponse(access_token=create_access_token(str(user.id)))


@router.post("/login", response_model=TokenResponse)
def login(data: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.login == data.login).first()
    if not user or not verify_password(data.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Invalid credentials")
    return TokenResponse(access_token=create_access_token(str(user.id)))


