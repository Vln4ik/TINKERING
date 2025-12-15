from __future__ import annotations

import os
from pathlib import Path


DEFAULT_AVATAR_NAME = "default_avatar.svg"


def ensure_default_avatar(upload_dir: str) -> str:
    """
    Creates a small SVG placeholder avatar in upload_dir (text file, safe to ship).
    Returns absolute path to the avatar file.
    """
    Path(upload_dir).mkdir(parents=True, exist_ok=True)
    path = os.path.join(upload_dir, DEFAULT_AVATAR_NAME)
    if os.path.exists(path):
        return path

    svg = """<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#7C4DFF"/>
      <stop offset="1" stop-color="#00E5FF"/>
    </linearGradient>
  </defs>
  <rect width="512" height="512" rx="256" fill="url(#g)"/>
  <circle cx="256" cy="210" r="86" fill="rgba(0,0,0,0.28)"/>
  <path d="M128 444c22-78 76-118 128-118s106 40 128 118" fill="rgba(0,0,0,0.28)"/>
</svg>
"""
    with open(path, "w", encoding="utf-8") as f:
        f.write(svg)
    return path


