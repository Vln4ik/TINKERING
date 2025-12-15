from __future__ import annotations

from pydantic import BaseModel

from app.schemas.profile import ProfilePublic


class FeedResponse(BaseModel):
    users: list[ProfilePublic]


