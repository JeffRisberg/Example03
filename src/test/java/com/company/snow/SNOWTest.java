package com.company.snow;

import com.company.api.ISchemaMapping;
import com.company.common.ContentType;
import com.company.common.ContentTypeManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

@PowerMockIgnore("javax.net.ssl.*")
public class SNOWTest {

  @Rule
  public PowerMockRule rule = new PowerMockRule();

  String parametersStr = "{\n" +
    "\t\"uri\": \"https://fakeServer.service-now.com/\",\n" +
    "\t\"username\": \"admin\",\n" +
    "\t\"password\": \"fakepswd\"\n" +
    "}";

  SNOWClient mockClient;
  String tenantId;
  ISchemaMapping schemaMapping;

  @Before
  public void setupMockClient() throws Exception {
    mockClient = mock(SNOWClient.class);
    when(mockClient.getTableEntriesIncrementally(TABLE_INCIDENT, null, null, null))
      .thenReturn(new ImmutablePair<>(Optional.empty(), null));
    when(mockClient.getTableEntriesIncrementally(TABLE_PROBLEM, null, null, null))
      .thenReturn(new ImmutablePair<>(Optional.empty(), null));
    when(mockClient.getTableEntriesIncrementally(TABLE_CHANGE_REQUEST, null, null, null))
      .thenReturn(new ImmutablePair<>(Optional.empty(), null));
    when(mockClient.getTableEntriesIncrementally(TABLE_SC_REQUEST, null, null, null))
      .thenReturn(new ImmutablePair<>(Optional.empty(), null));
    when(mockClient.getTableEntriesIncrementally(TABLE_SC_REQUEST_ITEMS, null, null, null))
      .thenReturn(new ImmutablePair<>(Optional.empty(), null));

    when(mockClient.getCommentEntriesIncrementally(ArgumentMatchers.anyList(), ArgumentMatchers.isNull()))
      .thenReturn(new ImmutablePair<>(Optional.empty(), null));
    when(mockClient.getRequestItemsEntriesIncrementally(ArgumentMatchers.anyList(), ArgumentMatchers.isNull()))
      .thenReturn(new ImmutablePair<>(Optional.empty(), null));
    when(mockClient.getCMDBEntriesIncrementally(ArgumentMatchers.anyList(), ArgumentMatchers.isNull()))
      .thenReturn(new ImmutablePair<>(Optional.empty(), null));
    when(mockClient.getUserGroupEntriesIncrementally(ArgumentMatchers.anyList(), ArgumentMatchers.isNull()))
      .thenReturn(new ImmutablePair<>(Optional.empty(), null));
    when(mockClient.getUserEntriesIncrementally(ArgumentMatchers.anyList(), ArgumentMatchers.isNull()))
      .thenReturn(new ImmutablePair<>(Optional.empty(), null));
    whenNew(SNOWClient.class).withAnyArguments().thenReturn(mockClient);

    ContentTypeManager contentTypeManager = ContentTypeManager.getInstance();
    ContentType incidents = contentTypeManager.getContentType("incident");

    tenantId = "10000";
    schemaMapping = SNOWDefaultMappings.get(incidents, tenantId);
  }

