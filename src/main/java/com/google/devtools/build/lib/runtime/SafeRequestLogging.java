// Copyright 2020 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.runtime;

import java.util.List;

/** Utils for logging safely user commandlines. */
public class SafeRequestLogging {
  private SafeRequestLogging() {}

  /**
   * Generates a string form of a request to be written to the logs, filtering the user environment
   * to remove anything that looks private. The current filter criteria removes any variable whose
   * name includes "auth", "pass", "cookie" or "token".
   *
   * @return the filtered request to write to the log.
   */
  public static String getRequestLogString(List<String> requestStrings) {
    // TODO(jmmv): Reuse the same logic we have in the BEP redaction.
    return "REDACTED";
  }
}
