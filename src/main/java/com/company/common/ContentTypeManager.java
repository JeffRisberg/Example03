package com.company.common;

import com.company.common.*;
import com.company.connector.beans.ServiceCatalogItem;
import com.company.connector.beans.enums.DataSourceFunction;
import com.company.service.ServiceCatalog;
import com.company.service.ServiceCategory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ContentTypeManager {

  // The generic class
  public final static String CONTENT_TYPE_JSON = "JSON";

  // A ticket and its subtypes
  public final static String CONTENT_TYPE_TICKET = "Ticket";
  public final static String CONTENT_TYPE_INCIDENT = "Incident";
  public final static String CONTENT_TYPE_PROBLEM = "Problem";
  public final static String CONTENT_TYPE_CHANGE_REQUEST = "ChangeRequest";
  public final static String CONTENT_TYPE_ALERT = "Alert";
  public final static String CONTENT_TYPE_REQUEST = "Request";
  public final static String CONTENT_TYPE_REQUEST_ITEM = "RequestItem";

  // ContentTypes associated with Tickets
  public final static String CONTENT_TYPE_CMDB_CI = "CMDB_CI";
  public final static String CONTENT_TYPE_APPLICATION = "Application";
  public final static String CONTENT_TYPE_DEVICE = "Device";
  public final static String CONTENT_TYPE_SERVICE = "Service";

  // ContentTypes for ServiceCatalogs
  public final static String CONTENT_TYPE_SERVICE_CATALOG = "ServiceCatalog";
  public final static String CONTENT_TYPE_SERVICE_CATALOG_ITEM = "ServiceCatalogItem";
  public final static String CONTENT_TYPE_SERVICE_CATALOG_CATEGORY = "ServiceCatalogCategory";

  // A knowledge article and its subtypes
  public final static String CONTENT_TYPE_KNOWLEDGE_ARTICLE = "KnowledgeArticle";
  public final static String CONTENT_TYPE_EXTERNAL_DOCUMENT = "ExternalDocument";
  public final static String CONTENT_TYPE_FAQ = "FAQ";

  // Other important objects
  public final static String CONTENT_TYPE_EVENT = "Event";
  public final static String CONTENT_TYPE_USER = "User";
  public final static String CONTENT_TYPE_USER_GROUP = "UserGroup";

  private static ContentTypeManager instance = null;

  protected Map<String, ContentType> rootContentTypes = new HashMap<String, ContentType>();

  public static ContentTypeManager getInstance() {
    if (instance == null) instance = new ContentTypeManager();
    return instance;
  }

  protected ContentTypeManager() {
    ContentType contentType;

    ContentType jsonContentType = new ContentType(CONTENT_TYPE_JSON);
    rootContentTypes.put(jsonContentType.getName(), jsonContentType);

    // Ticket related content types
    ContentType ticketContentType = new ContentType(CONTENT_TYPE_TICKET);
    rootContentTypes.put(ticketContentType.getName(), ticketContentType);

    contentType = new ContentType(CONTENT_TYPE_INCIDENT);
    contentType.setParent(ticketContentType);
    contentType.setProtobufClassName(TicketContent.class.getSimpleName());
    contentType.setCreatable(true);
    contentType.setModifiable(true);
    ticketContentType.addChild(contentType);

    contentType = new ContentType(CONTENT_TYPE_PROBLEM);
    contentType.setParent(ticketContentType);
    contentType.setProtobufClassName(TicketContent.class.getSimpleName());
    ticketContentType.addChild(contentType);

    contentType = new ContentType(CONTENT_TYPE_CHANGE_REQUEST);
    contentType.setParent(ticketContentType);
    contentType.setProtobufClassName(TicketContent.class.getSimpleName());
    ticketContentType.addChild(contentType);

    contentType = new ContentType(CONTENT_TYPE_ALERT);
    contentType.setParent(ticketContentType);
    contentType.setProtobufClassName(TicketContent.class.getSimpleName());
    ticketContentType.addChild(contentType);

    contentType = new ContentType(CONTENT_TYPE_REQUEST);
    contentType.setParent(ticketContentType);
    contentType.setProtobufClassName(TicketContent.class.getSimpleName());
    contentType.setCreatable(true);
    ticketContentType.addChild(contentType);

    contentType = new ContentType(CONTENT_TYPE_REQUEST_ITEM);
    contentType.setParent(ticketContentType);
    contentType.setProtobufClassName(TicketContent.class.getSimpleName());
    contentType.setCreatable(true);
    ticketContentType.addChild(contentType);

    // ContentTypes associated with Tickets
    ContentType cmdbCiContentType = new ContentType(CONTENT_TYPE_CMDB_CI);
    cmdbCiContentType.setProtobufClassName(CmdbCi.class.getSimpleName());
    rootContentTypes.put(cmdbCiContentType.getName(), cmdbCiContentType);

    ContentType applicationContentType = new ContentType(CONTENT_TYPE_APPLICATION);
    applicationContentType.setProtobufClassName(Application.class.getSimpleName());
    applicationContentType.setParent(cmdbCiContentType);
    cmdbCiContentType.addChild(applicationContentType);

    ContentType deviceContentType = new ContentType(CONTENT_TYPE_DEVICE);
    deviceContentType.setProtobufClassName(Device.class.getSimpleName());
    deviceContentType.setParent(cmdbCiContentType);
    cmdbCiContentType.addChild(deviceContentType);

    ContentType serviceContentType = new ContentType(CONTENT_TYPE_SERVICE);
    serviceContentType.setProtobufClassName(Service.class.getSimpleName());
    serviceContentType.setParent(cmdbCiContentType);
    cmdbCiContentType.addChild(serviceContentType);

    // Knowledge Article content types
    ContentType knowledgeArticleContentType = new ContentType(CONTENT_TYPE_KNOWLEDGE_ARTICLE);
    knowledgeArticleContentType.setProtobufClassName(ExternalDocument.class.getSimpleName());
    rootContentTypes.put(knowledgeArticleContentType.getName(), knowledgeArticleContentType);

    contentType = new ContentType(CONTENT_TYPE_EXTERNAL_DOCUMENT);
    contentType.setProtobufClassName(ExternalDocument.class.getSimpleName());
    knowledgeArticleContentType.addChild(contentType);

    contentType = new ContentType(CONTENT_TYPE_FAQ);
    contentType.setProtobufClassName(FAQContent.class.getSimpleName());
    knowledgeArticleContentType.addChild(contentType);

    // ServiceCatalog related
    ContentType serviceCatalogContentType = new ContentType(CONTENT_TYPE_SERVICE_CATALOG);
    serviceCatalogContentType.setProtobufClassName(ServiceCatalog.class.getSimpleName());
    rootContentTypes.put(serviceCatalogContentType.getName(), serviceCatalogContentType);

    ContentType scItemContentType = new ContentType(CONTENT_TYPE_SERVICE_CATALOG_ITEM);
    scItemContentType.setProtobufClassName(ServiceCatalogItem.class.getSimpleName());
    scItemContentType.setParent(serviceCatalogContentType);
    serviceCatalogContentType.addChild(scItemContentType);

    ContentType scCategoryContentType = new ContentType(CONTENT_TYPE_SERVICE_CATALOG_CATEGORY);
    scCategoryContentType.setProtobufClassName(ServiceCategory.class.getSimpleName());
    scCategoryContentType.setParent(serviceCatalogContentType);
    serviceCatalogContentType.addChild(scCategoryContentType);

    // Others
    ContentType eventContentType = new ContentType(CONTENT_TYPE_EVENT);
    eventContentType.setProtobufClassName(EventContent.class.getSimpleName());
    rootContentTypes.put(eventContentType.getName(), eventContentType);

    ContentType userContentType = new ContentType(CONTENT_TYPE_USER);
    userContentType.setProtobufClassName(User.class.getSimpleName());
    rootContentTypes.put(userContentType.getName(), userContentType);

    ContentType userGroupContentType = new ContentType(CONTENT_TYPE_USER_GROUP);
    userGroupContentType.setProtobufClassName(UserGroup.class.getSimpleName());
    rootContentTypes.put(userGroupContentType.getName(), userGroupContentType);
  }

  public Collection<ContentType> getRootContentTypes() {
    return rootContentTypes.values();
  }

  public ContentType getContentType(String name) {
    for (ContentType contentType : rootContentTypes.values()) {
      ContentType ct = getContentTypeAux(contentType, name);
      if (ct != null) {
        return ct;
      }
    }
    return null;
  }

  protected ContentType getContentTypeAux(ContentType node, String name) {
    if (node.getName().equalsIgnoreCase(name))
      return node;

    for (ContentType child : node.getChildren().values()) {
      ContentType ct = getContentTypeAux(child, name);
      if (ct != null) {
        return ct;
      }
    }
    return null;
  }

  public ContentType contentTypeForDataSourceFunction(DataSourceFunction function) {
    switch (function) {
      case CreateTickets:
      case LearnTickets:
        return getContentType(CONTENT_TYPE_TICKET);
      case LearnIncidents:
      case CreateIncidents:
        return getContentType(CONTENT_TYPE_INCIDENT);
      case CreateProblems:
      case LearnProblems:
        return getContentType(CONTENT_TYPE_PROBLEM);
      case CreateChangeRequests:
      case LearnChangeRequests:
        return getContentType(CONTENT_TYPE_CHANGE_REQUEST);
      case LearnAlerts:
        return getContentType(CONTENT_TYPE_ALERT);
      case CreateRequests:
      case LearnRequests:
        return getContentType(CONTENT_TYPE_REQUEST);
      case CreateRequestItems:
      case LearnRequestItems:
        return getContentType(CONTENT_TYPE_REQUEST_ITEM);
      case LearnCmdbCi:
        return getContentType(CONTENT_TYPE_CMDB_CI);
      case LearnApplications:
        return getContentType(CONTENT_TYPE_APPLICATION);
      case LearnDevices:
        return getContentType(CONTENT_TYPE_DEVICE);
      case LearnServices:
        return getContentType(CONTENT_TYPE_SERVICE);
      case LearnUsers:
        return getContentType(CONTENT_TYPE_USER);
      case LearnUserGroups:
        return getContentType(CONTENT_TYPE_USER_GROUP);
      case LearnKB:
        return getContentType(CONTENT_TYPE_KNOWLEDGE_ARTICLE);
      case LearnServiceCatalogs:
        return getContentType(CONTENT_TYPE_SERVICE_CATALOG);
    }
    return null;
  }
}
