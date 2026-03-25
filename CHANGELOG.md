# Changelog

## [1.1.0] - 26-03-2026

### Added

- **Bottom Navigation**: Seamlessly switch between Dashboard, History, and Statistics.
- **Dedicated History Screen**: Moved history logs to a separate page for better focus.
- **Statistics & Calendar**: New screen featuring a monthly saving calendar and trend chart.
- **Mock Sync System**: Architecture ready for future cloud synchronization.
- **Multi-language Support**: Enhanced Indonesian and English translations.

### Improved

- **Scrolling Performance**: Optimized `LazyColumn` with stable keys and `remember`ed calculations to ensure 60-120Hz smoothness.
- **UI Structure**: Adopted `Scaffold` with consistent top and bottom bars across all screens.
- **Code Quality**: Moved heavy logic out of composables to prevent unnecessary recompositions.
