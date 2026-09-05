# E2E Test Infra: pts-mobile Native Jetpack Compose Rewrite

## Test Philosophy
- Opaque-box, requirement-driven. Derived from `ORIGINAL_REQUEST.md` and web application specifications.
- Methodology: Category-Partition + Boundary Value Analysis (BVA) + Pairwise Combinatorial Testing + Real-World Workload Testing.
- Test execution: Automated test suite runner verifying all features across all 5 applications without relying on internal implementation details.

## Feature Inventory & Test Coverage Goals
| # | Feature | Source | Tier 1 (Min 5) | Tier 2 (Min 5) | Tier 3 (Pairwise) | Tier 4 (Scenario) |
|---|---------|--------|:--------------:|:--------------:|:-----------------:|:-----------------:|
| 1 | Pitch Black / Emerald Theme | R1 | 5 | 5 | ✓ | ✓ |
| 2 | Sketch Border Shape & Modifiers | R1 | 5 | 5 | ✓ | ✓ |
| 3 | Sketch Custom Typography | R1 | 5 | 5 | ✓ | ✓ |
| 4 | Stream Media Catalog & Grid | R2 | 5 | 5 | ✓ | ✓ |
| 5 | Stream Fuzzy Search Matcher | R2 | 5 | 5 | ✓ | ✓ |
| 6 | Native Media3 ExoPlayer | R2 | 5 | 5 | ✓ | ✓ |
| 7 | Watchlist & Progress Sync | R2 | 5 | 5 | ✓ | ✓ |
| 8 | Hub Queue Live Polling | R2 | 5 | 5 | ✓ | ✓ |
| 9 | Real-time Duplicate Warning | R2 | 5 | 5 | ✓ | ✓ |
| 10 | Bulk URL Multi-Column Parser | R2 | 5 | 5 | ✓ | ✓ |
| 11 | Queue Task Actions | R2 | 5 | 5 | ✓ | ✓ |
| 12 | Vault Biometric Auth | R2 | 5 | 5 | ✓ | ✓ |
| 13 | Stacked Card Deck UI | R2 | 5 | 5 | ✓ | ✓ |
| 14 | Multi-Template Cards | R2 | 5 | 5 | ✓ | ✓ |
| 15 | Secure Notes CRUD | R2 | 5 | 5 | ✓ | ✓ |
| 16 | Files Folder Browser | R2 | 5 | 5 | ✓ | ✓ |
| 17 | File & Directory Operations | R2 | 5 | 5 | ✓ | ✓ |
| 18 | Archive & Permissions Tools | R2 | 5 | 5 | ✓ | ✓ |
| 19 | In-App Editor & Media Preview | R2 | 5 | 5 | ✓ | ✓ |
| 20 | Real JSch SSH Connection | R2 | 5 | 5 | ✓ | ✓ |
| 21 | Interactive Terminal & ANSI Buffer | R2 | 5 | 5 | ✓ | ✓ |
| 22 | Mobile Accessory Key Row | R2 | 5 | 5 | ✓ | ✓ |
| 23 | Quick Command Drawer | R2 | 5 | 5 | ✓ | ✓ |
| 24 | DynamicBottomDock Navigation | R3 | 5 | 5 | ✓ | ✓ |
| 25 | Robust BackHandler Routing | R3 | 5 | 5 | ✓ | ✓ |
| 26 | Unified Retrofit Client | Acceptance | 5 | 5 | ✓ | ✓ |
| 27 | Room Offline Caching | Architecture | 5 | 5 | ✓ | ✓ |

## Minimum Thresholds
- Total identified features: N = 27 core features
- Tier 1 (Feature Coverage): ≥ 135 tests (5 × 27)
- Tier 2 (Boundary & Corner Cases): ≥ 135 tests (5 × 27)
- Tier 3 (Cross-Feature Combinations): ≥ 27 pairwise tests
- Tier 4 (Real-World Application Scenarios): ≥ 14 comprehensive workloads
- **Total Minimum Target**: ≥ 311 test cases across Tiers 1-4.
