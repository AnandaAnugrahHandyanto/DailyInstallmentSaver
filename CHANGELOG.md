# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0] - 2024-05-22

### Added
- **Smart Suggestion System**: Recommends daily saving amount, highlighting if user missed previous days.
- **Mark as Saved**: Button to track daily savings per installment.
- **Progress Tracking**: Visual progress bar and percentage for each installment.
- **Wallet Breakdown**: Displays total daily savings categorized by wallet (Dana, Gopay, etc.).
- **Localization Support**: Full Indonesian and English support with persistent setting.

### Fixed
- **Currency Formatting**: Updated to proper Indonesian format (e.g., Rp 3.440).
- **Language Toggle**: Fixed persistence and immediate UI update without full app restart.
- **Database Schema**: Updated to version 2 to support saving progress and tracking dates.

### Changed
- **UI Improvements**: Enhanced Dashboard with cards, better spacing, and improved readability for daily calculations.
- **Architecture**: Refined MVVM implementation in `InstallmentViewModel`.
