from __future__ import annotations

from sqlalchemy.orm import Session

from app.db.models import Base, Interest
from app.db.session import engine

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


def create_tables() -> None:
    Base.metadata.create_all(bind=engine)


def seed_interests(db: Session) -> None:
    existing = {row[0] for row in db.query(Interest.key).all()}
    to_add = [Interest(key=k) for k in INTERESTS_LIST if k not in existing]
    if to_add:
        db.add_all(to_add)
        db.commit()