  @Test
  @PrepareForTest({ConnectorClient.class, ConnectorPipeline.class, SNOWConnector.class, SNOWReader.class, SNOWClient.class})
  public void testSnowIncidentsRead() throws Exception {

    InputStream inputStream = SNOWTest.class.getResourceAsStream("/snow_incidents_all.json");
    String jsonStr = IOUtils.toString(inputStream, "UTF-8");
    JSONParser parser = new JSONParser();
    JSONObject topObject = (JSONObject) parser.parse(jsonStr);

    when(mockClient.getTableEntriesIncrementally(TABLE_INCIDENT, null, null, null))
      .thenReturn(new ImmutablePair<>(Optional.of(topObject), null));

    inputStream = SNOWTest.class.getResourceAsStream("/snow_incidents_all_comments.json");
    jsonStr = IOUtils.toString(inputStream, "UTF-8");
    topObject = (JSONObject) parser.parse(jsonStr);
    when(mockClient.getCommentEntriesIncrementally(ArgumentMatchers.anyList(), ArgumentMatchers.isNull()))
      .thenReturn(new ImmutablePair<>(Optional.of(topObject), null));

    inputStream = SNOWTest.class.getResourceAsStream("/snow_incidents_all_cmdb_ci.json");
    jsonStr = IOUtils.toString(inputStream, "UTF-8");
    topObject = (JSONObject) parser.parse(jsonStr);
    when(mockClient.getCMDBEntriesIncrementally(ArgumentMatchers.anyList(), ArgumentMatchers.isNull()))
      .thenReturn(new ImmutablePair<>(Optional.of(topObject), null));

    inputStream = SNOWTest.class.getResourceAsStream("/snow_sys_user_groups.json");
    jsonStr = IOUtils.toString(inputStream, "UTF-8");
    topObject = (JSONObject) parser.parse(jsonStr);
    when(mockClient.getUserGroupEntriesIncrementally(ArgumentMatchers.anyList(), ArgumentMatchers.isNull()))
      .thenReturn(new ImmutablePair<>(Optional.of(topObject), null));

    try {
      AtomicInteger counter = new AtomicInteger(0);

      SNOWConnector connector = new SNOWConnector("10000", -1, 0,

        new ArrayList<>(Arrays.asList(DataSourceFunction.LearnTickets)),
        parametersStr, (message) -> {
        // System.out.println(message);
        counter.incrementAndGet();
        // check comments ingestion
        // incident 9d385017c611228701d22104cc95c371 has 2 comments
        // incident 9c573169c611228700193229fff72400 has 4 comments
        TicketContent ticketContent = message.getStream().getStream(0).getTicketContent();
        String ticketId = ticketContent.getId();
        int commentsCount = ticketContent.getIncidentContent().getCommentsCount();
        if (ticketId.equals("9d385017c611228701d22104cc95c371")) {
          Assert.assertEquals("Could not process SNOW comments", commentsCount, 2);
          Assert.assertTrue(ticketContent.getCmdbCisCount() > 0);

          // assignment group
          Assert.assertTrue(ticketContent.hasAssignmentGroup());
          Assert.assertEquals("1c590685c0a8018b2a473a7159ff5d9a",
            ticketContent.getAssignmentGroup().getEntity().getExternalId());
          Assert.assertEquals("RMA Approvers", ticketContent.getAssignmentGroup().getEntity().getName());
          Assert.assertEquals(0, ticketContent.getAssignmentGroup().getRolesCount());
          Assert.assertEquals("Responsible for Return Material Authorization approvals and RMA number allocations in Inventory Management",
            ticketContent.getAssignmentGroup().getDescription());
        } else if (ticketId.equals("9c573169c611228700193229fff72400")) {
          Assert.assertEquals("Could not process SNOW comments", commentsCount, 4);
          Assert.assertTrue(ticketContent.getCmdbCisCount() > 0);

          // assignment group
          Assert.assertTrue(ticketContent.hasAssignmentGroup());
          Assert.assertEquals("220f8e71c61122840197e57c33464f70",
            ticketContent.getAssignmentGroup().getEntity().getExternalId());
          Assert.assertEquals("Catalog Request Approvers > $1000", ticketContent.getAssignmentGroup().getEntity().getName());
          Assert.assertEquals(2, ticketContent.getAssignmentGroup().getRolesCount());
          Assert.assertEquals("catalog", ticketContent.getAssignmentGroup().getRoles(0));
          Assert.assertEquals("This is the group of users that need to approve a Service Catalog request that is greater than $1000",
            ticketContent.getAssignmentGroup().getDescription());

          // cmdb ci
          CmdbCi cmdbCi = ticketContent.getCmdbCis(0);
          Assert.assertEquals("b0c4030ac0a800090152e7a4564ca36c", cmdbCi.getEntity().getExternalId());
          Assert.assertEquals("MacBook Pro 15", cmdbCi.getEntity().getName());
          Assert.assertEquals("PO100003", cmdbCi.getDisplayId());
          Assert.assertEquals(1535532665000L, cmdbCi.getLastUpdatedDate());
          Assert.assertEquals("cmdb_ci_computer", cmdbCi.getClassName());
          Assert.assertEquals("ABE-486-V17263-DO", cmdbCi.getSerialNumber());
          Assert.assertEquals("system", cmdbCi.getUpdatedBy());
          Assert.assertEquals("d501454f1b1310002502fbcd2c071334", cmdbCi.getModelId());
          Assert.assertEquals("221f79b7c6112284005d646b76ab978c", cmdbCi.getDepartment());
          Assert.assertEquals("8226baa4ac1d55eb40eb653c02649519", cmdbCi.getLocation().getBuilding());
          Assert.assertEquals("Hardware", cmdbCi.getCategory());
          Assert.assertEquals("Computer", cmdbCi.getSubcategory());
        } else {
          Assert.assertEquals("Could not process SNOW comments", commentsCount, 0);
          Assert.assertEquals(0, ticketContent.getCmdbCisCount());
          Assert.assertFalse(ticketContent.hasAssignmentGroup());
        }
      }, false, false, 0, schemaMapping);
      connector.start();

      Assert.assertTrue("Failed to get SNOW incidents", counter.get() > 0);
      // comments are read in single page for all retrieved incidents
      verify(mockClient, times(1)).getCommentEntriesIncrementally(ArgumentMatchers.anyList(), ArgumentMatchers.isNull());

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Failed to run the connector");
    }
  }
}
