"""
Vault Engine
Validates biometric authentication, 3D stacked card deck UI layout calculations,
10 multi-template card parsers/formatters, and confidential notes CRUD.
"""

import re
from typing import Dict, Any, List, Optional

class BiometricAuthSimulator:
    MAX_FAILED_ATTEMPTS = 5

    def __init__(self, hardware_present: bool = True, enrolled: bool = True):
        self.hardware_present = hardware_present
        self.enrolled = enrolled
        self.failed_attempts = 0
        self.is_locked_out = False
        self.is_authenticated = False

    def authenticate_biometric(self, success: bool) -> Dict[str, Any]:
        if not self.hardware_present or not self.enrolled:
            return {"success": False, "error": "BIOMETRIC_UNAVAILABLE", "fallback_to_pin": True}
        if self.is_locked_out:
            return {"success": False, "error": "BIOMETRIC_LOCKED_OUT", "fallback_to_pin": True}

        if success:
            self.is_authenticated = True
            self.failed_attempts = 0
            return {"success": True, "error": None, "fallback_to_pin": False}
        else:
            self.failed_attempts += 1
            if self.failed_attempts >= self.MAX_FAILED_ATTEMPTS:
                self.is_locked_out = True
                return {"success": False, "error": "BIOMETRIC_LOCKOUT", "fallback_to_pin": True}
            return {"success": False, "error": "BIOMETRIC_FAILED", "fallback_to_pin": False}

    def authenticate_with_pin(self, pin: str, correct_pin: str) -> bool:
        if pin == correct_pin:
            self.is_authenticated = True
            self.is_locked_out = False
            self.failed_attempts = 0
            return True
        return False

class StackedCardDeckLayout:
    DEFAULT_OVERLAP_OFFSET_DP = -160.0
    HOVER_LIFT_OFFSET_DP = -28.0
    HOVER_SCALE = 1.02
    SELECTED_POP_OFFSET_DP = -40.0
    SELECTED_SCALE = 1.03

    @classmethod
    def calculate_card_transform(cls, card_index: int, selected_index: Optional[int], is_hovered: bool) -> Dict[str, float]:
        base_y = card_index * 80.0 # Effective spaced position
        if selected_index is not None and selected_index == card_index:
            return {
                "offset_y": base_y + cls.SELECTED_POP_OFFSET_DP,
                "scale": cls.SELECTED_SCALE,
                "z_index": 100.0
            }
        elif is_hovered:
            return {
                "offset_y": base_y + cls.HOVER_LIFT_OFFSET_DP,
                "scale": cls.HOVER_SCALE,
                "z_index": 50.0
            }
        else:
            return {
                "offset_y": base_y,
                "scale": 1.0,
                "z_index": float(card_index)
            }

class CardTemplateFormatter:
    @classmethod
    def format_aadhaar(cls, num: str, mask: bool = True) -> str:
        digits = re.sub(r'\D', '', num)
        if len(digits) != 12:
            return num
        if mask:
            return f"XXXX XXXX {digits[8:12]}"
        return f"{digits[0:4]} {digits[4:8]} {digits[8:12]}"

    @classmethod
    def format_pan(cls, pan: str) -> str:
        cleaned = re.sub(r'[^a-zA-Z0-9]', '', pan).upper()
        if re.match(r'^[A-Z]{5}[0-9]{4}[A-Z]$', cleaned):
            return cleaned
        return pan

    @classmethod
    def format_bank_card(cls, num: str, mask: bool = True) -> str:
        digits = re.sub(r'\D', '', num)
        if len(digits) != 16:
            return num
        if mask:
            return f"XXXX-XXXX-XXXX-{digits[12:16]}"
        return f"{digits[0:4]} {digits[4:8]} {digits[8:12]} {digits[12:16]}"

    @classmethod
    def validate_card(cls, doc_type: str, number: str) -> bool:
        cleaned = re.sub(r'[\s\-]', '', number)
        if doc_type == "aadhaar":
            return len(cleaned) == 12 and cleaned.isdigit()
        elif doc_type == "pan":
            return bool(re.match(r'^[A-Z]{5}[0-9]{4}[A-Z]$', cleaned.upper()))
        elif doc_type in ("bank_card", "debit", "credit"):
            return len(cleaned) == 16 and cleaned.isdigit()
        elif doc_type == "passport":
            return bool(re.match(r'^[A-Z][0-9]{7}$', cleaned.upper()))
        elif doc_type == "rc":
            return bool(re.match(r'^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$', cleaned.upper()))
        elif doc_type == "voter":
            return bool(re.match(r'^[A-Z]{3}[0-9]{7}$', cleaned.upper()))
        return len(number.strip()) > 0

