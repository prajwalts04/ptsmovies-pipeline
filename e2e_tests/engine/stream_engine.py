"""
Stream Engine
Validates media catalog models, multi-layer fuzzy search engine,
native ExoPlayer controller simulation, progress tracking, and watchlist sync.
"""

import unicodedata
import re
from typing import List, Dict, Any, Optional

class FuzzySearchEngine:
    ROMAN_NUMERAL_MAP = {
        "I": "1", "II": "2", "III": "3", "IV": "4", "V": "5",
        "VI": "6", "VII": "7", "VIII": "8", "IX": "9", "X": "10",
        "XI": "11", "XII": "12", "XIII": "13", "XIV": "14", "XV": "15",
        "XVI": "16", "XVII": "17", "XVIII": "18", "XIX": "19", "XX": "20"
    }
    REVERSE_ROMAN_MAP = {v: k for k, v in ROMAN_NUMERAL_MAP.items()}

    @classmethod
    def normalize_text(cls, text: str) -> str:
        if not text:
            return ""
        # 1. Normalize unicode diacritics
        nfkd_form = unicodedata.normalize('NFD', text)
        ascii_text = "".join([c for c in nfkd_form if not unicodedata.combining(c)])
        # 2. Lowercase and replace non-alphanumeric (except spaces)
        cleaned = re.sub(r'[^a-zA-Z0-9\s]', ' ', ascii_text.lower())
        # 3. Collapse multiple spaces
        return re.sub(r'\s+', ' ', cleaned).strip()

    @classmethod
    def strip_punctuation(cls, text: str) -> str:
        if not text:
            return ""
        norm = cls.normalize_text(text)
        return re.sub(r'[^a-z0-9]', '', norm)

    @classmethod
    def expand_roman_numerals(cls, text: str) -> List[str]:
        tokens = text.split()
        expanded_tokens_list = [[]]
        for token in tokens:
            upper_token = token.upper()
            variants = [token]
            if upper_token in cls.ROMAN_NUMERAL_MAP:
                variants.append(cls.ROMAN_NUMERAL_MAP[upper_token])
            if token in cls.REVERSE_ROMAN_MAP:
                variants.append(cls.REVERSE_ROMAN_MAP[token].lower())
            
            new_list = []
            for prefix in expanded_tokens_list:
                for v in variants:
                    new_list.append(prefix + [v])
            expanded_tokens_list = new_list
        
        return list(dict.fromkeys([" ".join(combo) for combo in expanded_tokens_list]))

    @classmethod
    def is_subsequence_acronym(cls, query: str, target: str) -> bool:
        q = re.sub(r'[^a-zA-Z0-9]', '', query.lower())
        t_words = [w for w in re.split(r'[^a-zA-Z0-9]', target.lower()) if w]
        if not q or not t_words:
            return False
        # First check initials
        initials = "".join([w[0] for w in t_words])
        if q == initials or q in initials:
            return True
        # Check strict subsequence across concatenated target
        t_concat = "".join(t_words)
        it = iter(t_concat)
        return all(char in it for char in q)

    @classmethod
    def match(cls, query: str, target_fields: List[str]) -> bool:
        if not query or not query.strip():
            return True
        norm_query = cls.normalize_text(query)
        if not norm_query:
            return True
        query_variants = cls.expand_roman_numerals(norm_query)
        combined_target = " ".join(target_fields)
        norm_target = cls.normalize_text(combined_target)
        target_variants = cls.expand_roman_numerals(norm_target)

        # 1. Direct substring match with any roman variant
        for q_var in query_variants:
            for t_var in target_variants:
                if q_var in t_var:
                    return True
            q_tokens = q_var.split()
            if all(t in norm_target for t in q_tokens):
                return True

        # 2. Compact / Punctuation-stripped match
        compact_query_variants = [re.sub(r'[^a-z0-9]', '', q_var) for q_var in query_variants]
        compact_target_variants = [re.sub(r'[^a-z0-9]', '', t_var) for t_var in target_variants]
        for cq in compact_query_variants:
            if len(cq) >= 2:
                for ct in compact_target_variants:
                    if cq in ct:
                        return True

        # 3. Token-level acronym/word match
        for q_var in query_variants:
            q_tokens = q_var.split()
            matched_count = 0
            for t in q_tokens:
                num_t = cls.ROMAN_NUMERAL_MAP.get(t.upper()) or cls.REVERSE_ROMAN_MAP.get(t, '').lower()
                matched = False
                for field in target_fields:
                    f_words = [re.sub(r'[^a-z0-9]', '', w.lower()) for w in field.split() if w]
                    f_initials = "".join([w[0] for w in f_words if w])
                    f_compact = re.sub(r'[^a-z0-9]', '', field.lower())
                    if t in f_words or (num_t and num_t in f_words) or t == f_initials or t in f_initials or t in f_compact or (num_t and num_t in f_compact):
                        matched = True
                        break
                if matched:
                    matched_count += 1
            if matched_count == len(q_tokens):
                return True

        # 4. Acronym match against primary title
        if target_fields and cls.is_subsequence_acronym(query, target_fields[0]):
            return True

        return False

class ExoPlayerSimulator:
    ALLOWED_SPEEDS = [0.5, 0.75, 1.0, 1.25, 1.5, 2.0]
    AUTO_HIDE_TIMEOUT_MS = 3500

    def __init__(self, duration_seconds: float):
        self.duration = max(0.0, duration_seconds)
        self.current_position = 0.0
        self.buffered_position = min(duration_seconds, duration_seconds * 0.25)
        self.playback_speed = 1.0
        self.is_playing = False
        self.controls_visible = True
        self.last_user_interaction_ms = 0

    def play(self):
        self.is_playing = True

    def pause(self):
        self.is_playing = False

    def toggle_play_pause(self):
        self.is_playing = not self.is_playing

    def seek_by(self, delta_seconds: float):
        new_pos = self.current_position + delta_seconds
        self.current_position = max(0.0, min(self.duration, new_pos))

    def seek_to(self, position_seconds: float):
        self.current_position = max(0.0, min(self.duration, position_seconds))

    def double_tap_seek_forward(self):
        self.seek_by(10.0)

    def double_tap_seek_rewind(self):
        self.seek_by(-10.0)

    def set_playback_speed(self, speed: float):
        if speed in self.ALLOWED_SPEEDS:
            self.playback_speed = speed
        else:
            raise ValueError(f"Invalid playback speed: {speed}. Allowed: {self.ALLOWED_SPEEDS}")

    def update_buffer(self, buffered_seconds: float):
        self.buffered_position = max(0.0, min(self.duration, buffered_seconds))

    def check_controls_auto_hide(self, current_elapsed_ms: int) -> bool:
        if self.is_playing and (current_elapsed_ms - self.last_user_interaction_ms >= self.AUTO_HIDE_TIMEOUT_MS):
            self.controls_visible = False
        return self.controls_visible

class WatchlistAndProgressTracker:
    @classmethod
    def calculate_progress_percentage(cls, position_seconds: float, duration_seconds: float) -> float:
        if duration_seconds <= 0:
            return 0.0
        return max(0.0, min(1.0, position_seconds / duration_seconds))

    @classmethod
    def should_include_in_continue_watching(cls, position_seconds: float, duration_seconds: float) -> bool:
        pct = cls.calculate_progress_percentage(position_seconds, duration_seconds)
        # Continue watching window: 1% to 95% complete
        return 0.01 <= pct <= 0.95
