# Changelog

## [1.3.0] - 2026-03-26

### Added
- **Backup & Restore**: Users can now export their data to a JSON file and restore it later using the Storage Access Framework.
- **Home Screen Widget**: A new Glance-based widget that displays today's saving goal and a breakdown per wallet for quick access.
- **Settings Screen**: Dedicated screen to manage app language, toggle notifications, and perform data backups.
- **Smart Reminder System**: Enhanced notification logic that detects if the user missed previous days and adjusts the message to be more encouraging.

### Improved
- **Data Persistence**: Updated Room DAO to support bulk operations for faster backup and restore processes.
- **User Experience**: Improved keyboard handling in forms with `adjustResize` and added high-priority notification channels for reminders.
- **Multi-language**: Added comprehensive translations for all new features in both English and Indonesian.
- **Navigation**: Integrated Settings into the main bottom navigation bar.

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
