# Snowflake Patches Omitted in 9.0.0 Migration

This document tracks Snowflake patches from `release-8.2.1-sf5` that are no longer needed in the `release-9.0.0` migration.

## Patch #10: Set deadlines for Execute RPCs in GrpcRemoteExecutor
- **Commit:** d4cd10f406
- **Status:** MERGED UPSTREAM
- **Merged as:** 62dc3678b4 in release-9.0.0
- **Upstream PR:** #26231 "lib/remote: Remove ExperimentalGrpcRemoteExecutor"
- **Reason:** The full PR #26231 was merged to 9.0.0, which includes the deadline feature that was backported in the Snowflake patch, plus the complete removal of ExperimentalGrpcRemoteExecutor. The 9.0.0 version provides more functionality than just the backported deadline feature.
- **Action:** No action needed - functionality already present in 9.0.0

## Patch #3: Fix zlib for new macOS version
- **Commit:** 3bab8208bd
- **Status:** OBSOLETE
- **Reason:** The file `third_party/zlib/zutil.h` no longer exists in release-9.0.0. The zlib dependency has been moved to an external dependency, so this platform-specific fix is no longer applicable to the Bazel codebase.
- **Action:** No action needed - file no longer exists

---

**Total Patches Omitted:** 2 of 13 (15%)
**Patches Remaining to Port:** 11
