from __future__ import annotations

import uuid
from typing import Literal

from pydantic import BaseModel, Field

INTERESTS_LIST = [
    "music",
    "sports",
    "coding",
    "movies",
    "travel",
    "art",
    "football",
    "reading",
]


Gender = Literal["male", "female", "other"]


class ProfilePublic(BaseModel):
    user_id: uuid.UUID
    name: str
    gender: Gender
    age: int
    about: str
    photo_url: str
    interests: list[str]


class ProfileUpdate(BaseModel):
    name: str | None = Field(default=None, max_length=64)
    gender: Gender | None = None
    age: int | None = Field(default=None, ge=18, le=99)
    about: str | None = None
    interests: list[str] | None = None


