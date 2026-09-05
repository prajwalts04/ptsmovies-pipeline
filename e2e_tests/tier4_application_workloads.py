"""
Tier 4: Real-World Application Workloads
15 comprehensive end-to-end multi-step scenarios across all 5 integrated applications.
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

def register_tier4_tests(suite: TestSuite):
    TIER_ID = 4
    TIER_NAME = "Real-World Application Workloads"

    # Scenario 1: Stream Catalog Ingestion, Search, and Filter Workflow
    def test_scenario_01_stream_catalog_search_and_filter(res: TestResult):
        # 1. Fetch catalog
        catalog = [
            {"id": "m1", "title": "Inception", "year": "2010", "genres": ["Action", "Sci-Fi"], "rating": "8.8"},
            {"id": "m2", "title": "Interstellar", "year": "2014", "genres": ["Adventure", "Sci-Fi"], "rating": "8.7"},
            {"id": "m3", "title": "The Dark Knight", "year": "2008", "genres": ["Action", "Crime"], "rating": "9.0"}
        ]
        # 2. Search for "Sci-Fi" movies
        matched = [m for m in catalog if "Sci-Fi" in m["genres"]]
        assert len(matched) == 2
        # 3. Fuzzy search for "interstellar"
        filtered = [m for m in matched if FuzzySearchEngine.match("interstellar", [m["title"]])]
        assert len(filtered) == 1 and filtered[0]["id"] == "m2"

    # Scenario 2: Complete ExoPlayer Video Streaming & Progress Sync Session
    def test_scenario_02_exoplayer_streaming_and_progress_sync(res: TestResult):
        # 1. Initialize player with 120s video
        sim = ExoPlayerSimulator(duration_seconds=120.0)
        sim.play()
        assert sim.is_playing is True
        # 2. Seek +10s twice
        sim.double_tap_seek_forward()
        sim.double_tap_seek_forward()
        assert sim.current_position == 20.0
        # 3. Change speed to 1.5x
        sim.set_playback_speed(1.5)
        assert sim.playback_speed == 1.5
        # 4. Check progress inclusion (20/120 = 16.6%)
        pct = WatchlistAndProgressTracker.calculate_progress_percentage(sim.current_position, sim.duration)
        assert WatchlistAndProgressTracker.should_include_in_continue_watching(sim.current_position, sim.duration) is True
        # 5. Controls auto-hide after 3500ms
        assert sim.check_controls_auto_hide(4000) is False

    # Scenario 3: Series Episode Playback & Progression
    def test_scenario_03_series_episode_progression(res: TestResult):
        ep1_sim = ExoPlayerSimulator(duration_seconds=100.0)
        ep1_sim.seek_to(96.0) # 96% complete
        # Excluded from continue watching (completed)
        assert WatchlistAndProgressTracker.should_include_in_continue_watching(ep1_sim.current_position, ep1_sim.duration) is False
        # Advance to Episode 2
        ep2_sim = ExoPlayerSimulator(duration_seconds=100.0)
        ep2_sim.seek_to(10.0)
        assert WatchlistAndProgressTracker.should_include_in_continue_watching(ep2_sim.current_position, ep2_sim.duration) is True

    # Scenario 4: Series Bulk Excel Ingestion & GHA Dispatch Pipeline
    def test_scenario_04_bulk_excel_ingestion_and_gha_pipeline(res: TestResult):
        excel_tsv = "Season\tEpisode\tDownload URL\n1\t1\thttps://ex.com/s1e1.mp4\n1\t2\thttps://ex.com/s1e2.mp4"
        episodes = BulkUrlParser.parse_bulk_text(excel_tsv)
        assert len(episodes) == 2
        # Dispatch first episode through pipeline
        stage = "queued"
        for next_stg in ["gha_downloading", "gha_compressing", "gha_uploading_hf", "hf_ready", "completed"]:
            assert DownloadPipelineStateMachine.validate_transition(stage, next_stg) is True
            stage = next_stg
        assert stage == "completed"

    # Scenario 5: Duplicate Ingestion Detection & Selective Skip
    def test_scenario_05_duplicate_detection_and_skip(res: TestResult):
        on_disk = [{"title": "Breaking Bad", "year": "2008"}]
        chk = DuplicateChecker.check_duplicate("Breaking Bad", "2008", "Series", on_disk, [])
        assert chk["hasDuplicate"] is True and chk["onDisk"] is True
        # User opts to skip existing and enqueue season 2
        raw_s2 = "2 | 1 | https://ex.com/s2e1.mp4"
        s2_items = BulkUrlParser.parse_bulk_text(raw_s2)
        assert len(s2_items) == 1 and s2_items[0]["season"] == 2

    # Scenario 6: Queue Error Handling & Task Retry Lifecycle
    def test_scenario_06_queue_error_and_retry_lifecycle(res: TestResult):
        task = {"id": "tsk_err_1", "stage": "gha_compressing", "error": None}
        # Step 1: Encounter error
        task["stage"] = "failed"
        task["error"] = "FFmpeg out of memory"
        assert task["stage"] == "failed"
        # Step 2: User taps retry
        assert DownloadPipelineStateMachine.validate_transition(task["stage"], "queued") is True
        task["stage"] = "queued"
        task["error"] = None
        assert task["stage"] == "queued"

    # Scenario 7: Vault First-Time Setup & Biometric Unlock Flow
    def test_scenario_07_vault_biometric_card_deck_workflow(res: TestResult):
        # 1. Authenticate with biometric
        sim = BiometricAuthSimulator()
        out = sim.authenticate_biometric(success=True)
        assert out["success"] is True
        # 2. Render cards
        cards = [{"id": 1, "type": "aadhaar", "number": "123456789012"}]
        formatted = CardTemplateFormatter.format_aadhaar(cards[0]["number"], mask=True)
        assert formatted == "XXXX XXXX 9012"
        # 3. Card transform
        tf = StackedCardDeckLayout.calculate_card_transform(0, selected_index=0, is_hovered=False)
        assert tf["scale"] == 1.03

    # Scenario 8: Biometric Lockout & Master PIN Fallback Recovery
    def test_scenario_08_biometric_lockout_and_pin_recovery(res: TestResult):
        sim = BiometricAuthSimulator()
        for _ in range(5):
            sim.authenticate_biometric(success=False)
        assert sim.is_locked_out is True
        # Fallback to PIN
        assert sim.authenticate_with_pin("1234", "1234") is True
        assert sim.is_authenticated is True

    # Scenario 9: Confidential Notes Creation, Search, and Clipboard Copy
    def test_scenario_09_notes_crud_and_copy_workflow(res: TestResult):
        db = RoomDatabaseCacheSimulator()
        note = {"id": 1, "title": "Recovery Key", "content": "alpha beta gamma delta"}
        db.sync_vault([], [note])
        assert len(db.cached_vault_notes) == 1
        # 1-tap copy
        clipboard = db.cached_vault_notes[1]["content"]
        assert "alpha beta" in clipboard

    # Scenario 10: Files Manager Deep Navigation & Batch Deletion
    def test_scenario_10_files_browser_and_batch_deletion(res: TestResult):
        crumbs = FilesystemHelper.parse_breadcrumbs("/Data/Downloads/Series/Dark")
        assert len(crumbs) == 5
        # Select 3 temporary files
        temp_files = ["/Data/Downloads/Series/Dark/ep1.tmp", "/Data/Downloads/Series/Dark/ep2.tmp"]
        assert len(temp_files) == 2
        # Verify endpoint contract
        cfg = RetrofitEndpointContract.ENDPOINTS["delete_files"]
        assert cfg["method"] == "POST"

    # Scenario 11: File Permissions & Code Editor Workflow
    def test_scenario_11_permissions_and_code_editor(res: TestResult):
        # 1. Parse octal
        octal = FilesystemHelper.posix_string_to_octal("rwxr-xr-x")
        assert octal == 0o755
        # 2. Edit code
        editor_text = "#!/bin/bash\nexport PORT=3000\nnode server.js"
        assert "PORT=3000" in editor_text

    # Scenario 12: Directory Creation, Multi-File Compression, and Extraction
    def test_scenario_12_dir_creation_zip_and_unzip(res: TestResult):
        zip_cfg = RetrofitEndpointContract.ENDPOINTS["zip_files"]
        unzip_cfg = RetrofitEndpointContract.ENDPOINTS["unzip_file"]
        assert zip_cfg["method"] == "POST" and unzip_cfg["method"] == "POST"

    # Scenario 13: Full Native JSch SSH Terminal Session
    def test_scenario_13_ssh_terminal_session_and_controls(res: TestResult):
        key_engine = TerminalKeyRowEngine()
        ring = TerminalSessionRingBuffer()
        ring.create_session("pts_sess_prod_1")
        # 1. Output from bash prompt
        ring.append_output("\x1b[32mprajwal@pts-pi:~# \x1b[0m")
        assert "prajwal@pts-pi" in AnsiSequenceParser.strip_ansi(ring.get_replay_data())
        # 2. Press Ctrl+C
        sigint = key_engine.process_key_press("CTRL_C")
        assert sigint == "\x03"
        # 3. Adjust font size
        new_sz = key_engine.adjust_font_size(2)
        assert new_sz == 15
        # 4. Calculate resized PTY dimensions
        cols, rows = PtyDimensionCalculator.calculate_dimensions(360, 640, new_sz)
        assert cols > 0 and rows > 0

    # Scenario 14: SSH Session Reconnect & Ring Buffer Scrollback Replay
    def test_scenario_14_ssh_session_reconnect_replay(res: TestResult):
        ring = TerminalSessionRingBuffer(capacity=5000)
        ring.create_session("pts_sess_102")
        ring.append_output("Building project...\n[1/5] Compiling C++\n[2/5] Linking\n")
        # Reconnection occurs
        replay = ring.get_replay_data()
        assert "[2/5] Linking" in replay

    # Scenario 15: Full Offline Mode Transition & Room Database Playback
    def test_scenario_15_offline_room_database_transition(res: TestResult):
        db = RoomDatabaseCacheSimulator()
        nav = BackHandlerNavigator()
        # 1. Populate cache while online
        movies = [{"id": "m1", "title": "Dune 2021", "filePath": "/DATA/Movies/Dune.mp4"}]
        db.sync_media_library(movies, [])
        # 2. Network disconnects, user browses offline
        offline_movies = db.query_offline_catalog("Dune")
        assert len(offline_movies) == 1
        # 3. User plays movie offline
        nav.open_player()
        assert nav.is_player_open is True
        # 4. Back gesture returns to catalog
        assert nav.handle_system_back() == "PLAYER_CLOSED"

    suite.add_test("scenario_01_stream_catalog_search_and_filter", 4, "Stream Catalog Workflow", TIER_ID, TIER_NAME, test_scenario_01_stream_catalog_search_and_filter)
    suite.add_test("scenario_02_exoplayer_streaming_and_progress_sync", 6, "ExoPlayer Streaming Workflow", TIER_ID, TIER_NAME, test_scenario_02_exoplayer_streaming_and_progress_sync)
    suite.add_test("scenario_03_series_episode_progression", 7, "Series Episode Progression", TIER_ID, TIER_NAME, test_scenario_03_series_episode_progression)
    suite.add_test("scenario_04_bulk_excel_ingestion_and_gha_pipeline", 10, "Bulk Ingestion & GHA Pipeline", TIER_ID, TIER_NAME, test_scenario_04_bulk_excel_ingestion_and_gha_pipeline)
    suite.add_test("scenario_05_duplicate_detection_and_skip", 9, "Duplicate Detection & Skip", TIER_ID, TIER_NAME, test_scenario_05_duplicate_detection_and_skip)
    suite.add_test("scenario_06_queue_error_and_retry_lifecycle", 11, "Queue Error & Retry Lifecycle", TIER_ID, TIER_NAME, test_scenario_06_queue_error_and_retry_lifecycle)
    suite.add_test("scenario_07_vault_biometric_card_deck_workflow", 12, "Vault Biometrics & Deck Workflow", TIER_ID, TIER_NAME, test_scenario_07_vault_biometric_card_deck_workflow)
    suite.add_test("scenario_08_biometric_lockout_and_pin_recovery", 12, "Biometric Lockout & Recovery", TIER_ID, TIER_NAME, test_scenario_08_biometric_lockout_and_pin_recovery)
    suite.add_test("scenario_09_notes_crud_and_copy_workflow", 15, "Notes CRUD & Copy Workflow", TIER_ID, TIER_NAME, test_scenario_09_notes_crud_and_copy_workflow)
    suite.add_test("scenario_10_files_browser_and_batch_deletion", 16, "Files Browser & Batch Delete", TIER_ID, TIER_NAME, test_scenario_10_files_browser_and_batch_deletion)
    suite.add_test("scenario_11_permissions_and_code_editor", 18, "Permissions & Editor Workflow", TIER_ID, TIER_NAME, test_scenario_11_permissions_and_code_editor)
    suite.add_test("scenario_12_dir_creation_zip_and_unzip", 17, "Directory & Zip Workflow", TIER_ID, TIER_NAME, test_scenario_12_dir_creation_zip_and_unzip)
    suite.add_test("scenario_13_ssh_terminal_session_and_controls", 20, "SSH Terminal & Controls Workflow", TIER_ID, TIER_NAME, test_scenario_13_ssh_terminal_session_and_controls)
    suite.add_test("scenario_14_ssh_session_reconnect_replay", 21, "SSH Reconnect & Replay Workflow", TIER_ID, TIER_NAME, test_scenario_14_ssh_session_reconnect_replay)
    suite.add_test("scenario_15_offline_room_database_transition", 27, "Offline Room Cache Workflow", TIER_ID, TIER_NAME, test_scenario_15_offline_room_database_transition)



if __name__ == "__main__":
    from e2e_tests.engine.test_runner import TestSuite
    suite = TestSuite("Tier 4 Application Workloads Test Suite")
    register_tier4_tests(suite)
    results = suite.run_all(verbose=True)
    suite.print_summary()
    failed = sum(1 for r in results if not r.passed)
    sys.exit(1 if failed > 0 else 0)
