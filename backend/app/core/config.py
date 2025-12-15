from __future__ import annotations

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    app_name: str = "twinby-mvp"
    env: str = "dev"
    public_base_url: str = "http://localhost:8080"

    jwt_secret: str = "change_me_please"
    jwt_expires_min: int = 60 * 24 * 7

    # Default to local persistent SQLite for easy local run (Docker/Postgres overrides via env)
    database_url: str = "sqlite:///./twinby.db"
    # Local run: store uploads inside project folder; Docker overrides to /app/uploads
    upload_dir: str = "./uploads"

    reco_service_url: str | None = None

    gigachat_credentials: str | None = None
    gigachat_scope: str = "GIGACHAT_API_PERS"
    gigachat_model: str = "GigaChat-2-Pro"
    gigachat_verify_ssl: bool = True


settings = Settings()


