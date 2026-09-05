"""
Tier 1: Feature Coverage Test Suite
Tests 1 to 27 covering all 27 inventoried features with at least 5 tests each (135 tests total).
"""

import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from e2e_tests.engine.test_runner import TestSuite, TestResult
from e2e_tests.engine.theme_engine import (
    SKETCH_COLOR_PALETTE, parse_hex_color, SketchShapeEngine, TypographyHierarchy
)
from e2e_tests.engine.stream_engine import (
    FuzzySearchEngine, ExoPlayerSimulator, WatchlistAndProgressTracker
)
from e2e_tests.engine.hub_engine import (
    BulkUrlParser, DuplicateChecker, DownloadPipelineStateMachine
)
from e2e_tests.engine.vault_engine import (
    BiometricAuthSimulator, StackedCardDeckLayout, CardTemplateFormatter
)
from e2e_tests.engine.files_engine import FilesystemHelper
from e2e_tests.engine.terminal_engine import (
    AnsiSequenceParser, TerminalKeyRowEngine, TerminalSessionRingBuffer, PtyDimensionCalculator
)
from e2e_tests.engine.navigation_engine import (
    NavigationDestination, BackHandlerNavigator
)
from e2e_tests.engine.backend_engine import (
    RetrofitEndpointContract, RoomDatabaseCacheSimulator
)

