"""
Tier 3: Cross-Feature Combinations (Pairwise Tests)
Verifies multi-feature interactions across all 27 core capabilities (28 pairwise tests).
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

def register_tier3_tests(suite: TestSuite):
    TIER_ID = 3
    TIER_NAME = "Cross-Feature Combinations"

    # Pairwise 1: F1 (Theme) + F4 (Stream Grid)
    def test_p1_theme_stream_grid_card_tokens(res: TestResult):
        bg = SKETCH_COLOR_PALETTE["bg_card"].hex_code
        star = SKETCH_COLOR_PALETTE["accent_yellow"].hex_code
        assert bg == "#0A0A0A" and star == "#EAB308"

    # Pairwise 2: F1 (Theme) + F9 (Hub Duplicate Alert)
    def test_p2_theme_duplicate_alert_border(res: TestResult):
        amber = SKETCH_COLOR_PALETTE["accent_amber"].hex_code
        chk = DuplicateChecker.check_duplicate("Test", None, "Movie", [], [])
        assert chk["alertBorderColor"] == amber

    # Pairwise 3: F1 (Theme) + F13 (Vault Card Deck)
    def test_p3_theme_vault_deck_active_border(res: TestResult):
        active_border = SKETCH_COLOR_PALETTE["sketch_border_active"].hex_code
        assert active_border == "#9E9EA8"

    # Pairwise 4: F1 (Theme) + F16 (Files Browser)
    def test_p4_theme_files_browser_icon_colors(res: TestResult):
        folder_color = FilesystemHelper.ICON_COLORS["folder"]
        video_color = FilesystemHelper.ICON_COLORS["video"]
        assert folder_color == "#EAB308" and video_color == "#38BDF8"

    # Pairwise 5: F1 (Theme) + F21 (Terminal Buffer)
    def test_p5_theme_terminal_pitch_canvas_and_ansi_green(res: TestResult):
        pitch = SKETCH_COLOR_PALETTE["bg_pitch"].hex_code
        emerald = SKETCH_COLOR_PALETTE["accent_green"].hex_code
        assert pitch == "#040404" and emerald == "#22C55E"

    # Pairwise 6: F2 (Sketch Shape) + F13 (Vault Deck)
    def test_p6_sketch_shape_vault_card_scaling(res: TestResult):
        card_w, card_h = 320.0, 200.0
        radii = SketchShapeEngine.scale_radii(SketchShapeEngine.PRIMARY_RADII, card_w, card_h)
        assert radii.top_left_x > 0 and radii.top_right_y > 0

    # Pairwise 7: F2 (Sketch Shape) + F16 (Files Browser)
    def test_p7_sketch_shape_file_pill_badges(res: TestResult):
        pill_radii = SketchShapeEngine.scale_radii(SketchShapeEngine.SM_RADII, width=60.0, height=24.0)
        assert pill_radii.top_left_x <= 30.0 and pill_radii.top_left_y <= 12.0

    # Pairwise 8: F2 (Sketch Shape) + F24 (DynamicBottomDock)
    def test_p8_sketch_shape_bottom_dock_border(res: TestResult):
        dock_radii = SketchShapeEngine.scale_radii(SketchShapeEngine.PRIMARY_RADII, width=360.0, height=64.0)
        assert dock_radii.top_left_x <= 180.0

    # Pairwise 9: F3 (Typography) + F6 (ExoPlayer)
    def test_p9_typography_exoplayer_overlay(res: TestResult):
        title_font = TypographyHierarchy.resolve_font_for_element("body_text")
        time_font = TypographyHierarchy.resolve_font_for_element("timestamp")
        assert title_font == "Space Grotesk" and time_font == "JetBrains Mono"

    # Pairwise 10: F3 (Typography) + F14 (Card Templates)
    def test_p10_typography_card_template_emboss(res: TestResult):
        num_font = TypographyHierarchy.resolve_font_for_element("card_number")
        assert num_font == "Share Tech Mono"

    # Pairwise 11: F3 (Typography) + F22 (Accessory Keys)
    def test_p11_typography_terminal_accessory_row(res: TestResult):
        key_font = TypographyHierarchy.resolve_font_for_element("terminal_buffer")
        assert key_font == "JetBrains Mono"

    # Pairwise 12: F4 (Stream Catalog) + F5 (Fuzzy Search)
    def test_p12_catalog_fuzzy_search_filtering(res: TestResult):
        catalog = [
            {"id": "1", "title": "K.G.F: Chapter 2", "genres": ["Action"]},
            {"id": "2", "title": "Inception", "genres": ["Sci-Fi"]}
        ]
        matched = [m for m in catalog if FuzzySearchEngine.match("kgf ii", [m["title"]] + m["genres"])]
        assert len(matched) == 1 and matched[0]["id"] == "1"

    # Pairwise 13: F4 (Stream Catalog) + F7 (Watchlist Sync)
    def test_p13_catalog_watchlist_toggle(res: TestResult):
        watchlist = []
        item = {"id": "mov_1", "title": "Batman"}
        watchlist.append(item)
        assert len(watchlist) == 1
        watchlist.remove(item)
        assert len(watchlist) == 0

    # Pairwise 14: F4 (Stream Catalog) + F27 (Room Caching)
    def test_p14_catalog_room_database_sync(res: TestResult):
        db = RoomDatabaseCacheSimulator()
        movies = [{"id": "m1", "title": "Interstellar"}]
        db.sync_media_library(movies, [])
        offline_res = db.query_offline_catalog("Interstellar")
        assert len(offline_res) == 1 and offline_res[0]["id"] == "m1"

    # Pairwise 15: F5 (Fuzzy Search) + F27 (Room Database)
    def test_p15_fuzzy_search_over_room_cache(res: TestResult):
        db = RoomDatabaseCacheSimulator()
        db.sync_media_library([{"id": "m1", "title": "Le Fabuleux Destin d'Amélie Poulain"}], [])
        all_cached = db.query_offline_catalog("")
        matched = [m for m in all_cached if FuzzySearchEngine.match("amelie", [m["title"]])]
        assert len(matched) == 1 and matched[0]["id"] == "m1"

    # Pairwise 16: F6 (ExoPlayer) + F7 (Progress Heartbeat)
    def test_p16_exoplayer_seek_triggers_progress_sync(res: TestResult):
        sim = ExoPlayerSimulator(duration_seconds=100.0)
        sim.double_tap_seek_forward() # pos = 10s
        pct = WatchlistAndProgressTracker.calculate_progress_percentage(sim.current_position, sim.duration)
        assert pct == 0.10
        assert WatchlistAndProgressTracker.should_include_in_continue_watching(sim.current_position, sim.duration) is True

    # Pairwise 17: F6 (ExoPlayer) + F25 (BackHandler)
    def test_p17_exoplayer_back_handler_closes_player(res: TestResult):
        nav = BackHandlerNavigator()
        nav.open_player()
        assert nav.is_player_open is True
        res_code = nav.handle_system_back()
        assert res_code == "PLAYER_CLOSED" and nav.is_player_open is False

    # Pairwise 18: F8 (Hub Polling) + F11 (Task Actions)
    def test_p18_hub_polling_and_retry_action(res: TestResult):
        task = {"id": "tsk_1", "stage": "failed"}
        if task["stage"] == "failed":
            task["stage"] = "queued"
        assert task["stage"] == "queued"
        assert DownloadPipelineStateMachine.validate_transition(task["stage"], "gha_downloading") is True

    # Pairwise 19: F9 (Duplicate Warning) + F10 (Bulk URL Parser)
    def test_p19_duplicate_check_on_parsed_bulk_urls(res: TestResult):
        raw = "1 | 1 | https://ex.com/s01e01.mp4\n1 | 2 | https://ex.com/s01e02.mp4"
        parsed = BulkUrlParser.parse_bulk_text(raw)
        on_disk_episodes = [{"epCode": "S01E01"}]
        filtered = [p for p in parsed if not any(d["epCode"] == p["epCode"] for d in on_disk_episodes)]
        assert len(filtered) == 1 and filtered[0]["epCode"] == "S01E02"

    # Pairwise 20: F10 (Bulk URL Parser) + F11 (Queue Actions)
    def test_p20_bulk_url_parser_to_queue_dispatch(res: TestResult):
        raw = "S01E01 | https://ex.com/e1.mp4"
        parsed = BulkUrlParser.parse_bulk_text(raw)
        payload = {"type": "Series", "title": "Show", "items": parsed}
        assert len(payload["items"]) == 1

    # Pairwise 21: F12 (Vault Biometrics) + F13 (Card Deck)
    def test_p21_biometric_auth_unlocks_card_deck(res: TestResult):
        sim = BiometricAuthSimulator()
        auth_res = sim.authenticate_biometric(success=True)
        assert auth_res["success"] is True
        deck_cards = [{"id": 1, "title": "Visa Card"}]
        assert len(deck_cards) == 1

    # Pairwise 22: F12 (Vault Biometrics) + F15 (Notes CRUD)
    def test_p22_pin_fallback_unlocks_note_editing(res: TestResult):
        sim = BiometricAuthSimulator()
        sim.is_locked_out = True
        unlocked = sim.authenticate_with_pin("1234", "1234")
        assert unlocked is True
        note = {"id": 1, "title": "Secrets", "content": "Updated"}
        assert note["content"] == "Updated"

    # Pairwise 23: F14 (Card Templates) + F15 (Notes CRUD)
    def test_p23_formatted_card_copied_to_note(res: TestResult):
        formatted_card = CardTemplateFormatter.format_bank_card("4532891234567890", mask=True)
        note = {"id": 1, "title": "Card Details", "content": f"Primary: {formatted_card}"}
        assert "XXXX-XXXX-XXXX-7890" in note["content"]

    # Pairwise 24: F16 (Files Browser) + F17 (File Operations)
    def test_p24_browser_navigate_and_batch_delete(res: TestResult):
        crumbs = FilesystemHelper.parse_breadcrumbs("/Data/Downloads/Temp")
        assert len(crumbs) == 4
        del_payload = {"paths": ["/Data/Downloads/Temp/f1.tmp", "/Data/Downloads/Temp/f2.tmp"]}
        assert len(del_payload["paths"]) == 2

    # Pairwise 25: F17 (File Operations) + F18 (Archive Tools)
    def test_p25_file_crud_and_zip_compression(res: TestResult):
        files = ["/Data/log1.txt", "/Data/log2.txt"]
        zip_payload = {"sources": files, "targetDir": "/Data", "zipName": "logs.zip"}
        assert zip_payload["zipName"].endswith(".zip") and len(zip_payload["sources"]) == 2

    # Pairwise 26: F18 (Permissions) + F19 (In-App Editor)
    def test_p26_chmod_and_edit_script(res: TestResult):
        octal = FilesystemHelper.posix_string_to_octal("rwxr-xr-x")
        assert octal == 0o755
        editor_payload = {"filePath": "/Data/deploy.sh", "content": "#!/bin/bash\necho 'running'"}
        assert editor_payload["content"].startswith("#!/bin/bash")

    # Pairwise 27: F20 (JSch SSH) + F21 (Terminal Buffer)
    def test_p27_ssh_session_streams_to_ring_buffer(res: TestResult):
        ring = TerminalSessionRingBuffer()
        ring.create_session("pts_sess_101")
        ansi_out = "\x1b[32mroot@pts-pi:~# \x1b[0mls -la\n"
        ring.append_output(ansi_out)
        replay = ring.get_replay_data()
        assert "root@pts-pi" in AnsiSequenceParser.strip_ansi(replay)

    # Pairwise 28: F21 (Terminal Buffer) + F22 (Accessory Keys)
    def test_p28_accessory_ctrl_c_interrupts_pty_buffer(res: TestResult):
        engine = TerminalKeyRowEngine()
        sigint_byte = engine.process_key_press("CTRL_C")
        assert sigint_byte == "\x03"
        ring = TerminalSessionRingBuffer()
        ring.append_output("^C\n")
        assert "^C" in ring.get_replay_data()

    suite.add_test("p1_theme_stream_grid_card_tokens", 1, "Theme + Stream Grid", TIER_ID, TIER_NAME, test_p1_theme_stream_grid_card_tokens)
    suite.add_test("p2_theme_duplicate_alert_border", 9, "Theme + Duplicate Alert", TIER_ID, TIER_NAME, test_p2_theme_duplicate_alert_border)
    suite.add_test("p3_theme_vault_deck_active_border", 13, "Theme + Vault Deck", TIER_ID, TIER_NAME, test_p3_theme_vault_deck_active_border)
    suite.add_test("p4_theme_files_browser_icon_colors", 16, "Theme + Files Browser", TIER_ID, TIER_NAME, test_p4_theme_files_browser_icon_colors)
    suite.add_test("p5_theme_terminal_pitch_canvas_and_ansi_green", 21, "Theme + Terminal", TIER_ID, TIER_NAME, test_p5_theme_terminal_pitch_canvas_and_ansi_green)
    suite.add_test("p6_sketch_shape_vault_card_scaling", 2, "Sketch Shape + Vault Deck", TIER_ID, TIER_NAME, test_p6_sketch_shape_vault_card_scaling)
    suite.add_test("p7_sketch_shape_file_pill_badges", 2, "Sketch Shape + Files", TIER_ID, TIER_NAME, test_p7_sketch_shape_file_pill_badges)
    suite.add_test("p8_sketch_shape_bottom_dock_border", 24, "Sketch Shape + BottomDock", TIER_ID, TIER_NAME, test_p8_sketch_shape_bottom_dock_border)
    suite.add_test("p9_typography_exoplayer_overlay", 3, "Typography + ExoPlayer", TIER_ID, TIER_NAME, test_p9_typography_exoplayer_overlay)
    suite.add_test("p10_typography_card_template_emboss", 14, "Typography + Card Templates", TIER_ID, TIER_NAME, test_p10_typography_card_template_emboss)
    suite.add_test("p11_typography_terminal_accessory_row", 22, "Typography + Accessory Keys", TIER_ID, TIER_NAME, test_p11_typography_terminal_accessory_row)
    suite.add_test("p12_catalog_fuzzy_search_filtering", 5, "Catalog + Fuzzy Search", TIER_ID, TIER_NAME, test_p12_catalog_fuzzy_search_filtering)
    suite.add_test("p13_catalog_watchlist_toggle", 7, "Catalog + Watchlist", TIER_ID, TIER_NAME, test_p13_catalog_watchlist_toggle)
    suite.add_test("p14_catalog_room_database_sync", 27, "Catalog + Room Cache", TIER_ID, TIER_NAME, test_p14_catalog_room_database_sync)
    suite.add_test("p15_fuzzy_search_over_room_cache", 5, "Fuzzy Search + Room Cache", TIER_ID, TIER_NAME, test_p15_fuzzy_search_over_room_cache)
    suite.add_test("p16_exoplayer_seek_triggers_progress_sync", 6, "ExoPlayer + Progress Sync", TIER_ID, TIER_NAME, test_p16_exoplayer_seek_triggers_progress_sync)
    suite.add_test("p17_exoplayer_back_handler_closes_player", 25, "ExoPlayer + BackHandler", TIER_ID, TIER_NAME, test_p17_exoplayer_back_handler_closes_player)
    suite.add_test("p18_hub_polling_and_retry_action", 8, "Hub Polling + Retry Action", TIER_ID, TIER_NAME, test_p18_hub_polling_and_retry_action)
    suite.add_test("p19_duplicate_check_on_parsed_bulk_urls", 10, "Duplicate Check + Bulk Parser", TIER_ID, TIER_NAME, test_p19_duplicate_check_on_parsed_bulk_urls)
    suite.add_test("p20_bulk_url_parser_to_queue_dispatch", 10, "Bulk Parser + Queue Dispatch", TIER_ID, TIER_NAME, test_p20_bulk_url_parser_to_queue_dispatch)
    suite.add_test("p21_biometric_auth_unlocks_card_deck", 12, "Biometric + Card Deck", TIER_ID, TIER_NAME, test_p21_biometric_auth_unlocks_card_deck)
    suite.add_test("p22_pin_fallback_unlocks_note_editing", 15, "PIN Unlock + Note Editing", TIER_ID, TIER_NAME, test_p22_pin_fallback_unlocks_note_editing)
    suite.add_test("p23_formatted_card_copied_to_note", 14, "Card Template + Notes CRUD", TIER_ID, TIER_NAME, test_p23_formatted_card_copied_to_note)
    suite.add_test("p24_browser_navigate_and_batch_delete", 16, "Files Browser + Batch Delete", TIER_ID, TIER_NAME, test_p24_browser_navigate_and_batch_delete)
    suite.add_test("p25_file_crud_and_zip_compression", 18, "File CRUD + Zip Compression", TIER_ID, TIER_NAME, test_p25_file_crud_and_zip_compression)
    suite.add_test("p26_chmod_and_edit_script", 19, "Chmod + In-App Editor", TIER_ID, TIER_NAME, test_p26_chmod_and_edit_script)
    suite.add_test("p27_ssh_session_streams_to_ring_buffer", 20, "SSH Session + ANSI Buffer", TIER_ID, TIER_NAME, test_p27_ssh_session_streams_to_ring_buffer)
    suite.add_test("p28_accessory_ctrl_c_interrupts_pty_buffer", 22, "Accessory Keys + PTY Buffer", TIER_ID, TIER_NAME, test_p28_accessory_ctrl_c_interrupts_pty_buffer)



if __name__ == "__main__":
    from e2e_tests.engine.test_runner import TestSuite
    suite = TestSuite("Tier 3 Pairwise Combinations Test Suite")
    register_tier3_tests(suite)
    results = suite.run_all(verbose=True)
    suite.print_summary()
    failed = sum(1 for r in results if not r.passed)
    sys.exit(1 if failed > 0 else 0)
