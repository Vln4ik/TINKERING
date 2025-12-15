from __future__ import annotations

import argparse
import random

from sqlalchemy.orm import Session

from app.core.security import hash_password
from app.core.config import settings
from app.db.models import Gender, Profile, User, UserInterest
from app.db.session import SessionLocal
from app.schemas.profile import INTERESTS_LIST
from app.utils.default_assets import ensure_default_avatar, DEFAULT_AVATAR_NAME


NAMES_M = ["Kirill", "Alex", "Dmitry", "Nikita", "Sergey", "Ivan", "Maksim"]
NAMES_F = ["Anna", "Daria", "Maria", "Ekaterina", "Olga", "Sofia", "Alina"]
ABOUT = [
    "Люблю прогулки, кофе и хорошие разговоры.",
    "Ищу человека для общения и приключений.",
    "Работаю, развиваюсь, люблю юмор.",
    "Кино, музыка, путешествия — это про меня.",
    "Спорт по утрам, мемы по вечерам.",
]


def seed(db: Session, count: int, password: str) -> int:
    ensure_default_avatar(settings.upload_dir)
    default_photo_path = f"{settings.upload_dir.rstrip('/')}/{DEFAULT_AVATAR_NAME}"

    created = 0
    for i in range(1, count + 1):
        login = f"demo{i}"
        exists = db.query(User).filter(User.login == login).first()
        if exists:
            continue

        gender = random.choice([Gender.male, Gender.female])
        name = random.choice(NAMES_M if gender == Gender.male else NAMES_F)
        age = random.randint(18, 35)
        about = random.choice(ABOUT)
        interests = random.sample(INTERESTS_LIST, k=random.randint(1, 4))

        u = User(login=login, password_hash=hash_password(password))
        u.profile = Profile(
            name=name,
            gender=gender,
            age=age,
            about=about,
            photo_path=default_photo_path,
        )
        db.add(u)
        db.flush()
        db.add_all([UserInterest(user_id=u.id, interest_key=k) for k in interests])
        created += 1

    db.commit()
    return created


def main() -> None:
    p = argparse.ArgumentParser()
    p.add_argument("--count", type=int, default=30)
    p.add_argument("--password", type=str, default="demo12345")
    args = p.parse_args()

    db = SessionLocal()
    try:
        created = seed(db, args.count, args.password)
        print(f"Seeded users: {created}")
        print(f"Login pattern: demo1..demo{args.count}, password: {args.password}")
    finally:
        db.close()


if __name__ == "__main__":
    main()


