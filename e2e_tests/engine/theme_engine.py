"""
Theme and Design System Engine
Validates Pitch Black / Emerald tokens, asymmetric SketchShape formulas,
chunky border math, typography hierarchy, and UI canvas styling.
"""

from dataclasses import dataclass
from typing import Dict, List, Tuple, Optional
import math
import re

@dataclass(frozen=True)
class ColorToken:
    name: str
    hex_code: str
    argb_int: int
    alpha: float
    red: int
    green: int
    blue: int

def parse_hex_color(hex_str: str) -> ColorToken:
    cleaned = hex_str.strip().lstrip('#')
    if len(cleaned) == 6:
        a = 0xFF
        r = int(cleaned[0:2], 16)
        g = int(cleaned[2:4], 16)
        b = int(cleaned[4:6], 16)
        argb = (a << 24) | (r << 16) | (g << 8) | b
    elif len(cleaned) == 8:
        a = int(cleaned[0:2], 16)
        r = int(cleaned[2:4], 16)
        g = int(cleaned[4:6], 16)
        b = int(cleaned[6:8], 16)
        argb = (a << 24) | (r << 16) | (g << 8) | b
    else:
        raise ValueError(f"Invalid hex color string: {hex_str}")
    return ColorToken(
        name=hex_str,
        hex_code=f"#{cleaned.upper()}",
        argb_int=argb,
        alpha=a / 255.0,
        red=r,
        green=g,
        blue=b
    )

SKETCH_COLOR_PALETTE = {
    "bg_pitch": parse_hex_color("#040404"),
    "bg_card": parse_hex_color("#0A0A0A"),
    "bg_card_hover": parse_hex_color("#111113"),
    "bg_card_selected": parse_hex_color("#161622"),
    "bg_input": parse_hex_color("#070707"),
    "graphite_100": parse_hex_color("#FFFFFF"),
    "graphite_200": parse_hex_color("#E2E2E8"),
    "graphite_300": parse_hex_color("#B4B4C0"),
    "graphite_400": parse_hex_color("#787888"),
    "graphite_500": parse_hex_color("#4A4A58"),
    "graphite_600": parse_hex_color("#3A3A42"),
    "graphite_700": parse_hex_color("#25252C"),
    "graphite_800": parse_hex_color("#1E1E28"),
    "graphite_900": parse_hex_color("#121218"),
    "sketch_border": parse_hex_color("#404048"),
    "sketch_border_active": parse_hex_color("#9E9EA8"),
    "sketch_border_white": parse_hex_color("#FFFFFF"),
    "accent_green": parse_hex_color("#22C55E"),
    "accent_green_bright": parse_hex_color("#4ADE80"),
    "accent_green_dark": parse_hex_color("#052E16"),
    "accent_red": parse_hex_color("#EF4444"),
    "accent_red_light": parse_hex_color("#F87171"),
    "accent_yellow": parse_hex_color("#EAB308"),
    "accent_yellow_bright": parse_hex_color("#FACC15"),
    "accent_amber": parse_hex_color("#F59E0B"),
    "accent_blue": parse_hex_color("#38BDF8"),
    "accent_indigo": parse_hex_color("#818CF8"),
    "accent_purple": parse_hex_color("#C084FC"),
}

@dataclass
class SketchCornerRadii:
    top_left_x: float
    top_right_x: float
    bottom_right_x: float
    bottom_left_x: float
    top_left_y: float
    top_right_y: float
    bottom_right_y: float
    bottom_left_y: float

class SketchShapeEngine:
    """
    Computes CSS 8-value asymmetric border-radius curves natively.
    CSS primary: 255px 15px 225px 15px / 15px 225px 15px 255px
    CSS alt:     15px 225px 15px 255px / 255px 15px 225px 15px
    CSS sm:      120px 8px 110px 8px / 8px 110px 8px 120px
    """
    PRIMARY_RADII = SketchCornerRadii(255, 15, 225, 15, 15, 225, 15, 255)
    ALT_RADII     = SketchCornerRadii(15, 225, 15, 255, 255, 15, 225, 15)
    SM_RADII      = SketchCornerRadii(120, 8, 110, 8, 8, 110, 8, 120)

    @classmethod
    def scale_radii(cls, base: SketchCornerRadii, width: float, height: float) -> SketchCornerRadii:
        ref_w, ref_h = 300.0, 200.0
        scale_x = width / ref_w if ref_w > 0 else 1.0
        scale_y = height / ref_h if ref_h > 0 else 1.0
        
        # Clamp maximum radius to not exceed container bounds
        max_rx = width / 2.0
        max_ry = height / 2.0
        
        return SketchCornerRadii(
            top_left_x=min(base.top_left_x * scale_x, max_rx),
            top_right_x=min(base.top_right_x * scale_x, max_rx),
            bottom_right_x=min(base.bottom_right_x * scale_x, max_rx),
            bottom_left_x=min(base.bottom_left_x * scale_x, max_rx),
            top_left_y=min(base.top_left_y * scale_y, max_ry),
            top_right_y=min(base.top_right_y * scale_y, max_ry),
            bottom_right_y=min(base.bottom_right_y * scale_y, max_ry),
            bottom_left_y=min(base.bottom_left_y * scale_y, max_ry)
        )

    @classmethod
    def get_border_width_range(cls) -> Tuple[float, float]:
        # Standard chunky border width between 2.0dp and 2.5dp
        return (2.0, 2.5)

class TypographyHierarchy:
    FONTS = {
        "heading_handwritten": "Architects Daughter",
        "body_primary": "Space Grotesk",
        "monospace_code": "JetBrains Mono",
        "card_emboss": "Share Tech Mono"
    }

    @classmethod
    def resolve_font_for_element(cls, element_type: str) -> str:
        mapping = {
            "brand_title": cls.FONTS["heading_handwritten"],
            "modal_title": cls.FONTS["heading_handwritten"],
            "card_badge": cls.FONTS["heading_handwritten"],
            "button_text": cls.FONTS["body_primary"],
            "body_text": cls.FONTS["body_primary"],
            "form_label": cls.FONTS["body_primary"],
            "code_editor": cls.FONTS["monospace_code"],
            "terminal_buffer": cls.FONTS["monospace_code"],
            "timestamp": cls.FONTS["monospace_code"],
            "episode_code": cls.FONTS["monospace_code"],
            "posix_perm": cls.FONTS["monospace_code"],
            "card_number": cls.FONTS["card_emboss"],
            "card_expiry": cls.FONTS["card_emboss"],
            "card_cvv": cls.FONTS["card_emboss"]
        }
        return mapping.get(element_type, cls.FONTS["body_primary"])