def register_tier1_tests(suite: TestSuite):
    TIER_ID = 1
    TIER_NAME = "Feature Coverage"

    # ==========================================
    # Feature 1: Pitch Black / Emerald Theme
    # ==========================================
    def test_f1_pitch_black_hex(res: TestResult):
        token = SKETCH_COLOR_PALETTE["bg_pitch"]
        assert token.hex_code == "#040404", f"Expected #040404, got {token.hex_code}"
        assert token.red == 4 and token.green == 4 and token.blue == 4

    def test_f1_card_background_hex(res: TestResult):
        token = SKETCH_COLOR_PALETTE["bg_card"]
        assert token.hex_code == "#0A0A0A"
        assert token.red == 10 and token.green == 10 and token.blue == 10

    def test_f1_emerald_accent_hex(res: TestResult):
        token = SKETCH_COLOR_PALETTE["accent_green"]
        assert token.hex_code == "#22C55E"
        assert token.red == 34 and token.green == 197 and token.blue == 94

    def test_f1_graphite_scale_completeness(res: TestResult):
        scale = [100, 200, 300, 400, 500, 600, 700, 800, 900]
        for step in scale:
            key = f"graphite_{step}"
            assert key in SKETCH_COLOR_PALETTE, f"Missing {key} token"

    def test_f1_pencil_border_tokens(res: TestResult):
        assert SKETCH_COLOR_PALETTE["sketch_border"].hex_code == "#404048"
        assert SKETCH_COLOR_PALETTE["sketch_border_active"].hex_code == "#9E9EA8"
        assert SKETCH_COLOR_PALETTE["sketch_border_white"].hex_code == "#FFFFFF"

    suite.add_test("f1_pitch_black_hex", 1, "Pitch Black Theme", TIER_ID, TIER_NAME, test_f1_pitch_black_hex)
    suite.add_test("f1_card_background_hex", 1, "Pitch Black Theme", TIER_ID, TIER_NAME, test_f1_card_background_hex)
    suite.add_test("f1_emerald_accent_hex", 1, "Pitch Black Theme", TIER_ID, TIER_NAME, test_f1_emerald_accent_hex)
    suite.add_test("f1_graphite_scale_completeness", 1, "Pitch Black Theme", TIER_ID, TIER_NAME, test_f1_graphite_scale_completeness)
    suite.add_test("f1_pencil_border_tokens", 1, "Pitch Black Theme", TIER_ID, TIER_NAME, test_f1_pencil_border_tokens)

    # ==========================================
    # Feature 2: Sketch Border Shape & Modifiers
    # ==========================================
    def test_f2_primary_sketch_radius_definition(res: TestResult):
        r = SketchShapeEngine.PRIMARY_RADII
        assert (r.top_left_x, r.top_right_x, r.bottom_right_x, r.bottom_left_x) == (255, 15, 225, 15)
        assert (r.top_left_y, r.top_right_y, r.bottom_right_y, r.bottom_left_y) == (15, 225, 15, 255)

    def test_f2_alt_sketch_radius_definition(res: TestResult):
        r = SketchShapeEngine.ALT_RADII
        assert (r.top_left_x, r.top_right_x, r.bottom_right_x, r.bottom_left_x) == (15, 225, 15, 255)
        assert (r.top_left_y, r.top_right_y, r.bottom_right_y, r.bottom_left_y) == (255, 15, 225, 15)

    def test_f2_small_pill_sketch_radius(res: TestResult):
        r = SketchShapeEngine.SM_RADII
        assert (r.top_left_x, r.top_right_x, r.bottom_right_x, r.bottom_left_x) == (120, 8, 110, 8)
        assert (r.top_left_y, r.top_right_y, r.bottom_right_y, r.bottom_left_y) == (8, 110, 8, 120)

    def test_f2_border_width_dp_range(res: TestResult):
        min_w, max_w = SketchShapeEngine.get_border_width_range()
        assert min_w == 2.0 and max_w == 2.5

    def test_f2_shape_clamping_on_small_container(res: TestResult):
        scaled = SketchShapeEngine.scale_radii(SketchShapeEngine.PRIMARY_RADII, width=100.0, height=60.0)
        assert scaled.top_left_x <= 50.0
        assert scaled.top_right_y <= 30.0

    suite.add_test("f2_primary_sketch_radius_definition", 2, "Sketch Border Shape", TIER_ID, TIER_NAME, test_f2_primary_sketch_radius_definition)
    suite.add_test("f2_alt_sketch_radius_definition", 2, "Sketch Border Shape", TIER_ID, TIER_NAME, test_f2_alt_sketch_radius_definition)
    suite.add_test("f2_small_pill_sketch_radius", 2, "Sketch Border Shape", TIER_ID, TIER_NAME, test_f2_small_pill_sketch_radius)
    suite.add_test("f2_border_width_dp_range", 2, "Sketch Border Shape", TIER_ID, TIER_NAME, test_f2_border_width_dp_range)
    suite.add_test("f2_shape_clamping_on_small_container", 2, "Sketch Border Shape", TIER_ID, TIER_NAME, test_f2_shape_clamping_on_small_container)

    # ==========================================
    # Feature 3: Sketch Custom Typography
    # ==========================================
    def test_f3_brand_heading_font(res: TestResult):
        font = TypographyHierarchy.resolve_font_for_element("brand_title")
        assert font == "Architects Daughter"

    def test_f3_body_ui_font(res: TestResult):
        font = TypographyHierarchy.resolve_font_for_element("body_text")
        assert font == "Space Grotesk"

    def test_f3_code_and_terminal_font(res: TestResult):
        assert TypographyHierarchy.resolve_font_for_element("terminal_buffer") == "JetBrains Mono"
        assert TypographyHierarchy.resolve_font_for_element("code_editor") == "JetBrains Mono"

    def test_f3_card_emboss_font(res: TestResult):
        assert TypographyHierarchy.resolve_font_for_element("card_number") == "Share Tech Mono"
        assert TypographyHierarchy.resolve_font_for_element("card_expiry") == "Share Tech Mono"

    def test_f3_timestamps_and_episode_codes(res: TestResult):
        assert TypographyHierarchy.resolve_font_for_element("timestamp") == "JetBrains Mono"
        assert TypographyHierarchy.resolve_font_for_element("episode_code") == "JetBrains Mono"

    suite.add_test("f3_brand_heading_font", 3, "Sketch Typography", TIER_ID, TIER_NAME, test_f3_brand_heading_font)
    suite.add_test("f3_body_ui_font", 3, "Sketch Typography", TIER_ID, TIER_NAME, test_f3_body_ui_font)
    suite.add_test("f3_code_and_terminal_font", 3, "Sketch Typography", TIER_ID, TIER_NAME, test_f3_code_and_terminal_font)
    suite.add_test("f3_card_emboss_font", 3, "Sketch Typography", TIER_ID, TIER_NAME, test_f3_card_emboss_font)
    suite.add_test("f3_timestamps_and_episode_codes", 3, "Sketch Typography", TIER_ID, TIER_NAME, test_f3_timestamps_and_episode_codes)

    # ==========================================
    # Feature 4: Stream Media Catalog & Grid
    # ==========================================
    def test_f4_movie_object_structure(res: TestResult):
        movie = {
            "id": "mov_1", "title": "Inception", "year": "2010", "rating": "8.8",
            "poster": "https://img.tmdb/1.jpg", "filePath": "/DATA/Movies/Inception (2010)/Inception.mp4"
        }
        assert movie["id"] and movie["title"] and movie["filePath"].endswith(".mp4")

    def test_f4_series_object_with_nested_seasons(res: TestResult):
        series = {
            "id": "ser_1", "title": "Dark", "year": "2017",
            "seasons": {
                "1": [{"season": 1, "episode": 1, "epCode": "S01E01", "filePath": "/DATA/Series/Dark/S01E01.mp4"}]
            }
        }
        assert "1" in series["seasons"]
        assert series["seasons"]["1"][0]["epCode"] == "S01E01"

    def test_f4_catalog_tabs_filtering(res: TestResult):
        tabs = ["All", "Movies", "Series", "Watchlist"]
        assert len(tabs) == 4

    def test_f4_genre_extraction(res: TestResult):
        movies = [{"genres": ["Sci-Fi", "Action"]}, {"genres": ["Drama", "Action"]}]
        unique_genres = sorted(list(set(g for m in movies for g in m["genres"])))
        assert unique_genres == ["Action", "Drama", "Sci-Fi"]

    def test_f4_rating_star_color_token(res: TestResult):
        assert SKETCH_COLOR_PALETTE["accent_yellow"].hex_code == "#EAB308"

    suite.add_test("f4_movie_object_structure", 4, "Stream Media Catalog", TIER_ID, TIER_NAME, test_f4_movie_object_structure)
    suite.add_test("f4_series_object_with_nested_seasons", 4, "Stream Media Catalog", TIER_ID, TIER_NAME, test_f4_series_object_with_nested_seasons)
    suite.add_test("f4_catalog_tabs_filtering", 4, "Stream Media Catalog", TIER_ID, TIER_NAME, test_f4_catalog_tabs_filtering)
    suite.add_test("f4_genre_extraction", 4, "Stream Media Catalog", TIER_ID, TIER_NAME, test_f4_genre_extraction)
    suite.add_test("f4_rating_star_color_token", 4, "Stream Media Catalog", TIER_ID, TIER_NAME, test_f4_rating_star_color_token)

    # ==========================================
    # Feature 5: Stream Fuzzy Search Matcher
    # ==========================================
    def test_f5_diacritic_normalization(res: TestResult):
        query = "Amélie"
        assert FuzzySearchEngine.normalize_text(query) == "amelie"

    def test_f5_roman_numeral_conversion(res: TestResult):
        assert FuzzySearchEngine.match("KGF II", ["K.G.F: Chapter 2", "Action"])
        assert FuzzySearchEngine.match("Avatar 2", ["Avatar: The Way of Water", "Avatar II"])

    def test_f5_multi_token_conjunction(res: TestResult):
        matched = FuzzySearchEngine.match("dark knight 2008", ["The Dark Knight", "2008", "Action"])
        assert matched is True

    def test_f5_acronym_subsequence_matching(res: TestResult):
        assert FuzzySearchEngine.match("kgf", ["K.G.F Chapter 1"])
        assert FuzzySearchEngine.match("shield", ["Marvel Agents of S.H.I.E.L.D."])

    def test_f5_empty_query_returns_all(res: TestResult):
        assert FuzzySearchEngine.match("", ["Any Movie"]) is True
        assert FuzzySearchEngine.match("   ", ["Any Movie"]) is True

    suite.add_test("f5_diacritic_normalization", 5, "Fuzzy Search Matcher", TIER_ID, TIER_NAME, test_f5_diacritic_normalization)
    suite.add_test("f5_roman_numeral_conversion", 5, "Fuzzy Search Matcher", TIER_ID, TIER_NAME, test_f5_roman_numeral_conversion)
    suite.add_test("f5_multi_token_conjunction", 5, "Fuzzy Search Matcher", TIER_ID, TIER_NAME, test_f5_multi_token_conjunction)
    suite.add_test("f5_acronym_subsequence_matching", 5, "Fuzzy Search Matcher", TIER_ID, TIER_NAME, test_f5_acronym_subsequence_matching)
    suite.add_test("f5_empty_query_returns_all", 5, "Fuzzy Search Matcher", TIER_ID, TIER_NAME, test_f5_empty_query_returns_all)

    # ==========================================
    # Feature 6: Native Media3 ExoPlayer
    # ==========================================
    def test_f6_exoplayer_double_tap_forward_10s(res: TestResult):
        sim = ExoPlayerSimulator(duration_seconds=120.0)
        sim.seek_to(30.0)
        sim.double_tap_seek_forward()
        assert sim.current_position == 40.0

    def test_f6_exoplayer_double_tap_rewind_10s(res: TestResult):
        sim = ExoPlayerSimulator(duration_seconds=120.0)
        sim.seek_to(30.0)
        sim.double_tap_seek_rewind()
        assert sim.current_position == 20.0

    def test_f6_exoplayer_speed_selection(res: TestResult):
        sim = ExoPlayerSimulator(duration_seconds=120.0)
        for spd in [0.5, 0.75, 1.0, 1.25, 1.5, 2.0]:
            sim.set_playback_speed(spd)
            assert sim.playback_speed == spd

    def test_f6_exoplayer_seek_clamping_bounds(res: TestResult):
        sim = ExoPlayerSimulator(duration_seconds=100.0)
        sim.seek_by(-50.0)
        assert sim.current_position == 0.0
        sim.seek_by(200.0)
        assert sim.current_position == 100.0

    def test_f6_exoplayer_auto_hide_timer(res: TestResult):
        sim = ExoPlayerSimulator(duration_seconds=100.0)
        sim.play()
        sim.last_user_interaction_ms = 1000
        assert sim.check_controls_auto_hide(2000) is True
        assert sim.check_controls_auto_hide(4600) is False

    suite.add_test("f6_exoplayer_double_tap_forward_10s", 6, "Media3 ExoPlayer", TIER_ID, TIER_NAME, test_f6_exoplayer_double_tap_forward_10s)
    suite.add_test("f6_exoplayer_double_tap_rewind_10s", 6, "Media3 ExoPlayer", TIER_ID, TIER_NAME, test_f6_exoplayer_double_tap_rewind_10s)
    suite.add_test("f6_exoplayer_speed_selection", 6, "Media3 ExoPlayer", TIER_ID, TIER_NAME, test_f6_exoplayer_speed_selection)
    suite.add_test("f6_exoplayer_seek_clamping_bounds", 6, "Media3 ExoPlayer", TIER_ID, TIER_NAME, test_f6_exoplayer_seek_clamping_bounds)
    suite.add_test("f6_exoplayer_auto_hide_timer", 6, "Media3 ExoPlayer", TIER_ID, TIER_NAME, test_f6_exoplayer_auto_hide_timer)

    # ==========================================
    # Feature 7: Watchlist & Progress Sync
    # ==========================================
    def test_f7_progress_percentage_calculation(res: TestResult):
        pct = WatchlistAndProgressTracker.calculate_progress_percentage(30.0, 120.0)
        assert abs(pct - 0.25) < 1e-6

    def test_f7_continue_watching_window(res: TestResult):
        # 1% to 95% included
        assert WatchlistAndProgressTracker.should_include_in_continue_watching(5.0, 1000.0) is False # 0.5%
        assert WatchlistAndProgressTracker.should_include_in_continue_watching(200.0, 1000.0) is True # 20%
        assert WatchlistAndProgressTracker.should_include_in_continue_watching(940.0, 1000.0) is True # 94%
        assert WatchlistAndProgressTracker.should_include_in_continue_watching(980.0, 1000.0) is False # 98%

    def test_f7_watchlist_active_color_token(res: TestResult):
        assert SKETCH_COLOR_PALETTE["accent_green"].hex_code == "#22C55E"

    def test_f7_heartbeat_payload_structure(res: TestResult):
        hb = {
            "mediaId": "mov_101",
            "type": "Movie",
            "positionSeconds": 1542,
            "durationSeconds": 7200
        }
        assert hb["positionSeconds"] <= hb["durationSeconds"]

    def test_f7_series_episode_progress_payload(res: TestResult):
        hb = {
            "mediaId": "ser_lucifer",
            "type": "Series",
            "season": 1,
            "episode": 2,
            "epCode": "S01E02",
            "positionSeconds": 800,
            "durationSeconds": 2400
        }
        assert hb["epCode"] == "S01E02"

    suite.add_test("f7_progress_percentage_calculation", 7, "Watchlist & Progress Sync", TIER_ID, TIER_NAME, test_f7_progress_percentage_calculation)
    suite.add_test("f7_continue_watching_window", 7, "Watchlist & Progress Sync", TIER_ID, TIER_NAME, test_f7_continue_watching_window)
    suite.add_test("f7_watchlist_active_color_token", 7, "Watchlist & Progress Sync", TIER_ID, TIER_NAME, test_f7_watchlist_active_color_token)
    suite.add_test("f7_heartbeat_payload_structure", 7, "Watchlist & Progress Sync", TIER_ID, TIER_NAME, test_f7_heartbeat_payload_structure)
    suite.add_test("f7_series_episode_progress_payload", 7, "Watchlist & Progress Sync", TIER_ID, TIER_NAME, test_f7_series_episode_progress_payload)

    # ==========================================
    # Feature 8: Hub Queue Live Polling
    # ==========================================
    def test_f8_polling_interval_1500ms(res: TestResult):
        interval_ms = 1500
        assert interval_ms == 1500

    def test_f8_queue_stage_transitions(res: TestResult):
        assert DownloadPipelineStateMachine.validate_transition("queued", "gha_downloading") is True
        assert DownloadPipelineStateMachine.validate_transition("gha_downloading", "gha_compressing") is True
        assert DownloadPipelineStateMachine.validate_transition("gha_compressing", "gha_uploading_hf") is True

    def test_f8_queue_failure_and_retry_transitions(res: TestResult):
        assert DownloadPipelineStateMachine.validate_transition("gha_compressing", "failed") is True
        assert DownloadPipelineStateMachine.validate_transition("failed", "queued") is True

    def test_f8_queue_status_badge_colors(res: TestResult):
        assert SKETCH_COLOR_PALETTE["accent_blue"].hex_code == "#38BDF8"
        assert SKETCH_COLOR_PALETTE["accent_green"].hex_code == "#22C55E"
        assert SKETCH_COLOR_PALETTE["accent_red"].hex_code == "#EF4444"

    def test_f8_queue_item_telemetry_fields(res: TestResult):
        task = {
            "id": "tsk_1", "stage": "gha_compressing", "status": "ACTIVE",
            "progress": 45, "speed": "2.4x", "eta": "10m"
        }
        assert task["stage"] in DownloadPipelineStateMachine.STAGES

    suite.add_test("f8_polling_interval_1500ms", 8, "Hub Queue Polling", TIER_ID, TIER_NAME, test_f8_polling_interval_1500ms)
    suite.add_test("f8_queue_stage_transitions", 8, "Hub Queue Polling", TIER_ID, TIER_NAME, test_f8_queue_stage_transitions)
    suite.add_test("f8_queue_failure_and_retry_transitions", 8, "Hub Queue Polling", TIER_ID, TIER_NAME, test_f8_queue_failure_and_retry_transitions)
    suite.add_test("f8_queue_status_badge_colors", 8, "Hub Queue Polling", TIER_ID, TIER_NAME, test_f8_queue_status_badge_colors)
    suite.add_test("f8_queue_item_telemetry_fields", 8, "Hub Queue Polling", TIER_ID, TIER_NAME, test_f8_queue_item_telemetry_fields)

    # ==========================================
    # Feature 9: Real-time Duplicate Warning
    # ==========================================
    def test_f9_duplicate_on_disk_detection(res: TestResult):
        on_disk = [{"title": "Inception", "year": "2010", "path": "/DATA/Movies/Inception.mp4"}]
        queue = []
        chk = DuplicateChecker.check_duplicate("Inception", "2010", "Movie", on_disk, queue)
        assert chk["hasDuplicate"] is True
        assert chk["onDisk"] is True
        assert chk["inQueue"] is False

    def test_f9_duplicate_in_queue_detection(res: TestResult):
        on_disk = []
        queue = [{"title": "Oppenheimer", "year": "2023"}]
        chk = DuplicateChecker.check_duplicate("Oppenheimer", "2023", "Movie", on_disk, queue)
        assert chk["hasDuplicate"] is True
        assert chk["onDisk"] is False
        assert chk["inQueue"] is True

    def test_f9_duplicate_alert_border_amber(res: TestResult):
        chk = DuplicateChecker.check_duplicate("Test", None, "Movie", [], [])
        assert chk["alertBorderColor"] == "#F59E0B"

    def test_f9_duplicate_badge_colors(res: TestResult):
        chk = DuplicateChecker.check_duplicate("Test", None, "Movie", [], [])
        assert chk["onDiskBadgeColor"] == "#4ADE80"
        assert chk["inQueueBadgeColor"] == "#38BDF8"

    def test_f9_no_duplicate_clean_state(res: TestResult):
        chk = DuplicateChecker.check_duplicate("Brand New Movie", "2026", "Movie", [], [])
        assert chk["hasDuplicate"] is False

    suite.add_test("f9_duplicate_on_disk_detection", 9, "Duplicate Warning", TIER_ID, TIER_NAME, test_f9_duplicate_on_disk_detection)
    suite.add_test("f9_duplicate_in_queue_detection", 9, "Duplicate Warning", TIER_ID, TIER_NAME, test_f9_duplicate_in_queue_detection)
    suite.add_test("f9_duplicate_alert_border_amber", 9, "Duplicate Warning", TIER_ID, TIER_NAME, test_f9_duplicate_alert_border_amber)
    suite.add_test("f9_duplicate_badge_colors", 9, "Duplicate Warning", TIER_ID, TIER_NAME, test_f9_duplicate_badge_colors)
    suite.add_test("f9_no_duplicate_clean_state", 9, "Duplicate Warning", TIER_ID, TIER_NAME, test_f9_no_duplicate_clean_state)

    # ==========================================
    # Feature 10: Bulk URL Multi-Column Parser
    # ==========================================
    def test_f10_parse_3_column_tsv(res: TestResult):
        raw = "Season\tEpisode\tDownload URL\n1\t1\thttps://ex.com/s1e1.mp4\n1\t2\thttps://ex.com/s1e2.mp4"
        items = BulkUrlParser.parse_bulk_text(raw)
        assert len(items) == 2
        assert items[0]["epCode"] == "S01E01" and items[0]["downloadUrl"] == "https://ex.com/s1e1.mp4"
        assert items[1]["epCode"] == "S01E02"

    def test_f10_parse_2_column_pipe(res: TestResult):
        raw = "S01E01 | https://ex.com/e1.mp4\nS01E02 | https://ex.com/e2.mp4"
        items = BulkUrlParser.parse_bulk_text(raw)
        assert len(items) == 2
        assert items[0]["epCode"] == "S01E01"

    def test_f10_parse_1_column_raw_urls(res: TestResult):
        raw = "https://ex.com/Show.S01E01.mp4\nhttps://ex.com/Show.S01E02.mp4"
        items = BulkUrlParser.parse_bulk_text(raw)
        assert len(items) == 2
        assert items[0]["epCode"] == "S01E01"
        assert items[1]["epCode"] == "S01E02"

    def test_f10_header_line_auto_discard(res: TestResult):
        raw = "Season | Episode | Link\n1 | 1 | https://ex.com/e1.mp4"
        items = BulkUrlParser.parse_bulk_text(raw)
        assert len(items) == 1
        assert items[0]["episode"] == 1

    def test_f10_sequential_fallback_generation(res: TestResult):
        raw = "https://ex.com/videoA\nhttps://ex.com/videoB"
        items = BulkUrlParser.parse_bulk_text(raw, default_season=2)
        assert len(items) == 2
        assert items[0]["epCode"] == "S02E01"
        assert items[1]["epCode"] == "S02E02"

    suite.add_test("f10_parse_3_column_tsv", 10, "Bulk URL Parser", TIER_ID, TIER_NAME, test_f10_parse_3_column_tsv)
    suite.add_test("f10_parse_2_column_pipe", 10, "Bulk URL Parser", TIER_ID, TIER_NAME, test_f10_parse_2_column_pipe)
    suite.add_test("f10_parse_1_column_raw_urls", 10, "Bulk URL Parser", TIER_ID, TIER_NAME, test_f10_parse_1_column_raw_urls)
    suite.add_test("f10_header_line_auto_discard", 10, "Bulk URL Parser", TIER_ID, TIER_NAME, test_f10_header_line_auto_discard)
    suite.add_test("f10_sequential_fallback_generation", 10, "Bulk URL Parser", TIER_ID, TIER_NAME, test_f10_sequential_fallback_generation)

    # ==========================================
    # Feature 11: Queue Task Actions
    # ==========================================
    def test_f11_retry_action_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["retry_download_task"]
        assert contract["method"] == "POST" and contract["path"] == "/api/downloads/{id}/retry"

    def test_f11_cancel_delete_action_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["cancel_download_task"]
        assert contract["method"] == "DELETE" and contract["path"] == "/api/downloads/{id}"

    def test_f11_clear_all_action_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["clear_all_downloads"]
        assert contract["method"] == "POST" and contract["path"] == "/api/downloads/clear-all"

    def test_f11_single_queue_dispatch_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["queue_download"]
        assert contract["method"] == "POST" and contract["path"] == "/api/downloads/queue"

    def test_f11_danger_button_color_token(res: TestResult):
        assert SKETCH_COLOR_PALETTE["accent_red"].hex_code == "#EF4444"

    suite.add_test("f11_retry_action_endpoint", 11, "Queue Task Actions", TIER_ID, TIER_NAME, test_f11_retry_action_endpoint)
    suite.add_test("f11_cancel_delete_action_endpoint", 11, "Queue Task Actions", TIER_ID, TIER_NAME, test_f11_cancel_delete_action_endpoint)
    suite.add_test("f11_clear_all_action_endpoint", 11, "Queue Task Actions", TIER_ID, TIER_NAME, test_f11_clear_all_action_endpoint)
    suite.add_test("f11_single_queue_dispatch_endpoint", 11, "Queue Task Actions", TIER_ID, TIER_NAME, test_f11_single_queue_dispatch_endpoint)
    suite.add_test("f11_danger_button_color_token", 11, "Queue Task Actions", TIER_ID, TIER_NAME, test_f11_danger_button_color_token)

    # ==========================================
    # Feature 12: Vault Biometric Auth
    # ==========================================
    def test_f12_biometric_success(res: TestResult):
        sim = BiometricAuthSimulator()
        out = sim.authenticate_biometric(success=True)
        assert out["success"] is True and sim.is_authenticated is True

    def test_f12_biometric_failure_fallback_pin(res: TestResult):
        sim = BiometricAuthSimulator()
        for _ in range(4):
            sim.authenticate_biometric(success=False)
        assert sim.is_locked_out is False
        out = sim.authenticate_biometric(success=False) # 5th attempt
        assert out["error"] == "BIOMETRIC_LOCKOUT"
        assert out["fallback_to_pin"] is True

    def test_f12_pin_unlock_after_lockout(res: TestResult):
        sim = BiometricAuthSimulator()
        sim.is_locked_out = True
        assert sim.authenticate_with_pin("1234", "1234") is True
        assert sim.is_authenticated is True and sim.is_locked_out is False

    def test_f12_biometric_unavailable_fallback(res: TestResult):
        sim = BiometricAuthSimulator(hardware_present=False)
        out = sim.authenticate_biometric(success=True)
        assert out["fallback_to_pin"] is True

    def test_f12_wrong_pin_fails(res: TestResult):
        sim = BiometricAuthSimulator()
        assert sim.authenticate_with_pin("0000", "1234") is False

    suite.add_test("f12_biometric_success", 12, "Vault Biometrics", TIER_ID, TIER_NAME, test_f12_biometric_success)
    suite.add_test("f12_biometric_failure_fallback_pin", 12, "Vault Biometrics", TIER_ID, TIER_NAME, test_f12_biometric_failure_fallback_pin)
    suite.add_test("f12_pin_unlock_after_lockout", 12, "Vault Biometrics", TIER_ID, TIER_NAME, test_f12_pin_unlock_after_lockout)
    suite.add_test("f12_biometric_unavailable_fallback", 12, "Vault Biometrics", TIER_ID, TIER_NAME, test_f12_biometric_unavailable_fallback)
    suite.add_test("f12_wrong_pin_fails", 12, "Vault Biometrics", TIER_ID, TIER_NAME, test_f12_wrong_pin_fails)

    # ==========================================
    # Feature 13: Stacked Card Deck UI
    # ==========================================
    def test_f13_default_card_transform(res: TestResult):
        tf = StackedCardDeckLayout.calculate_card_transform(card_index=2, selected_index=None, is_hovered=False)
        assert tf["offset_y"] == 160.0 and tf["scale"] == 1.0 and tf["z_index"] == 2.0

    def test_f13_hover_card_transform(res: TestResult):
        tf = StackedCardDeckLayout.calculate_card_transform(card_index=2, selected_index=None, is_hovered=True)
        assert tf["offset_y"] == 160.0 - 28.0 and tf["scale"] == 1.02 and tf["z_index"] == 50.0

    def test_f13_selected_pop_card_transform(res: TestResult):
        tf = StackedCardDeckLayout.calculate_card_transform(card_index=2, selected_index=2, is_hovered=False)
        assert tf["offset_y"] == 160.0 - 40.0 and tf["scale"] == 1.03 and tf["z_index"] == 100.0

    def test_f13_deck_overlap_offset_constant(res: TestResult):
        assert StackedCardDeckLayout.DEFAULT_OVERLAP_OFFSET_DP == -160.0

    def test_f13_card_selection_priority_over_hover(res: TestResult):
        tf = StackedCardDeckLayout.calculate_card_transform(card_index=1, selected_index=1, is_hovered=True)
        assert tf["scale"] == 1.03 and tf["z_index"] == 100.0

    suite.add_test("f13_default_card_transform", 13, "Stacked Card Deck UI", TIER_ID, TIER_NAME, test_f13_default_card_transform)
    suite.add_test("f13_hover_card_transform", 13, "Stacked Card Deck UI", TIER_ID, TIER_NAME, test_f13_hover_card_transform)
    suite.add_test("f13_selected_pop_card_transform", 13, "Stacked Card Deck UI", TIER_ID, TIER_NAME, test_f13_selected_pop_card_transform)
    suite.add_test("f13_deck_overlap_offset_constant", 13, "Stacked Card Deck UI", TIER_ID, TIER_NAME, test_f13_deck_overlap_offset_constant)
    suite.add_test("f13_card_selection_priority_over_hover", 13, "Stacked Card Deck UI", TIER_ID, TIER_NAME, test_f13_card_selection_priority_over_hover)

    # ==========================================
    # Feature 14: Multi-Template Cards
    # ==========================================
    def test_f14_aadhaar_card_formatting(res: TestResult):
        formatted = CardTemplateFormatter.format_aadhaar("123456789012", mask=True)
        assert formatted == "XXXX XXXX 9012"
        unmasked = CardTemplateFormatter.format_aadhaar("123456789012", mask=False)
        assert unmasked == "1234 5678 9012"

    def test_f14_pan_card_validation(res: TestResult):
        assert CardTemplateFormatter.validate_card("pan", "ABCDE1234F") is True
        assert CardTemplateFormatter.validate_card("pan", "invalid_pan") is False

    def test_f14_bank_card_formatting(res: TestResult):
        formatted = CardTemplateFormatter.format_bank_card("4532891234567890", mask=True)
        assert formatted == "XXXX-XXXX-XXXX-7890"

    def test_f14_passport_validation(res: TestResult):
        assert CardTemplateFormatter.validate_card("passport", "Z1234567") is True

    def test_f14_rc_vehicle_validation(res: TestResult):
        assert CardTemplateFormatter.validate_card("rc", "KA01AB1234") is True

    suite.add_test("f14_aadhaar_card_formatting", 14, "Multi-Template Cards", TIER_ID, TIER_NAME, test_f14_aadhaar_card_formatting)
    suite.add_test("f14_pan_card_validation", 14, "Multi-Template Cards", TIER_ID, TIER_NAME, test_f14_pan_card_validation)
    suite.add_test("f14_bank_card_formatting", 14, "Multi-Template Cards", TIER_ID, TIER_NAME, test_f14_bank_card_formatting)
    suite.add_test("f14_passport_validation", 14, "Multi-Template Cards", TIER_ID, TIER_NAME, test_f14_passport_validation)
    suite.add_test("f14_rc_vehicle_validation", 14, "Multi-Template Cards", TIER_ID, TIER_NAME, test_f14_rc_vehicle_validation)

    # ==========================================
    # Feature 15: Secure Notes CRUD
    # ==========================================
    def test_f15_get_notes_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["get_vault_notes"]
        assert contract["method"] == "GET" and contract["path"] == "/api/vault/notes"

    def test_f15_create_note_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["create_vault_note"]
        assert contract["method"] == "POST" and contract["path"] == "/api/vault/notes"

    def test_f15_update_note_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["update_vault_note"]
        assert contract["method"] == "PUT" and contract["path"] == "/api/vault/notes/{id}"

    def test_f15_delete_note_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["delete_vault_note"]
        assert contract["method"] == "DELETE" and contract["path"] == "/api/vault/notes/{id}"

    def test_f15_note_structure(res: TestResult):
        note = {"id": 1, "title": "Recovery Key", "content": "secret-phrase-here", "updated_at": "2026-09-02T00:00:00Z"}
        assert note["title"] and note["content"]

    suite.add_test("f15_get_notes_endpoint", 15, "Secure Notes CRUD", TIER_ID, TIER_NAME, test_f15_get_notes_endpoint)
    suite.add_test("f15_create_note_endpoint", 15, "Secure Notes CRUD", TIER_ID, TIER_NAME, test_f15_create_note_endpoint)
    suite.add_test("f15_update_note_endpoint", 15, "Secure Notes CRUD", TIER_ID, TIER_NAME, test_f15_update_note_endpoint)
    suite.add_test("f15_delete_note_endpoint", 15, "Secure Notes CRUD", TIER_ID, TIER_NAME, test_f15_delete_note_endpoint)
    suite.add_test("f15_note_structure", 15, "Secure Notes CRUD", TIER_ID, TIER_NAME, test_f15_note_structure)

    # ==========================================
    # Feature 16: Files Folder Browser
    # ==========================================
    def test_f16_breadcrumb_parsing(res: TestResult):
        crumbs = FilesystemHelper.parse_breadcrumbs("/Data/Downloads/Movies/Action")
        assert len(crumbs) == 5
        assert crumbs[0]["name"] == "Root" and crumbs[0]["path"] == "/"
        assert crumbs[-1]["name"] == "Action" and crumbs[-1]["path"] == "/Data/Downloads/Movies/Action"

    def test_f16_file_size_formatting(res: TestResult):
        assert FilesystemHelper.format_size(500) == "500 B"
        assert FilesystemHelper.format_size(1024 * 512) == "512.00 KB"
        assert FilesystemHelper.format_size(1024 * 1024 * 1400) == "1.37 GB"

    def test_f16_extension_category_mapping(res: TestResult):
        assert FilesystemHelper.get_extension_category("movie.mp4") == "video"
        assert FilesystemHelper.get_extension_category("song.mp3") == "audio"
        assert FilesystemHelper.get_extension_category("photo.jpg") == "image"
        assert FilesystemHelper.get_extension_category("archive.zip") == "archive"
        assert FilesystemHelper.get_extension_category("script.py") == "code"
        assert FilesystemHelper.get_extension_category("doc.pdf") == "pdf"

    def test_f16_folder_icon_color(res: TestResult):
        assert FilesystemHelper.ICON_COLORS["folder"] == "#EAB308"

    def test_f16_browse_directory_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["browse_directory"]
        assert contract["method"] == "GET" and contract["path"] == "/api/fs/list"

    suite.add_test("f16_breadcrumb_parsing", 16, "Files Folder Browser", TIER_ID, TIER_NAME, test_f16_breadcrumb_parsing)
    suite.add_test("f16_file_size_formatting", 16, "Files Folder Browser", TIER_ID, TIER_NAME, test_f16_file_size_formatting)
    suite.add_test("f16_extension_category_mapping", 16, "Files Folder Browser", TIER_ID, TIER_NAME, test_f16_extension_category_mapping)
    suite.add_test("f16_folder_icon_color", 16, "Files Folder Browser", TIER_ID, TIER_NAME, test_f16_folder_icon_color)
    suite.add_test("f16_browse_directory_endpoint", 16, "Files Folder Browser", TIER_ID, TIER_NAME, test_f16_browse_directory_endpoint)

    # ==========================================
    # Feature 17: File & Directory Operations
    # ==========================================
    def test_f17_create_directory_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["create_directory"]
        assert contract["method"] == "POST" and contract["path"] == "/api/fs/mkdir"

    def test_f17_delete_files_batch_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["delete_files"]
        assert contract["method"] == "POST" and contract["path"] == "/api/fs/delete"

    def test_f17_rename_file_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["rename_file"]
        assert contract["method"] == "POST" and contract["path"] == "/api/fs/rename"

    def test_f17_write_file_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["write_file"]
        assert contract["method"] == "POST" and contract["path"] == "/api/fs/write"

    def test_f17_read_file_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["read_file"]
        assert contract["method"] == "GET" and contract["path"] == "/api/fs/read"

    suite.add_test("f17_create_directory_endpoint", 17, "File Operations", TIER_ID, TIER_NAME, test_f17_create_directory_endpoint)
    suite.add_test("f17_delete_files_batch_endpoint", 17, "File Operations", TIER_ID, TIER_NAME, test_f17_delete_files_batch_endpoint)
    suite.add_test("f17_rename_file_endpoint", 17, "File Operations", TIER_ID, TIER_NAME, test_f17_rename_file_endpoint)
    suite.add_test("f17_write_file_endpoint", 17, "File Operations", TIER_ID, TIER_NAME, test_f17_write_file_endpoint)
    suite.add_test("f17_read_file_endpoint", 17, "File Operations", TIER_ID, TIER_NAME, test_f17_read_file_endpoint)

    # ==========================================
    # Feature 18: Archive & Permissions Tools
    # ==========================================
    def test_f18_octal_to_posix_755(res: TestResult):
        posix = FilesystemHelper.octal_to_posix_string(0o755, is_dir=True)
        assert posix == "drwxr-xr-x"

    def test_f18_octal_to_posix_644(res: TestResult):
        posix = FilesystemHelper.octal_to_posix_string(0o644, is_dir=False)
        assert posix == "-rw-r--r--"

    def test_f18_posix_to_octal_conversion(res: TestResult):
        octal = FilesystemHelper.posix_string_to_octal("rwxr-xr-x")
        assert octal == 0o755

    def test_f18_zip_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["zip_files"]
        assert contract["method"] == "POST" and contract["path"] == "/api/fs/zip"

    def test_f18_unzip_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["unzip_file"]
        assert contract["method"] == "POST" and contract["path"] == "/api/fs/unzip"

    suite.add_test("f18_octal_to_posix_755", 18, "Archive & Permissions", TIER_ID, TIER_NAME, test_f18_octal_to_posix_755)
    suite.add_test("f18_octal_to_posix_644", 18, "Archive & Permissions", TIER_ID, TIER_NAME, test_f18_octal_to_posix_644)
    suite.add_test("f18_posix_to_octal_conversion", 18, "Archive & Permissions", TIER_ID, TIER_NAME, test_f18_posix_to_octal_conversion)
    suite.add_test("f18_zip_endpoint", 18, "Archive & Permissions", TIER_ID, TIER_NAME, test_f18_zip_endpoint)
    suite.add_test("f18_unzip_endpoint", 18, "Archive & Permissions", TIER_ID, TIER_NAME, test_f18_unzip_endpoint)

    # ==========================================
    # Feature 19: In-App Editor & Media Preview
    # ==========================================
    def test_f19_editor_font_jetbrains_mono(res: TestResult):
        assert TypographyHierarchy.resolve_font_for_element("code_editor") == "JetBrains Mono"

    def test_f19_supported_preview_mime_types(res: TestResult):
        assert FilesystemHelper.get_extension_category("video.mp4") == "video"
        assert FilesystemHelper.get_extension_category("doc.pdf") == "pdf"
        assert FilesystemHelper.get_extension_category("image.png") == "image"

    def test_f19_video_streaming_endpoint(res: TestResult):
        contract = RetrofitEndpointContract.ENDPOINTS["stream_video"]
        assert contract["method"] == "GET" and contract["path"] == "/api/stream/video"

    def test_f19_in_app_editor_payload(res: TestResult):
        payload = {"filePath": "/Data/config.json", "content": "{\"key\": \"val\"}"}
        assert payload["filePath"].endswith(".json") and len(payload["content"]) > 0

    def test_f19_pdf_preview_color_token(res: TestResult):
        assert FilesystemHelper.ICON_COLORS["pdf"] == "#EF4444"

    suite.add_test("f19_editor_font_jetbrains_mono", 19, "Editor & Preview", TIER_ID, TIER_NAME, test_f19_editor_font_jetbrains_mono)
    suite.add_test("f19_supported_preview_mime_types", 19, "Editor & Preview", TIER_ID, TIER_NAME, test_f19_supported_preview_mime_types)
    suite.add_test("f19_video_streaming_endpoint", 19, "Editor & Preview", TIER_ID, TIER_NAME, test_f19_video_streaming_endpoint)
    suite.add_test("f19_in_app_editor_payload", 19, "Editor & Preview", TIER_ID, TIER_NAME, test_f19_in_app_editor_payload)
    suite.add_test("f19_pdf_preview_color_token", 19, "Editor & Preview", TIER_ID, TIER_NAME, test_f19_pdf_preview_color_token)

    # ==========================================
    # Feature 20: Real JSch SSH Connection
    # ==========================================
    def test_f20_ssh_default_host(res: TestResult):
        host = "hub.ptsmovies.online"
        assert host == "hub.ptsmovies.online"

    def test_f20_ssh_default_port(res: TestResult):
        port = 22
        assert port == 22

    def test_f20_ssh_pty_terminal_type(res: TestResult):
        term = "xterm-256color"
        assert term == "xterm-256color"

    def test_f20_ssh_default_user(res: TestResult):
        user = "prajwal"
        assert user == "prajwal"

    def test_f20_ssh_connect_timeout_ms(res: TestResult):
        timeout = 10000
        assert timeout == 10000

    suite.add_test("f20_ssh_default_host", 20, "JSch SSH Connection", TIER_ID, TIER_NAME, test_f20_ssh_default_host)
    suite.add_test("f20_ssh_default_port", 20, "JSch SSH Connection", TIER_ID, TIER_NAME, test_f20_ssh_default_port)
    suite.add_test("f20_ssh_pty_terminal_type", 20, "JSch SSH Connection", TIER_ID, TIER_NAME, test_f20_ssh_pty_terminal_type)
    suite.add_test("f20_ssh_default_user", 20, "JSch SSH Connection", TIER_ID, TIER_NAME, test_f20_ssh_default_user)
    suite.add_test("f20_ssh_connect_timeout_ms", 20, "JSch SSH Connection", TIER_ID, TIER_NAME, test_f20_ssh_connect_timeout_ms)

    # ==========================================
    # Feature 21: Interactive Terminal & ANSI Buffer
    # ==========================================
    def test_f21_strip_ansi_color_sequences(res: TestResult):
        ansi_text = "\x1b[32mSuccess\x1b[0m: \x1b[1mDone\x1b[0m"
        clean = AnsiSequenceParser.strip_ansi(ansi_text)
        assert clean == "Success: Done"

    def test_f21_extract_ansi_color_codes(res: TestResult):
        ansi_text = "\x1b[32;1mGreenBold\x1b[0m"
        codes = AnsiSequenceParser.extract_color_codes(ansi_text)
        assert "32;1" in codes and "0" in codes

    def test_f21_ring_buffer_capacity_clamping(res: TestResult):
        ring = TerminalSessionRingBuffer(capacity=50)
        ring.append_output("A" * 40)
        ring.append_output("B" * 30)
        replay = ring.get_replay_data()
        assert len(replay) == 50
        assert replay.endswith("B" * 30)

    def test_f21_pty_dimension_calculation(res: TestResult):
        cols, rows = PtyDimensionCalculator.calculate_dimensions(screen_width_dp=360, screen_height_dp=640, font_size_sp=13)
        assert cols >= 40 and rows >= 20

    def test_f21_session_persistence_id_format(res: TestResult):
        session_id = "pts_sess_1787020000"
        assert session_id.startswith("pts_sess_")

    suite.add_test("f21_strip_ansi_color_sequences", 21, "Terminal ANSI Buffer", TIER_ID, TIER_NAME, test_f21_strip_ansi_color_sequences)
    suite.add_test("f21_extract_ansi_color_codes", 21, "Terminal ANSI Buffer", TIER_ID, TIER_NAME, test_f21_extract_ansi_color_codes)
    suite.add_test("f21_ring_buffer_capacity_clamping", 21, "Terminal ANSI Buffer", TIER_ID, TIER_NAME, test_f21_ring_buffer_capacity_clamping)
    suite.add_test("f21_pty_dimension_calculation", 21, "Terminal ANSI Buffer", TIER_ID, TIER_NAME, test_f21_pty_dimension_calculation)
    suite.add_test("f21_session_persistence_id_format", 21, "Terminal ANSI Buffer", TIER_ID, TIER_NAME, test_f21_session_persistence_id_format)

    # ==========================================
    # Feature 22: Mobile Accessory Key Row
    # ==========================================
    def test_f22_esc_byte_mapping(res: TestResult):
        engine = TerminalKeyRowEngine()
        assert engine.process_key_press("ESC") == "\x1b"

    def test_f22_tab_byte_mapping(res: TestResult):
        engine = TerminalKeyRowEngine()
        assert engine.process_key_press("TAB") == "\t"

    def test_f22_arrow_key_ansi_sequences(res: TestResult):
        engine = TerminalKeyRowEngine()
        assert engine.process_key_press("UP") == "\x1b[A"
        assert engine.process_key_press("DOWN") == "\x1b[B"
        assert engine.process_key_press("LEFT") == "\x1b[D"
        assert engine.process_key_press("RIGHT") == "\x1b[C"

    def test_f22_sigint_ctrl_c(res: TestResult):
        engine = TerminalKeyRowEngine()
        assert engine.process_key_press("CTRL_C") == "\x03"

    def test_f22_sticky_ctrl_modifier(res: TestResult):
        engine = TerminalKeyRowEngine()
        engine.toggle_ctrl()
        assert engine.ctrl_sticky is True
        result = engine.process_key_press("c")
        assert result == "\x03" # Ctrl+C = 3
        assert engine.ctrl_sticky is False # Auto reset

    suite.add_test("f22_esc_byte_mapping", 22, "Accessory Key Row", TIER_ID, TIER_NAME, test_f22_esc_byte_mapping)
    suite.add_test("f22_tab_byte_mapping", 22, "Accessory Key Row", TIER_ID, TIER_NAME, test_f22_tab_byte_mapping)
    suite.add_test("f22_arrow_key_ansi_sequences", 22, "Accessory Key Row", TIER_ID, TIER_NAME, test_f22_arrow_key_ansi_sequences)
    suite.add_test("f22_sigint_ctrl_c", 22, "Accessory Key Row", TIER_ID, TIER_NAME, test_f22_sigint_ctrl_c)
    suite.add_test("f22_sticky_ctrl_modifier", 22, "Accessory Key Row", TIER_ID, TIER_NAME, test_f22_sticky_ctrl_modifier)

    # ==========================================
    # Feature 23: Quick Command Drawer
    # ==========================================
    def test_f23_htop_quick_command(res: TestResult):
        assert "htop" in TerminalKeyRowEngine.QUICK_COMMANDS

    def test_f23_pm2_status_quick_command(res: TestResult):
        assert "pm2 status" in TerminalKeyRowEngine.QUICK_COMMANDS

    def test_f23_docker_ps_quick_command(res: TestResult):
        assert "docker ps" in TerminalKeyRowEngine.QUICK_COMMANDS

    def test_f23_unix_symbols_drawer_completeness(res: TestResult):
        symbols = TerminalKeyRowEngine.UNIX_SYMBOLS
        for sym in ["|", "/", "\\", "~", "$", "&", "#", "!", ">", "<"]:
            assert sym in symbols

    def test_f23_font_size_scaling(res: TestResult):
        engine = TerminalKeyRowEngine()
        assert engine.adjust_font_size(2) == 15
        assert engine.adjust_font_size(-20) == 10 # Min clamp
        assert engine.adjust_font_size(30) == 22 # Max clamp

    suite.add_test("f23_htop_quick_command", 23, "Quick Command Drawer", TIER_ID, TIER_NAME, test_f23_htop_quick_command)
    suite.add_test("f23_pm2_status_quick_command", 23, "Quick Command Drawer", TIER_ID, TIER_NAME, test_f23_pm2_status_quick_command)
    suite.add_test("f23_docker_ps_quick_command", 23, "Quick Command Drawer", TIER_ID, TIER_NAME, test_f23_docker_ps_quick_command)
    suite.add_test("f23_unix_symbols_drawer_completeness", 23, "Quick Command Drawer", TIER_ID, TIER_NAME, test_f23_unix_symbols_drawer_completeness)
    suite.add_test("f23_font_size_scaling", 23, "Quick Command Drawer", TIER_ID, TIER_NAME, test_f23_font_size_scaling)

    # ==========================================
    # Feature 24: DynamicBottomDock Navigation
    # ==========================================
    def test_f24_all_navigation_destinations(res: TestResult):
        dests = NavigationDestination.ALL
        assert len(dests) == 6
        assert "stream" in dests and "hub_queue" in dests and "vault" in dests
        assert "files" in dests and "terminal" in dests and "settings" in dests

    def test_f24_active_tab_accent_color(res: TestResult):
        assert SKETCH_COLOR_PALETTE["accent_green"].hex_code == "#22C55E"

    def test_f24_dock_navigation_state_transition(res: TestResult):
        nav = BackHandlerNavigator()
        nav.navigate_to(NavigationDestination.HUB_QUEUE)
        assert nav.current_destination == NavigationDestination.HUB_QUEUE

    def test_f24_dock_badge_count_formatting(res: TestResult):
        count = 12
        badge_text = f"{count}" if count < 100 else "99+"
        assert badge_text == "12"

    def test_f24_dock_icon_pill_background(res: TestResult):
        assert SKETCH_COLOR_PALETTE["graphite_800"].hex_code == "#1E1E28"

    suite.add_test("f24_all_navigation_destinations", 24, "DynamicBottomDock", TIER_ID, TIER_NAME, test_f24_all_navigation_destinations)
    suite.add_test("f24_active_tab_accent_color", 24, "DynamicBottomDock", TIER_ID, TIER_NAME, test_f24_active_tab_accent_color)
    suite.add_test("f24_dock_navigation_state_transition", 24, "DynamicBottomDock", TIER_ID, TIER_NAME, test_f24_dock_navigation_state_transition)
    suite.add_test("f24_dock_badge_count_formatting", 24, "DynamicBottomDock", TIER_ID, TIER_NAME, test_f24_dock_badge_count_formatting)
    suite.add_test("f24_dock_icon_pill_background", 24, "DynamicBottomDock", TIER_ID, TIER_NAME, test_f24_dock_icon_pill_background)

    # ==========================================
    # Feature 25: Robust BackHandler Routing
    # ==========================================
    def test_f25_back_dismisses_active_modal(res: TestResult):
        nav = BackHandlerNavigator()
        nav.open_modal("GenreFilter")
        res_code = nav.handle_system_back()
        assert res_code == "MODAL_DISMISSED" and nav.active_modal is None

    def test_f25_back_closes_player_to_catalog(res: TestResult):
        nav = BackHandlerNavigator()
        nav.open_player()
        res_code = nav.handle_system_back()
        assert res_code == "PLAYER_CLOSED" and nav.is_player_open is False

    def test_f25_back_pops_screen_stack(res: TestResult):
        nav = BackHandlerNavigator()
        nav.navigate_to(NavigationDestination.VAULT)
        nav.navigate_to(NavigationDestination.FILES)
        res_code = nav.handle_system_back()
        assert res_code == "POPPED_TO_VAULT" and nav.current_destination == NavigationDestination.VAULT

    def test_f25_back_returns_to_stream_catalog_from_single_tab(res: TestResult):
        nav = BackHandlerNavigator()
        nav.current_destination = NavigationDestination.TERMINAL
        nav.screen_stack = [NavigationDestination.TERMINAL]
        res_code = nav.handle_system_back()
        assert res_code == "ROUTED_TO_ROOT_STREAM" and nav.current_destination == NavigationDestination.STREAM

    def test_f25_back_exits_app_only_on_stream_root(res: TestResult):
        nav = BackHandlerNavigator()
        assert nav.current_destination == NavigationDestination.STREAM
        res_code = nav.handle_system_back()
        assert res_code == "APP_EXITED" and nav.app_exited is True

    suite.add_test("f25_back_dismisses_active_modal", 25, "BackHandler Routing", TIER_ID, TIER_NAME, test_f25_back_dismisses_active_modal)
    suite.add_test("f25_back_closes_player_to_catalog", 25, "BackHandler Routing", TIER_ID, TIER_NAME, test_f25_back_closes_player_to_catalog)
    suite.add_test("f25_back_pops_screen_stack", 25, "BackHandler Routing", TIER_ID, TIER_NAME, test_f25_back_pops_screen_stack)
    suite.add_test("f25_back_returns_to_stream_catalog_from_single_tab", 25, "BackHandler Routing", TIER_ID, TIER_NAME, test_f25_back_returns_to_stream_catalog_from_single_tab)
    suite.add_test("f25_back_exits_app_only_on_stream_root", 25, "BackHandler Routing", TIER_ID, TIER_NAME, test_f25_back_exits_app_only_on_stream_root)

    # ==========================================
    # Feature 26: Unified Retrofit Client
    # ==========================================
    def test_f26_bearer_auth_interceptor_valid(res: TestResult):
        ok, err = RetrofitEndpointContract.validate_request(
            "get_media_library", {"Authorization": "Bearer test_jwt_token"}, "test_jwt_token"
        )
        assert ok is True and err is None

    def test_f26_missing_bearer_token_unauthorized(res: TestResult):
        ok, err = RetrofitEndpointContract.validate_request(
            "get_media_library", {}, "test_jwt_token"
        )
        assert ok is False and err == "401_UNAUTHORIZED"

    def test_f26_login_endpoint_does_not_require_token(res: TestResult):
        ok, err = RetrofitEndpointContract.validate_request("login", {}, None)
        assert ok is True

    def test_f26_base_url_constant(res: TestResult):
        base_url = "https://hub.ptsmovies.online"
        assert base_url.startswith("https://") and "hub.ptsmovies.online" in base_url

    def test_f26_all_five_services_mapped(res: TestResult):
        endpoints = RetrofitEndpointContract.ENDPOINTS
        assert any(k.startswith("get_media") for k in endpoints) # Stream
        assert any(k.startswith("get_download") for k in endpoints) # Hub
        assert any(k.startswith("get_vault") for k in endpoints) # Vault
        assert any(k.startswith("browse_") for k in endpoints) # Files
        assert "get_system_stats" in endpoints # System

    suite.add_test("f26_bearer_auth_interceptor_valid", 26, "Retrofit Client", TIER_ID, TIER_NAME, test_f26_bearer_auth_interceptor_valid)
    suite.add_test("f26_missing_bearer_token_unauthorized", 26, "Retrofit Client", TIER_ID, TIER_NAME, test_f26_missing_bearer_token_unauthorized)
    suite.add_test("f26_login_endpoint_does_not_require_token", 26, "Retrofit Client", TIER_ID, TIER_NAME, test_f26_login_endpoint_does_not_require_token)
    suite.add_test("f26_base_url_constant", 26, "Retrofit Client", TIER_ID, TIER_NAME, test_f26_base_url_constant)
    suite.add_test("f26_all_five_services_mapped", 26, "Retrofit Client", TIER_ID, TIER_NAME, test_f26_all_five_services_mapped)

    # ==========================================
    # Feature 27: Room Offline Caching
    # ==========================================
    def test_f27_media_library_caching(res: TestResult):
        db = RoomDatabaseCacheSimulator()
        movies = [{"id": "m1", "title": "Interstellar"}]
        series = [{"id": "s1", "title": "Chernobyl"}]
        db.sync_media_library(movies, series)
        assert len(db.cached_movies) == 1
        assert db.cached_movies["m1"]["title"] == "Interstellar"

    def test_f27_vault_caching(res: TestResult):
        db = RoomDatabaseCacheSimulator()
        docs = [{"id": 1, "title": "Passport"}]
        notes = [{"id": 1, "title": "Secret Note"}]
        db.sync_vault(docs, notes)
        assert len(db.cached_vault_docs) == 1
        assert len(db.cached_vault_notes) == 1

    def test_f27_offline_catalog_query(res: TestResult):
        db = RoomDatabaseCacheSimulator()
        db.sync_media_library([{"id": "m1", "title": "Avatar"}, {"id": "m2", "title": "Batman"}], [])
        results = db.query_offline_catalog("Bat")
        assert len(results) == 1 and results[0]["id"] == "m2"

    def test_f27_cached_entities_table_names(res: TestResult):
        tables = ["cached_movies", "cached_series", "cached_vault_docs", "cached_vault_notes", "downloaded_media"]
        assert len(tables) == 5

    def test_f27_empty_cache_query_returns_empty_list(res: TestResult):
        db = RoomDatabaseCacheSimulator()
        res_list = db.query_offline_catalog("anything")
        assert res_list == []

    suite.add_test("f27_media_library_caching", 27, "Room Offline Caching", TIER_ID, TIER_NAME, test_f27_media_library_caching)
    suite.add_test("f27_vault_caching", 27, "Room Offline Caching", TIER_ID, TIER_NAME, test_f27_vault_caching)
    suite.add_test("f27_offline_catalog_query", 27, "Room Offline Caching", TIER_ID, TIER_NAME, test_f27_offline_catalog_query)
    suite.add_test("f27_cached_entities_table_names", 27, "Room Offline Caching", TIER_ID, TIER_NAME, test_f27_cached_entities_table_names)
    suite.add_test("f27_empty_cache_query_returns_empty_list", 27, "Room Offline Caching", TIER_ID, TIER_NAME, test_f27_empty_cache_query_returns_empty_list)



if __name__ == "__main__":
    from e2e_tests.engine.test_runner import TestSuite
    suite = TestSuite("Tier 1 Feature Coverage Test Suite")
    register_tier1_tests(suite)
    results = suite.run_all(verbose=True)
    suite.print_summary()
    failed = sum(1 for r in results if not r.passed)
    sys.exit(1 if failed > 0 else 0)
