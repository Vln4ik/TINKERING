from __future__ import annotations

import os
import uuid
from pathlib import Path

from fastapi import UploadFile


def ensure_dir(path: str) -> None:
    Path(path).mkdir(parents=True, exist_ok=True)


def save_upload(upload_dir: str, file: UploadFile) -> str:
    ensure_dir(upload_dir)
    _, ext = os.path.splitext(file.filename or "")
    ext = (ext or ".jpg").lower()
    if ext not in {".jpg", ".jpeg", ".png", ".webp"}:
        ext = ".jpg"

    name = f"{uuid.uuid4()}{ext}"
    out_path = Path(upload_dir) / name
    with out_path.open("wb") as f:
        f.write(file.file.read())
    return str(out_path)


