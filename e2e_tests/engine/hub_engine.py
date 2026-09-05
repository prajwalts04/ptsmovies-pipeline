"""
Hub Queue Engine
Validates 1.5s live poller, real-time duplicate checking against disk and queue,
1-col/2-col/3-col bulk URL parser, 6-stage GHA pipeline, and task actions.
"""

import re
from typing import List, Dict, Any, Optional, Tuple

class BulkUrlParser:
    HEADER_KEYWORDS = [
        "season", "series", "episode", "ep", "link",
        "url", "download", "title", "name", "s#", "e#"
    ]

    @classmethod
    def is_header_line(cls, line: str) -> bool:
        lower = line.lower().strip()
        tokens = re.split(r'[\t|,;\s]+', lower)
        # If line contains multiple header keywords and no valid http url
        if "http://" not in lower and "https://" not in lower and "magnet:" not in lower:
            keyword_matches = sum(1 for kw in cls.HEADER_KEYWORDS if any(kw == t or kw in t for t in tokens))
            if keyword_matches >= 1:
                return True
        return False

    @classmethod
    def parse_bulk_text(cls, text: str, default_season: int = 1) -> List[Dict[str, Any]]:
        """
        Parses multi-format bulk URL input into structured episodes:
        - 3 Columns: Season | Episode | URL
        - 2 Columns: S01E01 | URL or Episode | URL
        - 1 Column:  URL per line (extracts embedded S01E01 or maps sequentially)
        """
        if not text or not text.strip():
            return []

        lines = [line.strip() for line in text.strip().splitlines() if line.strip()]
        parsed_items: List[Dict[str, Any]] = []
        sequential_ep = 1

        for line in lines:
            if cls.is_header_line(line):
                continue

            # Detect delimiter (tab, pipe, semicolon, or comma)
            delimiters = ['\t', '|', ';', ',']
            chosen_delim = None
            for d in delimiters:
                if d in line:
                    chosen_delim = d
                    break

            if chosen_delim:
                parts = [p.strip() for p in line.split(chosen_delim) if p.strip()]
            else:
                parts = [line]

            # 3 Columns: Season | Episode | URL
            if len(parts) >= 3:
                s_match = re.search(r'\d+', parts[0])
                e_match = re.search(r'\d+', parts[1])
                season = int(s_match.group()) if s_match else default_season
                episode = int(e_match.group()) if e_match else sequential_ep
                url = parts[2]
            # 2 Columns: S01E01 | URL or Episode | URL
            elif len(parts) == 2:
                col1 = parts[0]
                url = parts[1]
                se_match = re.search(r's(\d+)e(\d+)', col1, re.IGNORECASE)
                if se_match:
                    season = int(se_match.group(1))
                    episode = int(se_match.group(2))
                else:
                    s_match = re.search(r'\d+', col1)
                    season = default_season
                    episode = int(s_match.group()) if s_match else sequential_ep
            # 1 Column: Raw URL
            else:
                url = parts[0]
                se_match = re.search(r's(\d+)e(\d+)', url, re.IGNORECASE)
                if se_match:
                    season = int(se_match.group(1))
                    episode = int(se_match.group(2))
                else:
                    season = default_season
                    episode = sequential_ep

            ep_code = f"S{season:02d}E{episode:02d}"
            parsed_items.append({
                "season": season,
                "episode": episode,
                "epCode": ep_code,
                "downloadUrl": url
            })
            sequential_ep = episode + 1

        return parsed_items

class DuplicateChecker:
    @classmethod
    def check_duplicate(
        cls,
        title: str,
        year: Optional[str],
        media_type: str,
        on_disk_items: List[Dict[str, Any]],
        queue_items: List[Dict[str, Any]]
    ) -> Dict[str, Any]:
        """
        Evaluates duplication against on-disk files and active queue.
        Returns duplicate flags, matched paths, and episode breakdown for series.
        """
        norm_title = re.sub(r'[^a-zA-Z0-9]', '', title.lower())
        
        on_disk_match = None
        for item in on_disk_items:
            item_title = re.sub(r'[^a-zA-Z0-9]', '', item.get("title", "").lower())
            if norm_title == item_title or (norm_title in item_title and len(norm_title) > 3):
                if year and item.get("year") and str(year) != str(item.get("year")):
                    continue
                on_disk_match = item
                break

        in_queue_match = None
        for item in queue_items:
            item_title = re.sub(r'[^a-zA-Z0-9]', '', item.get("title", "").lower())
            if norm_title == item_title:
                in_queue_match = item
                break

        return {
            "hasDuplicate": bool(on_disk_match or in_queue_match),
            "onDisk": bool(on_disk_match),
            "inQueue": bool(in_queue_match),
            "diskItem": on_disk_match,
            "queueItem": in_queue_match,
            "alertBorderColor": "#F59E0B", # Amber
            "onDiskBadgeColor": "#4ADE80",  # Emerald bright
            "inQueueBadgeColor": "#38BDF8"  # Cyan / Blue
        }

class DownloadPipelineStateMachine:
    STAGES = [
        "queued",
        "gha_downloading",
        "gha_compressing",
        "gha_uploading_hf",
        "hf_ready",
        "completed"
    ]

    @classmethod
    def validate_transition(cls, current_stage: str, next_stage: str) -> bool:
        if next_stage == "failed":
            return True # Any active stage can fail
        if current_stage == "failed" and next_stage == "queued":
            return True # Retry transition
        if current_stage not in cls.STAGES or next_stage not in cls.STAGES:
            return False
        curr_idx = cls.STAGES.index(current_stage)
        next_idx = cls.STAGES.index(next_stage)
        return next_idx == curr_idx + 1

