/*
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.auroradb.postgres;

/**
 * Aurora DB PostgreSQL constants.
 */
public final class AuroraPostgresConstants {

  private AuroraPostgresConstants() {
    throw new AssertionError("Should not instantiate static utility class.");
  }

  public static final String PLUGIN_NAME = "AuroraPostgres";
  public static final String AURORA_POSTGRES_CONNECTION_STRING_FORMAT = "jdbc:postgresql://%s:%s/%s";
  public static final String CONNECTION_TIMEOUT = "connectionTimeout";
  public static final String CONNECTION_TIMEOUT_DESCRIPTION =
    "The timeout value used for socket connect operations. If connecting to the server takes longer" +
    " than this value, the connection is broken. " +
    "The timeout is specified in seconds and a value of zero means that it is disabled.";
}
