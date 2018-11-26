package com.company.snow;

import com.company.api.ISchemaMapping;
import com.company.common.Field;
import com.company.common.FieldMapping;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class SNOWDefaultMappings {
  private final static long connectorTypeId = ConnectorTypeEnum.ServiceNow.getId();

  static String autodeskTenantId = "autodesk";

  static ISchemaMapping defaultSnowTicketMapping(String tenantId) {
    ArrayList<FieldMapping> fieldMappings = new ArrayList<>();

    Field field;

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("DisplayId")
      .fieldPath("displayId")
      .fieldType("String")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("number")
      .externalFieldType("String")
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("Id")
      .fieldPath("id")
      .fieldType("String")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("sys_id")
      .externalFieldType("String")
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("Title")
      .fieldPath("title.text.text")
      .fieldType("String")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("short_description")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("Description")
      .fieldPath("description.text.text")
      .fieldType("Content")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("description")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("DescriptionFormat")
      .fieldPath("description.text.textFormat")
      .fieldType("TextFormat")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .fixedValue("HTML")
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("Resolution")
      .fieldPath("resolve.solution.text.text")
      .fieldType("Content")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("close_notes")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("Status")
      .fieldPath("status.statusCode")
      .fieldType("TicketStatus")
      .fieldValues("New, Open, Assigned, InProgress, Escalated, Pending, ReOpen, Closed, Void")
      .build();
    FieldMapping statusMapping = FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("state")
      .externalFieldType(FieldType.String.name())
      .externalFieldValues("New:1, InProgress:2, Pending:3, Closed:7, Closed:4, Closed:6, Pending:-5, Pending:-7")
      .build();
    if (tenantId != null && tenantId.equals(autodeskTenantId)) {
      statusMapping.setExternalFieldPath("u_state");
      statusMapping.setExternalFieldValues("Open:Acknowledged, Assigned:Assigned, Open:Awaiting Assignment, " +
        "Closed:Closed, InProgress:In Progress, New:New, ReOpen:Reopen, Closed:Resolved, Pending:Waiting");
    }
    fieldMappings.add(statusMapping);

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("Priority")
      .fieldPath("priority")
      .fieldType("Priority")
      .fieldValues("Blocker,PHigh,Normal,PLow,PNone")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("priority")
      .externalFieldType(FieldType.String.name())
      .externalFieldValues("Blocker:1, PHigh:2, Normal:3, PLow:4, PNone:5")
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("Severity")
      .fieldPath("severity")
      .fieldType("Severity")
      .fieldValues("Critical,Urgent,High,Medium,Low")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("severity")
      .externalFieldType(FieldType.String.name())
      .externalFieldValues("Critical:1, Urgent:2, High:3, Medium:4, Low:5")
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("Category")
      .fieldPath("category.name")
      .fieldType("String")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("category")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("subcategory")
      .fieldPath("subCategory")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("subcategory")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("CategoryHierarchy")
      .fieldPath("category.hierarchy")
      .fieldType("String[]")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("subcategory")
      .externalFieldType(FieldType.String.name())
      .build());

    field = new Field();
    field.setContentTypeName(CONTENT_TYPE_TICKET);
    field.setFieldName("CreationDate");
    field.setFieldPath("creationDate");
    field.setFieldType(FieldType.Date.name());
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalDateFormat("yyyy-MM-dd HH:mm:ss")
      .externalFieldPath("sys_created_on")
      .externalFieldType(FieldType.String.name())
      .build());

    field = new Field();
    field.setContentTypeName(CONTENT_TYPE_TICKET);
    field.setFieldName("UpdatedDate");
    field.setFieldPath("lastUpdatedDate");
    field.setFieldType(FieldType.Date.name());
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalDateFormat("yyyy-MM-dd HH:mm:ss")
      .externalFieldPath("sys_updated_on")
      .externalFieldType(FieldType.String.name())
      .build());

    field = new Field();
    field.setContentTypeName(CONTENT_TYPE_TICKET);
    field.setFieldName("ClosedDate");
    field.setFieldPath("closedDate");
    field.setFieldType(FieldType.Date.name());
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalDateFormat("yyyy-MM-dd HH:mm:ss")
      .externalFieldPath("sys_updated_on")
      .externalFieldType(FieldType.String.name())
      .build());

    field = new Field();
    field.setContentTypeName(CONTENT_TYPE_TICKET);
    field.setFieldName("SLADueDate");
    field.setFieldPath("slaDueDate");
    field.setFieldType(FieldType.Date.name());
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalDateFormat("yyyy-MM-dd HH:mm:ss")
      .externalFieldPath("sla_due")
      .externalFieldType(FieldType.String.name())
      .build());

    field = new Field();
    field.setContentTypeName(CONTENT_TYPE_TICKET);
    field.setFieldName("MadeSLA");
    field.setFieldPath("madeSLA");
    field.setFieldType(FieldType.Boolean.name());
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("made_sla")
      .externalFieldType(FieldType.String.name())
      .build());

    // Incident specific
    field = new Field();
    field.setContentTypeName(CONTENT_TYPE_TICKET);
    field.setFieldName("CaseComments");
    field.setFieldPath("incidentContent.comments.comment.text.text");
    field.setFieldType("String");
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_INCIDENT)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("$._comments[*].value")
      .externalFieldType(FieldType.String.name())
      .build());

    // Change Request specific
    field = new Field();
    field.setContentTypeName(CONTENT_TYPE_TICKET);
    field.setFieldName("LastUpdatedDate");
    field.setFieldPath("lastUpdatedDate");
    field.setFieldType(FieldType.Date.name());
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_CHANGE_REQUEST)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalDateFormat("yyyy-MM-dd HH:mm:ss")
      .externalFieldPath("sys_updated_on") // overriding how this external field name is used
      .externalFieldType(FieldType.String.name())
      .build());

    field = new Field();
    field.setContentTypeName(CONTENT_TYPE_TICKET);
    field.setFieldName("CloseNotes");
    field.setFieldPath("changeContent.closeNotes.text.text");
    field.setFieldType("Content");
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_CHANGE_REQUEST)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("close_notes")
      .externalFieldType(FieldType.String.name())
      .build());

    field = new Field();
    field.setContentTypeName(CONTENT_TYPE_TICKET);
    field.setFieldName("BackoutPlan");
    field.setFieldPath("changeContent.backoutPlan.text.text");
    field.setFieldType("Content");
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_CHANGE_REQUEST)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("backout_plan")
      .externalFieldType(FieldType.String.name())
      .build());

    // Ticket ReporterId (default)
    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("ReporterId")
      .fieldPath("reporter.userIdentity.entityIdentity.externalId")
      .fieldType("String")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("$.opened_by.value")
      .externalFieldType("String")
      .build());

    // Ticket AssignedToId (default)
    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("AssignedToId")
      .fieldPath("assignedTo.userIdentity.entityIdentity.externalId")
      .fieldType("String")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("$.assigned_to.value")
      .externalFieldType("String")
      .build());

    // Ticket ClosedById (default)
    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .fieldName("ClosedById")
      .fieldPath("closedBy.userIdentity.entityIdentity.externalId")
      .fieldType("String")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_TICKET)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("$.closed_by.value")
      .externalFieldType("String")
      .build());

    SchemaMapping schemaMapping = new SchemaMapping();
    schemaMapping.setFieldMappings(fieldMappings);
    return schemaMapping;
  }

  private static ISchemaMapping defaultSnowKBMapping(String tenantId) {
    ArrayList<FieldMapping> fieldMappings = new ArrayList<>();
    Field field;

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_KNOWLEDGE_ARTICLE)
      .fieldName("Id")
      .fieldPath("id")
      .fieldType("String")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName("KnowledgeArticle")
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("sys_id")
      .externalFieldType("String")
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_KNOWLEDGE_ARTICLE)
      .fieldName("Title")
      .fieldPath("title")
      .fieldType("String")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName("KnowledgeArticle")
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("short_description")
      .externalFieldType("String")
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_KNOWLEDGE_ARTICLE)
      .fieldName("LinkedContent")
      .fieldPath("linkedContent")
      .fieldType("LinkedContent")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName("KnowledgeArticle")
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("text")
      .externalFieldType("html")
      .build());

    SchemaMapping schemaMapping = new SchemaMapping();
    schemaMapping.setFieldMappings(fieldMappings);
    return schemaMapping;
  }

  private static ISchemaMapping defaultSnowUserMapping(String tenantId) {
    ArrayList<FieldMapping> fieldMappings = new ArrayList<>();
    Field field;

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_USER)
      .fieldName("UserName")
      .fieldPath("entity.name")
      .fieldType("String")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName("User")
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("user_name")
      .externalFieldType("String")
      .build());

    field = Field.builder()
      .contentTypeName(CONTENT_TYPE_USER)
      .fieldName("Identity")
      .fieldPath("identities.externalId")
      .fieldType("String")
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(CONTENT_TYPE_USER)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath("sys_id")
      .externalFieldType("String")
      .build());

    SchemaMapping schemaMapping = new SchemaMapping();
    schemaMapping.setFieldMappings(fieldMappings);
    return schemaMapping;
  }

  private static ISchemaMapping defaultSnowCmdbCiMapping(String tenantId, boolean nestedInTicket) {
    ArrayList<FieldMapping> fieldMappings = new ArrayList<>();
    String contentTypeName = nestedInTicket ? CONTENT_TYPE_TICKET : CONTENT_TYPE_CMDB_CI;
    String pathPrefix = nestedInTicket ? "cmdbCis." : "";
    String jsonPathPrefix = "$.";
    if (nestedInTicket) {
      jsonPathPrefix = "$." + CMDB_CI_APPEND_KEY + "[*].";
    }
    Field field;

    /*
     * Not needed on the Platform side
     * Adding it causes an empty CMDB/CI entity to be created in any case, holding just this fixed value
    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("entityType")
      .fieldPath(pathPrefix + "entity.entityType")
      .fieldType(FieldType.Enum.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .fixedValue("CmdbCi")
      .build());
      */

    /*
     removed because entity.externalId doesn't exist anymore
    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("externalId")
      .fieldPath(pathPrefix + "entity.externalId")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "sys_id")
      .externalFieldType(FieldType.String.name())
      .build());
      */

    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("name")
      .fieldPath(pathPrefix + "entity.name")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "name")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("displayId")
      .fieldPath(pathPrefix + "displayId")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "po_number")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("lastUpdatedDate")
      .fieldPath(pathPrefix + "lastUpdatedDate")
      .fieldType(FieldType.Date.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "sys_updated_on")
      .externalDateFormat("yyyy-MM-dd HH:mm:ss")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("updatedBy")
      .fieldPath(pathPrefix + "updatedBy")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "sys_updated_by")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("className")
      .fieldPath(pathPrefix + "className")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "sys_class_name")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("serialNumber")
      .fieldPath(pathPrefix + "serialNumber")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "serial_number")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("modelId")
      .fieldPath(pathPrefix + "modelId")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "model_id.value")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("department")
      .fieldPath(pathPrefix + "department")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "department.value")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("building")
      .fieldPath(pathPrefix + "location.building")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "location.value")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("category")
      .fieldPath(pathPrefix + "category")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "category")
      .externalFieldType(FieldType.String.name())
      .build());

    field = Field.builder()
      .contentTypeName(contentTypeName)
      .fieldName("subcategory")
      .fieldPath(pathPrefix + "subcategory")
      .fieldType(FieldType.String.name())
      .build();
    fieldMappings.add(FieldMapping.builder()
      .contentTypeName(contentTypeName)
      .connectorTypeId(connectorTypeId)
      .tenantId(tenantId)
      .field(field)
      .externalFieldPath(jsonPathPrefix + "subcategory")
      .externalFieldType(FieldType.String.name())
      .build());

    if (tenantId != null && tenantId.equals(autodeskTenantId)) {
      field = Field.builder()
        .contentTypeName(contentTypeName)
        .fieldName("Criticality")
        .fieldPath(pathPrefix + "criticality")
        .fieldType(FieldType.Enum.name())
        .build();
      fieldMappings.add(FieldMapping.builder()
        .contentTypeName(contentTypeName)
        .connectorTypeId(connectorTypeId)
        .tenantId(tenantId)
        .field(field)
        .externalFieldPath(jsonPathPrefix + "u_criticality")
        .externalFieldType(FieldType.String.name())
        .externalFieldValues("Low:Low, Medium:Medium, High:High, Critical:Critical")
        .build());

      field = Field.builder()
        .contentTypeName(contentTypeName)
        .fieldName("UsageType")
        .fieldPath(pathPrefix + "usageType")
        .fieldType(FieldType.Enum.name())
        .fieldValues("UndefinedUsageType, Production, Staging, QA, Test, Development, Demonstration, Training, " +
          "DisasterRecovery, Lab")
        .build();
      fieldMappings.add(FieldMapping.builder()
        .contentTypeName(contentTypeName)
        .connectorTypeId(connectorTypeId)
        .tenantId(tenantId)
        .field(field)
        .externalFieldPath(jsonPathPrefix + "u_used_for")
        .externalFieldType(FieldType.String.name())
        .externalFieldValues("Production:Production, Staging:Staging, QA:QA, Test:Test, Development:Development," +
          " Demonstration:Demonstration, Training:Training, DisasterRecovery:DisasterRecovery, Lab:Lab")
        .build());
    }

    SchemaMapping schemaMapping = new SchemaMapping();
    schemaMapping.setFieldMappings(fieldMappings);
    return schemaMapping;
  }

  public static ISchemaMapping get(ContentType contentType, String tenantId) {

    if (contentType == null) {
      log.error("ContentType is null");
      return null;
    } else if (contentType.hasAncestor(ContentTypeManager.CONTENT_TYPE_TICKET)) {
      return SchemaMappingUtil.appendMappings(defaultSnowTicketMapping(tenantId),
        defaultSnowCmdbCiMapping(tenantId, true));
    } else if (contentType.hasAncestor(ContentTypeManager.CONTENT_TYPE_KNOWLEDGE_ARTICLE)) {
      return defaultSnowKBMapping(tenantId);
    } else if (contentType.hasAncestor(ContentTypeManager.CONTENT_TYPE_USER)) {
      return defaultSnowUserMapping(tenantId);
    } else if (contentType.hasAncestor(ContentTypeManager.CONTENT_TYPE_CMDB_CI)) {
      return defaultSnowCmdbCiMapping(tenantId, false);
    } else {
      log.warn("ContentType " + contentType.getName() + " has no defined default mapping");
      return null;
    }
  }
}
