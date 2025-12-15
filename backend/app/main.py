from __future__ import annotations

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.api.routes.auth import router as auth_router
from app.api.routes.chats import router as chats_router
from app.api.routes.feed import router as feed_router
from app.api.routes.me import router as me_router
from app.api.routes.support import router as support_router
from app.core.config import settings
from app.db.init_db import create_tables, seed_interests
from app.db.session import SessionLocal
from app.utils.default_assets import ensure_default_avatar
from app.utils.images import ensure_dir

app = FastAPI(title=settings.app_name)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def on_startup() -> None:
    ensure_dir(settings.upload_dir)
    ensure_default_avatar(settings.upload_dir)
    create_tables()
    db = SessionLocal()
    try:
        seed_interests(db)
    finally:
        db.close()


app.mount("/static", StaticFiles(directory=settings.upload_dir), name="static")

app.include_router(auth_router)
app.include_router(me_router)
app.include_router(feed_router)
app.include_router(chats_router)
app.include_router(support_router)


