"""
Terminal Engine
Validates JSch OpenSSH session lifecycle, ANSI escape parsing,
mobile accessory key byte mappings, sticky modifier transitions,
session persistence replay, and dynamic PTY resizing.
"""

import re
from typing import Dict, Any, List, Optional, Tuple

class AnsiSequenceParser:
    ANSI_ESCAPE_RE = re.compile(r'\x1B(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])')

    @classmethod
    def strip_ansi(cls, text: str) -> str:
        return cls.ANSI_ESCAPE_RE.sub('', text)

    @classmethod
    def extract_color_codes(cls, text: str) -> List[str]:
        codes = []
        for match in cls.ANSI_ESCAPE_RE.finditer(text):
            seq = match.group(0)
            if seq.startswith('\x1B[') and seq.endswith('m'):
                codes.append(seq[2:-1])
        return codes

class TerminalKeyRowEngine:
    KEY_BYTE_MAPPINGS = {
        "ESC": "\x1b",
        "TAB": "\t",
        "UP": "\x1b[A",
        "DOWN": "\x1b[B",
        "LEFT": "\x1b[D",
        "RIGHT": "\x1b[C",
        "HOME": "\x1b[H",
        "END": "\x1b[F",
        "CTRL_C": "\x03",
        "CTRL_D": "\x04",
        "CTRL_Z": "\x1a",
        "CTRL_L": "\x0c"
    }

    QUICK_COMMANDS = [
        "htop", "docker ps", "pm2 status", "git status",
        "ifconfig", "df -h", "free -m", "ls -la"
    ]

    UNIX_SYMBOLS = ["|", "/", "\\", "~", "-", "_", ":", ";", "'", "\"", "`", "$", "&", "#", "!", "*", ">", "<", "=", "?"]

    def __init__(self):
        self.ctrl_sticky = False
        self.alt_sticky = False
        self.font_size_sp = 13

    def toggle_ctrl(self) -> bool:
        self.ctrl_sticky = not self.ctrl_sticky
        return self.ctrl_sticky

    def toggle_alt(self) -> bool:
        self.alt_sticky = not self.alt_sticky
        return self.alt_sticky

    def process_key_press(self, key_name: str) -> str:
        # Check standard accessory keys
        if key_name in self.KEY_BYTE_MAPPINGS:
            return self.KEY_BYTE_MAPPINGS[key_name]

        # Check single character with sticky modifiers
        if len(key_name) == 1:
            char = key_name
            if self.ctrl_sticky:
                self.ctrl_sticky = False
                # Ctrl+A is 1, Ctrl+C is 3, Ctrl+Z is 26
                code = ord(char.upper()) - 64
                if 1 <= code <= 26:
                    return chr(code)
            if self.alt_sticky:
                self.alt_sticky = False
                return f"\x1b{char}"
            return char

        return key_name

    def adjust_font_size(self, delta: int) -> int:
        new_size = max(10, min(22, self.font_size_sp + delta))
        self.font_size_sp = new_size
        return self.font_size_sp

class TerminalSessionRingBuffer:
    def __init__(self, capacity: int = 10000):
        self.capacity = capacity
        self.buffer = ""
        self.session_id: Optional[str] = None

    def create_session(self, session_id: str):
        self.session_id = session_id
        self.buffer = ""

    def append_output(self, data: str):
        self.buffer += data
        if len(self.buffer) > self.capacity:
            self.buffer = self.buffer[-self.capacity:]

    def get_replay_data(self) -> str:
        return self.buffer

class PtyDimensionCalculator:
    @classmethod
    def calculate_dimensions(cls, screen_width_dp: float, screen_height_dp: float, font_size_sp: float) -> Tuple[int, int]:
        char_w = font_size_sp * 0.62
        char_h = font_size_sp * 1.35
        cols = max(10, int(screen_width_dp / char_w))
        rows = max(5, int(screen_height_dp / char_h))
        return (cols, rows)

