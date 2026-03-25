# Changelog

## [1.2.0] - 2026-03-26

### Added
- **Swipeable Calendar**: Modern horizontal swipe gesture to navigate months in the Stats screen.
- **Smooth Month Transitions**: Added fade and slide animations when switching months.
- **Immutable UI States**: Introduced `DashboardUiState` and `StatsUiState` for predictable and efficient rendering.

### Improved
- **Scrolling Performance (Critical)**:
    - Implemented `key` and `contentType` in all `LazyColumn`s to minimize recompositions.
    - Moved all heavy calculations (trend analysis, grouping, formatting) to `ViewModel`.
    - Optimized state updates using `combine` and `StateFlow` for atomic UI updates.
- **Memory & CPU Efficiency**:
    - Replaced inline calculations with `remember` blocks in Composables.
    - Reduced layout passes by simplifying component hierarchies.
    - Optimized calendar day rendering with better caching of computed dates.
- **UX Refinement**:
    - Synced swipe gestures with previous/next buttons in the calendar.
    - Improved line chart rendering with smoother paths and better scaling.
