from __future__ import annotations

import argparse

from sqlalchemy.orm import Session

from app.db.models import Chat, Message, Profile, SupportMessage, Swipe, User, UserInterest
from app.db.session import SessionLocal


def reset_users(db: Session) -> None:
    """
    Deletes all user-generated data.
    Keeps the Interests table intact (seeded on startup).
    """
    # Delete in FK-safe order.
    db.query(SupportMessage).delete()
    db.query(Message).delete()
    db.query(Chat).delete()
    db.query(Swipe).delete()
    db.query(UserInterest).delete()
    db.query(Profile).delete()
    db.query(User).delete()
    db.commit()


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument(
        "--yes",
        action="store_true",
        help="Required confirmation flag. Without it, the script will not run.",
    )
    args = ap.parse_args()

    if not args.yes:
        raise SystemExit("Refusing to wipe users without --yes")

    db = SessionLocal()
    try:
        reset_users(db)
        print("OK: all users and related data removed (interests kept).")
    finally:
        db.close()


if __name__ == "__main__":
    main()


