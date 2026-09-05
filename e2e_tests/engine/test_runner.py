"""
PTS Mobile E2E Test Runner Engine
Provides test execution, assertions, timing, tier categorization,
colorful terminal reporting, and structured summary generation.
"""

import sys
import time
import traceback
from typing import Callable, List, Dict, Any, Optional

class Colors:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'
    DIM = '\033[2m'
    RESET = '\033[0m'

class TestResult:
    def __init__(self, name: str, feature_id: int, feature_name: str, tier: int, tier_name: str):
        self.name = name
        self.feature_id = feature_id
        self.feature_name = feature_name
        self.tier = tier
        self.tier_name = tier_name
        self.passed = False
        self.duration_ms = 0.0
        self.error_message: Optional[str] = None
        self.traceback: Optional[str] = None
        self.details: Dict[str, Any] = {}

class TestCase:
    def __init__(self, name: str, feature_id: int, feature_name: str, tier: int, tier_name: str, func: Callable):
        self.name = name
        self.feature_id = feature_id
        self.feature_name = feature_name
        self.tier = tier
        self.tier_name = tier_name
        self.func = func

    def run(self) -> TestResult:
        result = TestResult(self.name, self.feature_id, self.feature_name, self.tier, self.tier_name)
        start_time = time.perf_counter()
        try:
            self.func(result)
            result.passed = True
        except AssertionError as e:
            result.passed = False
            result.error_message = str(e) if str(e) else "Assertion failed"
            result.traceback = traceback.format_exc()
        except Exception as e:
            result.passed = False
            result.error_message = f"{type(e).__name__}: {str(e)}"
            result.traceback = traceback.format_exc()
        finally:
            result.duration_ms = (time.perf_counter() - start_time) * 1000.0
        return result

class TestSuite:
    def __init__(self, name: str):
        self.name = name
        self.tests: List[TestCase] = []
        self.results: List[TestResult] = []

    def add_test(self, name: str, feature_id: int, feature_name: str, tier: int, tier_name: str, func: Callable):
        self.tests.append(TestCase(name, feature_id, feature_name, tier, tier_name, func))

    def run_all(self, verbose: bool = False) -> List[TestResult]:
        self.results = []
        for test in self.tests:
            res = test.run()
            self.results.append(res)
            if verbose:
                status = f"{Colors.GREEN}✓ PASS{Colors.RESET}" if res.passed else f"{Colors.RED}✗ FAIL{Colors.RESET}"
                print(f"  [{res.tier_name}] F{res.feature_id:02d} - {res.name:<60} {status} ({res.duration_ms:.2f}ms)")
                if not res.passed and res.error_message:
                    print(f"     {Colors.RED}Error: {res.error_message}{Colors.RESET}")
        return self.results

    def get_tier_breakdown(self) -> Dict[int, Dict[str, int]]:
        breakdown: Dict[int, Dict[str, int]] = {}
        for r in self.results:
            if r.tier not in breakdown:
                breakdown[r.tier] = {"total": 0, "passed": 0, "failed": 0, "tier_name": r.tier_name}
            breakdown[r.tier]["total"] += 1
            if r.passed:
                breakdown[r.tier]["passed"] += 1
            else:
                breakdown[r.tier]["failed"] += 1
        return breakdown

    def print_summary(self):
        total = len(self.results)
        passed = sum(1 for r in self.results if r.passed)
        failed = sum(1 for r in self.results if not r.passed)
        total_time_ms = sum(r.duration_ms for r in self.results)

        print("\n" + "=" * 80)
        print(f"{Colors.BOLD}{Colors.CYAN} PTS MOBILE AUTOMATED E2E TEST SUITE REPORT{Colors.RESET}")
        print("=" * 80)

        breakdown = self.get_tier_breakdown()
        for tier in sorted(breakdown.keys()):
            data = breakdown[tier]
            t_name = data["tier_name"]
            t_tot = data["total"]
            t_pass = data["passed"]
            t_fail = data["failed"]
            pass_rate = (t_pass / t_tot * 100.0) if t_tot > 0 else 0.0
            color = Colors.GREEN if t_fail == 0 else Colors.RED
            print(f"  {Colors.BOLD}Tier {tier}: {t_name:<38}{Colors.RESET} "
                  f"Total: {t_tot:>3} | {Colors.GREEN}Pass: {t_pass:>3}{Colors.RESET} | "
                  f"{Colors.RED}Fail: {t_fail:>3}{Colors.RESET} | {color}{pass_rate:6.1f}%{Colors.RESET}")

        print("-" * 80)
        status_color = Colors.GREEN if failed == 0 else Colors.RED
        status_text = "ALL TESTS PASSED" if failed == 0 else f"{failed} TESTS FAILED"
        print(f"  {Colors.BOLD}TOTAL TEST COUNT:{Colors.RESET} {total:>3}   "
              f"{Colors.GREEN}PASSED: {passed:>3}{Colors.RESET}   "
              f"{Colors.RED}FAILED: {failed:>3}{Colors.RESET}   "
              f"Execution Time: {total_time_ms:.2f}ms")
        print(f"  {Colors.BOLD}STATUS:{Colors.RESET} {status_color}{Colors.BOLD}{status_text}{Colors.RESET}")
        print("=" * 80 + "\n")

