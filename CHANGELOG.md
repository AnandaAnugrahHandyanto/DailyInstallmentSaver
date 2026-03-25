# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0] - 2024-05-20

### Added
- Navigation Compose for switching between Dashboard and Add Installment screens.
- Room Database integration with StateFlow for automatic UI updates.
- Material 3 Date Picker for selecting installment due dates.
- Total daily saving calculation on the Dashboard.
- Daily notifications via WorkManager with saving details.
- Indonesian language support (Localization).
- Responsive UI design for various screen sizes.

### Changed
- Refactored project structure to MVVM (ui, data, model, viewmodel).
- Updated UI to use Material 3 components and spacing.
- Improved currency formatting for better readability.

### Fixed
- Fixed state management issues in the dashboard list.
- Resolved incorrect daily saving calculations.
