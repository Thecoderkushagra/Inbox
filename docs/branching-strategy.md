# Branching Strategy

This project follows a structured branching strategy to maintain stability and enable concurrent development.

## Branch Types and Responsibilities

- **`main`**: 
  - Represents the production-ready state of the application.
  - *Never* develop directly on this branch.
  - Only merges from `release` or `hotfix` branches are allowed.
- **`develop`**: 
  - The main integration branch for features.
  - Represents the latest delivered development changes for the next release.
- **`feature/*`**: 
  - Created from `develop`.
  - Used to develop new features for upcoming or distant releases.
  - Merged back into `develop` when the feature is complete and reviewed.
- **`bugfix/*`**: 
  - Created from `develop`.
  - Used to fix non-critical bugs found in development or QA environments.
  - Merged back into `develop`.
- **`hotfix/*`**: 
  - Created from `main`.
  - Used to immediately resolve critical bugs in production.
  - Must be merged back into *both* `main` and `develop`.
- **`release/*`**: 
  - Created from `develop` when preparing for a new production release.
  - Used for final bug fixes, testing, and version bumping.
  - Merged into `main` and tagged with the version number, then merged back into `develop`.
