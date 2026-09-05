"""
PTS Mobile Master Automated Test Runner
Executes Tiers 1-4 (≥311 test cases across 27 features),
validates requirement specifications, and generates comprehensive test reports.
"""

import sys
import os
import argparse

# Add parent directory to sys.path so e2e_tests module can be imported
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from e2e_tests.engine.test_runner import TestSuite, Colors
from e2e_tests.tier1_feature_coverage import register_tier1_tests
from e2e_tests.tier2_boundary_cases import register_tier2_tests
from e2e_tests.tier3_pairwise_combinations import register_tier3_tests
from e2e_tests.tier4_application_workloads import register_tier4_tests

def main():
    parser = argparse.ArgumentParser(description="PTS Mobile E2E Automated Test Suite Runner")
    parser.add_argument("--verbose", "-v", action="store_true", help="Print detailed per-test output")
    parser.add_argument("--tier", "-t", type=int, choices=[1, 2, 3, 4], help="Run a specific tier only")
    args = parser.parse_args()

    suite = TestSuite("PTS Mobile E2E Master Test Suite")

    print(f"\n{Colors.BOLD}{Colors.HEADER}================================================================================{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.HEADER}    INITIALIZING PTS MOBILE 4-TIER AUTOMATED E2E TEST SUITE                     {Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.HEADER}================================================================================{Colors.RESET}\n")

    if args.tier is None or args.tier == 1:
        register_tier1_tests(suite)
    if args.tier is None or args.tier == 2:
        register_tier2_tests(suite)
    if args.tier is None or args.tier == 3:
        register_tier3_tests(suite)
    if args.tier is None or args.tier == 4:
        register_tier4_tests(suite)

    print(f"  Registered {Colors.BOLD}{len(suite.tests)}{Colors.RESET} total test cases across requested tiers.")
    print(f"  Executing test suite now...\n")

    results = suite.run_all(verbose=args.verbose)
    suite.print_summary()

    failed = sum(1 for r in results if not r.passed)
    if failed > 0:
        print(f"{Colors.RED}{Colors.BOLD}FAILED: {failed} test(s) failed.{Colors.RESET}")
        sys.exit(1)
    else:
        print(f"{Colors.GREEN}{Colors.BOLD}SUCCESS: All {len(results)} test cases passed successfully (100% PASS RATE).{Colors.RESET}\n")
        sys.exit(0)

if __name__ == "__main__":
    main()
