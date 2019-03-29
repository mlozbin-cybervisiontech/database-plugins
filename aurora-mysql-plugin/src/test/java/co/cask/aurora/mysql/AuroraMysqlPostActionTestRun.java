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

package co.cask.aurora.mysql;

import co.cask.ConnectionConfig;
import co.cask.auroradb.mysql.AuroraMysqlConstants;
import co.cask.cdap.etl.api.batch.PostAction;
import co.cask.cdap.etl.mock.batch.MockSink;
import co.cask.cdap.etl.mock.batch.MockSource;
import co.cask.cdap.etl.proto.v2.ETLBatchConfig;
import co.cask.cdap.etl.proto.v2.ETLPlugin;
import co.cask.cdap.etl.proto.v2.ETLStage;
import co.cask.cdap.proto.artifact.AppRequest;
import co.cask.cdap.proto.id.ApplicationId;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.cdap.test.ApplicationManager;
import co.cask.db.batch.action.QueryActionConfig;
import co.cask.db.batch.action.QueryConfig;
import co.cask.hydrator.common.batch.action.Condition;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AuroraMysqlPostActionTestRun extends AuroraMysqlPluginTestBase {

  @Test
  public void testAction() throws Exception {
    ETLStage source = new ETLStage("source", MockSource.getPlugin("actionInput"));
    ETLStage sink = new ETLStage("sink", MockSink.getPlugin("actionOutput"));
    ETLStage action = new ETLStage("action", new ETLPlugin(
      AuroraMysqlConstants.PLUGIN_NAME,
      PostAction.PLUGIN_TYPE,
      ImmutableMap.<String, String>builder()
        .putAll(BASE_PROPS)
        .put(QueryConfig.QUERY, "delete from postActionTest where day = '${logicalStartTime(yyyy-MM-dd,0m,UTC)}'")
        .put(ConnectionConfig.ENABLE_AUTO_COMMIT, "false")
        .put(QueryActionConfig.RUN_CONDITION, Condition.SUCCESS.name())
        .build(),
      null));

    ETLBatchConfig config = ETLBatchConfig.builder()
      .addStage(source)
      .addStage(sink)
      .addPostAction(action)
      .addConnection(source.getName(), sink.getName())
      .build();

    AppRequest<ETLBatchConfig> appRequest = new AppRequest<>(DATAPIPELINE_ARTIFACT, config);
    ApplicationId appId = NamespaceId.DEFAULT.app("postActionTest");
    ApplicationManager appManager = deployApplication(appId, appRequest);
    runETLOnce(appManager, ImmutableMap.of("logical.start.time", "0"));

    try (Connection connection = createConnection();
         Statement statement = connection.createStatement();
         ResultSet results = statement.executeQuery("select * from postActionTest")) {
          Assert.assertFalse(results.next());
    }
  }
}