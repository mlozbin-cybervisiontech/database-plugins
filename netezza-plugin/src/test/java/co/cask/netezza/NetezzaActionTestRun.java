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

package co.cask.netezza;

import co.cask.db.batch.action.QueryConfig;
import com.google.common.collect.ImmutableMap;
import io.cdap.cdap.etl.api.action.Action;
import io.cdap.cdap.etl.mock.batch.MockSink;
import io.cdap.cdap.etl.mock.batch.MockSource;
import io.cdap.cdap.etl.proto.v2.ETLBatchConfig;
import io.cdap.cdap.etl.proto.v2.ETLPlugin;
import io.cdap.cdap.etl.proto.v2.ETLStage;
import io.cdap.cdap.proto.artifact.AppRequest;
import io.cdap.cdap.proto.id.ApplicationId;
import io.cdap.cdap.proto.id.NamespaceId;
import io.cdap.cdap.test.ApplicationManager;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class NetezzaActionTestRun extends NetezzaPluginTestBase {

  @Test
  public void testDBAction() throws Exception {

    ETLStage source = new ETLStage("source", MockSource.getPlugin("actionInput"));
    ETLStage sink = new ETLStage("sink", MockSink.getPlugin("actionOutput"));
    ETLStage action = new ETLStage("action", new ETLPlugin(
      NetezzaConstants.PLUGIN_NAME,
      Action.PLUGIN_TYPE,
      ImmutableMap.<String, String>builder()
        .putAll(BASE_PROPS)
        .put(QueryConfig.QUERY, "delete from db_action_test where day = '${logicalStartTime(yyyy-MM-dd,0m,UTC)}'")
        .build(),
      null));

    ETLBatchConfig config = ETLBatchConfig.builder()
      .addStage(source)
      .addStage(sink)
      .addStage(action)
      .addConnection(sink.getName(), action.getName())
      .addConnection(source.getName(), sink.getName())
      .build();

    AppRequest<ETLBatchConfig> appRequest = new AppRequest<>(DATAPIPELINE_ARTIFACT, config);
    ApplicationId appId = NamespaceId.DEFAULT.app("actionTest");
    ApplicationManager appManager = deployApplication(appId, appRequest);
    runETLOnce(appManager, ImmutableMap.of("logical.start.time", "0"));

    try (Connection connection = createConnection();
         Statement statement = connection.createStatement();
         ResultSet results = statement.executeQuery("select * from db_action_test")) {
      Assert.assertFalse(results.next());
    }
  }
}
