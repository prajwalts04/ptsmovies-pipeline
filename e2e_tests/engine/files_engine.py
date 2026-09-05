"""
Files Engine
Validates MergerFS breadcrumbs, file size formatting, extension color tokens,
CRUD operations, zip compression/extraction, chmod octal logic, and in-app editor.
"""

import posixpath
import re
from typing import List, Dict, Any, Tuple

class FilesystemHelper:
    ICON_COLORS = {
        "folder": "#EAB308",
        "video": "#38BDF8",
        "audio": "#C084FC",
        "image": "#4ADE80",
        "archive": "#FACC15",
        "code": "#818CF8",
        "pdf": "#EF4444",
        "default": "#E2E2E8"
    }

    VIDEO_EXTS = {".mp4", ".mkv", ".webm", ".avi", ".mov"}
    AUDIO_EXTS = {".mp3", ".flac", ".wav", ".aac", ".m4a"}
    IMAGE_EXTS = {".jpg", ".jpeg", ".png", ".webp", ".gif"}
    ARCHIVE_EXTS = {".zip", ".tar", ".gz", ".7z", ".rar"}
    CODE_EXTS = {".kt", ".java", ".py", ".js", ".jsx", ".ts", ".tsx", ".json", ".xml", ".html", ".css", ".sh", ".env"}

    @classmethod
    def format_size(cls, size_bytes: int) -> str:
        if size_bytes < 0:
            return "0 B"
        units = ["B", "KB", "MB", "GB", "TB", "PB"]
        unit_idx = 0
        val = float(size_bytes)
        while val >= 1024.0 and unit_idx < len(units) - 1:
            val /= 1024.0
            unit_idx += 1
        if unit_idx == 0:
            return f"{int(val)} B"
        return f"{val:.2f} {units[unit_idx]}"

    @classmethod
    def get_extension_category(cls, filename: str, is_dir: bool = False) -> str:
        if is_dir:
            return "folder"
        ext = posixpath.splitext(filename)[1].lower()
        fn_lower = filename.lower()
        if ext in cls.VIDEO_EXTS or fn_lower.endswith(tuple(cls.VIDEO_EXTS)):
            return "video"
        if ext in cls.AUDIO_EXTS or fn_lower.endswith(tuple(cls.AUDIO_EXTS)):
            return "audio"
        if ext in cls.IMAGE_EXTS or fn_lower.endswith(tuple(cls.IMAGE_EXTS)):
            return "image"
        if ext in cls.ARCHIVE_EXTS or fn_lower.endswith(tuple(cls.ARCHIVE_EXTS)):
            return "archive"
        if ext in cls.CODE_EXTS or fn_lower in cls.CODE_EXTS or fn_lower.endswith(tuple(cls.CODE_EXTS)):
            return "code"
        if ext == ".pdf" or fn_lower.endswith(".pdf"):
            return "pdf"
        return "default"

    @classmethod
    def parse_breadcrumbs(cls, path: str) -> List[Dict[str, str]]:
        if not path or not path.strip():
            return [{"name": "Root", "path": "/"}]
        norm = posixpath.normpath(path)
        if norm in ("/", ".", ""):
            return [{"name": "Root", "path": "/"}]
        segments = [s for s in norm.split('/') if s and s != "."]
        crumbs = [{"name": "Root", "path": "/"}]
        curr = ""
        for seg in segments:
            curr += f"/{seg}"
            crumbs.append({"name": seg, "path": curr})
        return crumbs

    @classmethod
    def octal_to_posix_string(cls, octal_mode: int, is_dir: bool = False) -> str:
        mode_oct = octal_mode & 0o777
        res = ["d" if is_dir else "-"]
        for shift in (6, 3, 0):
            digit = (mode_oct >> shift) & 7
            res.append("r" if (digit & 4) else "-")
            res.append("w" if (digit & 2) else "-")
            res.append("x" if (digit & 1) else "-")
        return "".join(res)

    @classmethod
    def posix_string_to_octal(cls, perm_str: str) -> int:
        s = perm_str[-9:] if len(perm_str) >= 9 else perm_str
        val = 0
        for i, char in enumerate(s):
            if char in ("r", "w", "x"):
                bit = 8 - i
                val |= (1 << bit)
        return val
