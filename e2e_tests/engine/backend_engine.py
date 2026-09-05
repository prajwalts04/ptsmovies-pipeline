"""
Backend & Integration Engine
Validates Retrofit REST endpoints, Bearer authentication headers,
Room SQLite entities and schemas, and offline cache synchronization.
"""

from typing import Dict, Any, List, Optional, Tuple
import json

class RetrofitEndpointContract:
    ENDPOINTS = {
        # Auth & System
        "login": {"method": "POST", "path": "/api/auth/login", "auth_required": False},
        "get_profile": {"method": "GET", "path": "/api/auth/me", "auth_required": True},
        "update_profile": {"method": "POST", "path": "/api/user/profile", "auth_required": True},
        "change_password": {"method": "POST", "path": "/api/user/change-password", "auth_required": True},
        "list_users": {"method": "GET", "path": "/api/users", "auth_required": True},
        "add_user": {"method": "POST", "path": "/api/users", "auth_required": True},
        "delete_user": {"method": "DELETE", "path": "/api/users/{id}", "auth_required": True},
        "get_system_stats": {"method": "GET", "path": "/api/system/stats", "auth_required": True},
        # Stream
        "get_media_library": {"method": "GET", "path": "/api/media/library", "auth_required": True},
        "toggle_watchlist": {"method": "POST", "path": "/api/media/watchlist/toggle", "auth_required": True},
        "update_progress": {"method": "POST", "path": "/api/progress/update", "auth_required": True},
        "stream_video": {"method": "GET", "path": "/api/stream/video", "auth_required": True},
        # Hub Queue
        "get_downloads_queue": {"method": "GET", "path": "/api/downloads", "auth_required": True},
        "queue_download": {"method": "POST", "path": "/api/downloads/queue", "auth_required": True},
        "retry_download_task": {"method": "POST", "path": "/api/downloads/{id}/retry", "auth_required": True},
        "cancel_download_task": {"method": "DELETE", "path": "/api/downloads/{id}", "auth_required": True},
        "clear_all_downloads": {"method": "POST", "path": "/api/downloads/clear-all", "auth_required": True},
        # Vault
        "get_vault_stats": {"method": "GET", "path": "/api/vault/stats", "auth_required": True},
        "get_vault_documents": {"method": "GET", "path": "/api/vault/documents", "auth_required": True},
        "upload_vault_document": {"method": "POST", "path": "/api/vault/documents", "auth_required": True},
        "delete_vault_document": {"method": "DELETE", "path": "/api/vault/documents/{id}", "auth_required": True},
        "get_vault_notes": {"method": "GET", "path": "/api/vault/notes", "auth_required": True},
        "create_vault_note": {"method": "POST", "path": "/api/vault/notes", "auth_required": True},
        "update_vault_note": {"method": "PUT", "path": "/api/vault/notes/{id}", "auth_required": True},
        "delete_vault_note": {"method": "DELETE", "path": "/api/vault/notes/{id}", "auth_required": True},
        # Files
        "browse_directory": {"method": "GET", "path": "/api/fs/list", "auth_required": True},
        "create_directory": {"method": "POST", "path": "/api/fs/mkdir", "auth_required": True},
        "delete_files": {"method": "POST", "path": "/api/fs/delete", "auth_required": True},
        "rename_file": {"method": "POST", "path": "/api/fs/rename", "auth_required": True},
        "read_file": {"method": "GET", "path": "/api/fs/read", "auth_required": True},
        "write_file": {"method": "POST", "path": "/api/fs/write", "auth_required": True},
        "zip_files": {"method": "POST", "path": "/api/fs/zip", "auth_required": True},
        "unzip_file": {"method": "POST", "path": "/api/fs/unzip", "auth_required": True},
        "chmod_file": {"method": "POST", "path": "/api/fs/chmod", "auth_required": True}
    }

    @classmethod
    def validate_request(cls, endpoint_key: str, headers: Dict[str, str], token: Optional[str]) -> Tuple[bool, Optional[str]]:
        if endpoint_key not in cls.ENDPOINTS:
            return False, f"Unknown endpoint: {endpoint_key}"
        cfg = cls.ENDPOINTS[endpoint_key]
        if cfg["auth_required"]:
            auth_header = headers.get("Authorization", "")
            if not auth_header.startswith("Bearer ") or len(auth_header.split(" ")) < 2:
                return False, "401_UNAUTHORIZED"
            req_token = auth_header.split(" ")[1]
            if not req_token or req_token != token:
                return False, "401_UNAUTHORIZED"
        return True, None

class RoomDatabaseCacheSimulator:
    def __init__(self):
        self.cached_movies: Dict[str, Dict[str, Any]] = {}
        self.cached_series: Dict[str, Dict[str, Any]] = {}
        self.cached_vault_docs: Dict[int, Dict[str, Any]] = {}
        self.cached_vault_notes: Dict[int, Dict[str, Any]] = {}
        self.downloaded_media: Dict[str, Dict[str, Any]] = {}

    def sync_media_library(self, movies: List[Dict[str, Any]], series: List[Dict[str, Any]]):
        self.cached_movies = {m["id"]: m for m in movies}
        self.cached_series = {s["id"]: s for s in series}

    def sync_vault(self, docs: List[Dict[str, Any]], notes: List[Dict[str, Any]]):
        self.cached_vault_docs = {d["id"]: d for d in docs}
        self.cached_vault_notes = {n["id"]: n for n in notes}

    def query_offline_catalog(self, query: str = "") -> List[Dict[str, Any]]:
        results = []
        for m in self.cached_movies.values():
            if not query or query.lower() in m.get("title", "").lower():
                results.append(m)
        return results

