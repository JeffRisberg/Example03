package com.company.snow;

import com.company.api.IAdapter;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SNOWAdapter implements IAdapter {
  private static final String BEARER_TOKEN = "Bearer ";
  private static final String BASIC_TOKEN = "Basic ";

  @Getter
  private final SNOWAdapterConfig adapterConfig;

  private static Map<FilterOperator, String> operatorMap = new HashMap();

  static {
    operatorMap.put(FilterOperator.none, "");
    operatorMap.put(FilterOperator.eq, "=");
    operatorMap.put(FilterOperator.neq, "!=");
    operatorMap.put(FilterOperator.like, "like");
    operatorMap.put(FilterOperator.gt, ">");
    operatorMap.put(FilterOperator.gte, ">=");
    operatorMap.put(FilterOperator.lt, "<");
    operatorMap.put(FilterOperator.lte, "<=");
  }

  static Map<String, String> contentTypeToSnowTable = new HashMap<>();

  static {
    contentTypeToSnowTable.put(CONTENT_TYPE_INCIDENT, TABLE_INCIDENT);
    contentTypeToSnowTable.put(CONTENT_TYPE_PROBLEM, TABLE_PROBLEM);
    contentTypeToSnowTable.put(CONTENT_TYPE_CHANGE_REQUEST, TABLE_CHANGE_REQUEST);
    contentTypeToSnowTable.put(CONTENT_TYPE_REQUEST, TABLE_SC_REQUEST);
    contentTypeToSnowTable.put(CONTENT_TYPE_REQUEST_ITEM, TABLE_SC_REQUEST_ITEMS);

    contentTypeToSnowTable.put(CONTENT_TYPE_KNOWLEDGE_ARTICLE, TABLE_KB);

    contentTypeToSnowTable.put(CONTENT_TYPE_SERVICE_CATALOG, TABLE_CATALOG);
    contentTypeToSnowTable.put(CONTENT_TYPE_SERVICE_CATALOG_ITEM, TABLE_ITEM);
    contentTypeToSnowTable.put(CONTENT_TYPE_SERVICE_CATALOG_CATEGORY, TABLE_CATEGORY);

    contentTypeToSnowTable.put(CONTENT_TYPE_USER, TABLE_USER);
    contentTypeToSnowTable.put(CONTENT_TYPE_USER_GROUP, TABLE_USER_GROUP);

    contentTypeToSnowTable.put(CONTENT_TYPE_CMDB_CI, TABLE_CMDB_CI);
  }

  /**
   * Constructor
   */
  public SNOWAdapter(SNOWAdapterConfig adapterConfig) {
    this(adapterConfig, null);
  }

  public SNOWAdapter(SNOWAdapterConfig adapterConfig, Consumer consumer) {
    this.adapterConfig = adapterConfig;
  }

  @Override
  public BaseAdapterConfig getConfig() {
    return getAdapterConfig();
  }

  @Override
  public IAdapterResponse getTableEntries(IAdapterRequest request, Consumer consumer) {
    String contentTypeName = request.getContentType().getName();
    String tableName = contentTypeToSnowTable.get(contentTypeName);
    if (tableName == null) {
      BaseAdapterResponse failureResp = new BaseAdapterResponse();
      failureResp.setDone(true);
      failureResp.setRequestStatus(RequestStatus.Failed);
      failureResp.setStatusMessage("Invalid contentType " + contentTypeName);
      return failureResp;
    }

    log.info("Table name: " + tableName);
    log.info("Start date: " + request.getStartDate());
    log.info("End date: " + request.getEndDate());

    BaseAdapterResponse response = new BaseAdapterResponse();
    AtomicLong counter = new AtomicLong();
    try {
      String nextUrlStr = null;
      do {
        Pair<Optional<JSONObject>, String> resp;
        resp = getTableEntriesIncrementally(tableName, nextUrlStr, request);
        nextUrlStr = null;
        if (resp.getLeft().isPresent()) {
          response.setJSONObject(resp.getLeft().get());
          JSONArray entries;
          if (resp.getLeft().get().get("result") instanceof JSONObject) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(resp.getLeft().get().get("result"));
            entries = jsonArray;
          } else {
            entries = (JSONArray) resp.getLeft().get().get("result");
          }
          // TODO: to be removed when all secondary entities are read separatelly
          if (contentTypeName.equals(CONTENT_TYPE_INCIDENT)) {
            fetchAndJoinCommentsForEntries(entries);
          }
          if (request.getContentType().hasAncestor(CONTENT_TYPE_TICKET)) {
            fetchAndJoinCMDB(entries);
            fetchAndJoinAssignmentGroups(entries);
            fetchAndJoinUsers(entries);
          }
          if (contentTypeName.equals(CONTENT_TYPE_REQUEST)) {
            fetchAndJoinItemsForRequests(entries);
          }

          if (request.getLimit() > 0 && counter.get() >= request.getLimit()) {
            // stop paginating. SNOW API uses limit as limit per page, not total.
            break;
          }

          if (consumer != null) {
            counter.incrementAndGet();
            entries.forEach(consumer::accept);
          }
          nextUrlStr = resp.getRight();
        }
      } while (nextUrlStr != null);
    } catch (Exception e) {
      log.error("Exception in SNOWAdapter", e);
      BaseAdapterResponse exceptionResponse = new BaseAdapterResponse();
      exceptionResponse.setDone(true);
      exceptionResponse.setRequestStatus(RequestStatus.Failed);
      exceptionResponse.setStatusMessage(e.getMessage());
      return exceptionResponse;
    }

    response.setDone(true);
    return response;
  }

  @Override
  public IAdapterResponse getTableEntries(IAdapterRequest request) {
    return getTableEntries(request, null);
  }

  @Override
  public IAdapterResponse getTableEntry(IAdapterRequest request, String key) {
    String contentTypeName = request.getContentType().getName();
    String tableName = contentTypeToSnowTable.get(contentTypeName);
    if (tableName == null) {
      return BaseAdapterResponse.failureResponse("Invalid contentType " + contentTypeName);
    }

    switch (contentTypeName) {
      case CONTENT_TYPE_SERVICE_CATALOG:
      case CONTENT_TYPE_SERVICE_CATALOG_CATEGORY:
      case CONTENT_TYPE_SERVICE_CATALOG_ITEM:
        return getScSingleTableEntry(tableName, key);
      default:
        return getSingleTableEntry(tableName, key);
    }
  }

  IAdapterResponse executeRequest(HttpRequestBase httpRequest) {
    return executePaginatedRequest(httpRequest).getLeft();
  }

  private Pair<IAdapterResponse, String> executePaginatedRequest(HttpRequestBase httpRequest) {
    if (this.getAdapterConfig().getAccessToken() != null)
      httpRequest.setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN + this.getAdapterConfig().getAccessToken());
    else {
      String userAndPswd = getAdapterConfig().getUsername() + ":" + getAdapterConfig().getPassword();
      byte[] bytesUserPswd = userAndPswd.getBytes(StandardCharsets.UTF_8);
      String headerBase64 = Base64.getEncoder().encodeToString(bytesUserPswd);
      httpRequest.setHeader(HttpHeaders.AUTHORIZATION, BASIC_TOKEN + headerBase64);
    }
    httpRequest.setHeader(HttpHeaders.ACCEPT, "application/json");
    httpRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

    String errorStr = null;
    try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
      CloseableHttpResponse httpResponse = httpclient.execute(httpRequest);
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
        JSONObject topObject = getJsonObjectFromResponse(httpResponse);
        String nextLink = getNextLinkFromHeaders(httpResponse);
        return new ImmutablePair<>(BaseAdapterResponse.successResponse(topObject, null), nextLink);
      } else {
        errorStr = "HTTP error: " + statusCode + EntityUtils.toString(httpResponse.getEntity());
        log.error(errorStr);
      }

    } catch (IOException e) {
      errorStr += "Failed to autoclose CloseableHttpClient in SNOWAdapter " + e.getMessage();
      log.error("Failed to autoclose CloseableHttpClient in SNOWAdapter", e);
    } catch (Exception e) {
      errorStr += "Exception in SNOWAdapter " +  e.getMessage();
      log.error("Exception in SNOWAdapter", e);
    }

    return new ImmutablePair<>(BaseAdapterResponse.failureResponse(errorStr), null);
  }

  private IAdapterResponse getSingleTableEntry(String api, String snowTable, String id) {
    String incidentsUri = getAdapterConfig().getUri() + "/api/" + api + "/" + snowTable + "/" + id;
    HttpGet incidentsRequest = new HttpGet(incidentsUri);
    return executeRequest(incidentsRequest);
  }

  IAdapterResponse getSingleTableEntry(String snowTable, String id) {
    return getSingleTableEntry("now/table", snowTable, id);
  }

  private IAdapterResponse getScSingleTableEntry(String snowTable, String id) {
    return getSingleTableEntry("sn_sc/servicecatalog", snowTable, id);
  }

  @Override
  public IAdapterResponse createTableEntry(Message record, ContentType contentType, ISchemaMapping schemaMapping) {
    if (!contentType.isCreatable()) {
      return BaseAdapterResponse.failureResponse("Content type " + contentType.getName() + " is not creatable");
    }
    String snowTable = contentTypeToSnowTable.get(contentType.getName());
    if (snowTable == null) {
      return BaseAdapterResponse.failureResponse("Unsupported content type: " + contentType.getName());
    }

    // TODO: assuming ticket content for now
    TicketContent ticketContent = (TicketContent) record;

    SNOWTicketReverseMapper reverseMapper = new SNOWTicketReverseMapper(contentType, schemaMapping);
    JSONObject jsonToCreate = reverseMapper.convert(ticketContent);

    log.info("Creation payload: " + jsonToCreate.toJSONString());

    String incidentsUri = getAdapterConfig().getUri() + "/api/now/table/" + snowTable;
    HttpPost createRequest = new HttpPost(incidentsUri);
    String jsonString = jsonToCreate.toJSONString();
    StringEntity postingString;

    try {
      postingString = new StringEntity(jsonString);
    } catch (UnsupportedEncodingException e) {
      log.error("Failed to encode JSON", e);
      return BaseAdapterResponse.failureResponse("Failed to encode JSON " + e.getMessage());
    }

    createRequest.setEntity(postingString);

    BaseAdapterResponse adapterResponse = (BaseAdapterResponse) executeRequest(createRequest);

    if (adapterResponse.getRequestStatus().equals(RequestStatus.Succeeded) &&
      adapterResponse.getJSONObject() != null) {
      SNOWMapper mapper = new SNOWMapper(getAdapterConfig(), contentType, schemaMapping);
      // TODO: what would be a better way to unwrap the json in key "result" the same way everywhere?
      // maybe a JSON to JSON mapper that is always chained to the adapter.
      JSONObject jsonObject = (JSONObject) adapterResponse.getJSONObject().get("result");
      TicketContent resultTicketContent = (TicketContent) mapper.convert(jsonObject);

      adapterResponse.setTicketContent(resultTicketContent);
      adapterResponse.setCreatedId(ticketContent.getId());

      // Autodesk-specific side effects
      // TODO: create using contentType
      if (contentType.getName().equals(CONTENT_TYPE_REQUEST)) {
        // AM-2243 also create a Request Item
        jsonToCreate.put("request", resultTicketContent.getId()); // associate Request Item with Request that just got created
        getGenericServiceRequestId().ifPresent(catalogItem -> jsonToCreate.put("cat_item", catalogItem));
        Optional<JSONObject> itemResult = createTableEntry(TABLE_SC_REQUEST_ITEMS, jsonToCreate.toJSONString());
        // Autodesk specific: Link to default catalog item. This code will not do anything on other instances.
        if (!itemResult.isPresent()) {
          log.error("Failed to create SNOW Request Item for Request {}", resultTicketContent.getId());
        }
      }
    }

    return adapterResponse;
  }

  @Override
  public IAdapterResponse updateTableEntry(Message record, ContentType contentType, ISchemaMapping schemaMapping) {
    if (!contentType.isModifiable()) {
      return BaseAdapterResponse.failureResponse("Content type " + contentType.getName() + " is not modifiable");
    }
    String snowTable = contentTypeToSnowTable.get(contentType.getName());
    if (snowTable == null) {
      return BaseAdapterResponse.failureResponse("Unsupported content type: " + contentType.getName());
    }
    // TODO: assuming ticket content for now
    TicketContent ticketContent = (TicketContent) record;
    ProtoToJsonMapper ticketToJsonMapper = new ProtoToJsonMapper(null);
    JSONObject jsonToEdit = ticketToJsonMapper.map(ticketContent, schemaMapping);
    log.info("Update payload", jsonToEdit.toJSONString());

    String incidentsUri = getAdapterConfig().getUri() + "/api/now/table/" + snowTable + "/" + ticketContent.getId();
    HttpPut updateRequest = new HttpPut(incidentsUri);
    String jsonString = jsonToEdit.toJSONString();
    StringEntity postingString;
    try {
      postingString = new StringEntity(jsonString);
    } catch (UnsupportedEncodingException e) {
      log.error("Failed to encode JSON", e);
      return BaseAdapterResponse.failureResponse("Failed to encode JSON " + e.getMessage());
    }

    updateRequest.setEntity(postingString);
    return executeRequest(updateRequest);
  }

  Pair<Optional<JSONObject>, String> getTableEntriesIncrementally(String tableName, String nextUrlStr,
                                                                  IAdapterRequest request) throws Exception {

    String recordsUri;
    if (nextUrlStr == null || nextUrlStr.isEmpty()) {
      recordsUri = getAdapterConfig().getUri() + "/api/now/table/" + tableName;
      if (tableName.equals(TABLE_CATALOG) || tableName.equals(TABLE_CATEGORY) || tableName.equals(TABLE_ITEM)) {
        recordsUri = getAdapterConfig().getUri() + "/api/sn_sc/servicecatalog/" + tableName;
        // extra params
        if (tableName.equals(TABLE_CATEGORY)) {
          if (getAdapterConfig().getCatalogId() != null) {
            recordsUri = getAdapterConfig().getUri() + "/api/sn_sc/servicecatalog/catalogs" + "/" + getAdapterConfig().getCatalogId() + "/categories";
          } else {
            return new ImmutablePair<>(Optional.empty(), null); // Nothing to do
          }
        } else if (tableName.equals(TABLE_ITEM)) {
          if (getAdapterConfig().getCategoryId() != null) {
            recordsUri = recordsUri + "?sysparm_category=" + getAdapterConfig().getCategoryId();
          } else if (getAdapterConfig().getItemId() != null) {
            recordsUri = recordsUri + "/" + getAdapterConfig().getItemId();
          }
        }
      }
    } else {
      recordsUri = nextUrlStr;
    }
    HttpGet recordsRequest = new HttpGet(recordsUri);
    URIBuilder uriBuilder = new URIBuilder(recordsRequest.getURI());
    buildIncrementalUri(nextUrlStr, request, uriBuilder);

    // TODO: remove, make part of adapter request filter options
    if (tableName.equals(TABLE_SC_REQUEST_ITEMS) && nextUrlStr == null) { // no need to add params to a pagination URL
      // if fetching request items, only fetch those that do not belong to a Request.
      String existingParam = "";
      for (NameValuePair pair : uriBuilder.getQueryParams()) {
        if (pair.getName().equals("sysparm_query")) {
          existingParam = pair.getValue();
        }
        existingParam = "^" + existingParam;
      }
      uriBuilder.addParameter("sysparm_query", existingParam + "requestISEMPTY");
    }

    URI uri = uriBuilder.build();
    recordsRequest.setURI(uri);
    Pair<IAdapterResponse, String> pair = executePaginatedRequest(recordsRequest);
    return new ImmutablePair<>(Optional.ofNullable(pair.getLeft().getJSONObject()), pair.getRight());
  }


  /** This is public so it can be tested */
  public void buildIncrementalUri(String nextUrlStr, IAdapterRequest request, URIBuilder uriBuilder) {
    StringBuffer whereClause = new StringBuffer();

    if (nextUrlStr == null) { // the query will already be part of the url when paginating

      if (request.getLimit() > 0) {
        uriBuilder.addParameter("sysparm_limit", "" + request.getLimit());
      } else {
        uriBuilder.addParameter("sysparm_limit", "200");
      }

      if (request.getFilterDescList() != null) {
        for (FilterDescription filterDesc : request.getFilterDescList()) {
          String operator = operatorMap.get(filterDesc.getOperator());

          if (whereClause.length() > 0) {
            whereClause.append("^");
          }
          whereClause.append(filterDesc.getField()).append(operator).append(filterDesc.getValue());
        }
      }

      if (request.getSortDescList() != null) {
        for (SortDescription sortDesc : request.getSortDescList()) {
          boolean isDesc = sortDesc.getDirection() == SortDirection.desc;
          String prefix = isDesc ? "ORDERBYDESC" : "ORDERBY";

          if (whereClause.length() > 0) {
            whereClause.append("^");
          }
          whereClause.append(prefix).append(sortDesc.getField());
        }
      }

      if (request.getStartDate() != null || request.getEndDate() != null) {
        // TODO: find proper solution to timezone hack
        long millis;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String dateString = null;
        String timeString = null;
        if (request.getStartDate() != null) {
          millis = request.getStartDate().getTime();
          // go back eight hours
          millis -= 8 * 60 * 60 * 1000L;
          request.setStartDate(new Date(millis));

          dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
          dateString = "'" + dateFormat.format(request.getStartDate()) + "'";

          timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
          timeString = "'" + timeFormat.format(request.getStartDate()) + "'";
        }

        String endDateString = null;
        String endTimeString = null;
        if (request.getEndDate() != null) {
          millis = request.getEndDate().getTime();
          millis -= 8 * 60 * 60 * 1000L;
          request.setEndDate(new Date(millis));
          endDateString = "'" + dateFormat.format(request.getEndDate()) + "'";
          endTimeString = "'" + timeFormat.format(request.getEndDate()) + "'";
          if (request.getStartDate() != null && request.getEndDate().before(request.getStartDate())) {
            request.setEndDate(null);
          }
        }

        String dateQuery = null;

        if (request.getStartDate() != null && request.getEndDate() != null) {
          dateQuery = "sys_updated_onBETWEENjavascript:gs.dateGenerate(" + dateString + "," + timeString +
            ")@javascript:gs.dateGenerate(" + endDateString + "," + endTimeString + ")";
        } else if (request.getStartDate() != null) {
          dateQuery = "sys_updated_on>=javascript:gs.dateGenerate(" + dateString + "," + timeString + ")";
        } else if (request.getEndDate() != null) {
          dateQuery = "sys_updated_on<=javascript:gs.dateGenerate(" + endDateString + "," + endTimeString + ")";
        }

        if (dateQuery != null) {
          if (whereClause.length() > 0) {
            whereClause.append("^");
          }
          whereClause.append(dateQuery);
        }
      }
      uriBuilder.addParameter("sysparm_query", whereClause.toString());
      log.info("whereClause {}", whereClause);
    }
  }

  private String getNextLinkFromHeaders(CloseableHttpResponse response) {
    /*
    Pagination links are in the links header eg
    Link <https://dev41045.service-now.com/api/now/table/incident?sysparm_limit=1&sysparm_offset=0>;rel="first",
         <https://dev41045.service-now.com/api/now/table/incident?sysparm_limit=1&sysparm_offset=-1>;rel="prev",
         <https://dev41045.service-now.com/api/now/table/incident?sysparm_limit=1&sysparm_offset=1>;rel="next",
         <https://dev41045.service-now.com/api/now/table/incident?sysparm_limit=1&sysparm_offset=53>;rel="last"
     */
    Header linkHeader = response.getFirstHeader("Link");
    String nextLink = null;
    if (linkHeader != null) { // if null, pagination has ended
      for (HeaderElement element : linkHeader.getElements()) {
        NameValuePair[] pairs = element.getParameters();
        for (int i = 0; i < pairs.length; ++i) {
          NameValuePair pair = pairs[i];
          if (pair.getValue().equals("next")) {
            String elementStr = element.toString();
            String regex = "[^<>]+";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(elementStr);
            if (matcher.find()) {
              nextLink = matcher.group(0);
              if (!nextLink.startsWith("http")) {
                nextLink = getAdapterConfig().getUri() + nextLink;
              }
            }
            break;
          }
        }
      }
    }
    return nextLink;
  }

  private JSONObject getJsonObjectFromResponse(CloseableHttpResponse response) throws IOException, ParseException {
    String jsonStr = EntityUtils.toString(response.getEntity());
    response.close();
    JSONParser parser = new JSONParser();
    return (JSONObject) parser.parse(jsonStr);
  }

  // TODO: eventually remove everything under here
  Pair<Optional<JSONObject>, String> getRelatedEntriesIncrementally(String table, String filterField,
                                                                    List<String> entryIds,
                                                                    Optional<Map<String, String>> extraParams,
                                                                    String nextUrlStr) {
    if (entryIds == null || entryIds.isEmpty()) {
      return new ImmutablePair<>(Optional.empty(), null);
    }
    CredentialsProvider provider = new BasicCredentialsProvider();
    UsernamePasswordCredentials credentials
      = new UsernamePasswordCredentials(getAdapterConfig().getUsername(), getAdapterConfig().getPassword());
    provider.setCredentials(AuthScope.ANY, credentials);

    try (CloseableHttpClient httpclient = HttpClientBuilder.create()
      .setDefaultCredentialsProvider(provider)
      .build()) {
      String uriStr;
      if (nextUrlStr == null || nextUrlStr.isEmpty()) {
        uriStr = getAdapterConfig().getUri() + "/api/now/table/" + table;
      } else {
        uriStr = nextUrlStr;
      }
      HttpGet relatedEntitiesRequest = new HttpGet(uriStr);

      if (nextUrlStr == null) { // if paginating, the getAdapterConfig() are already set
        StringBuilder idQueryBuilder = new StringBuilder(entryIds.size() * 50);
        idQueryBuilder.append(filterField);
        idQueryBuilder.append("IN");
        entryIds.forEach(entryId -> idQueryBuilder.append(entryId).append(","));
        idQueryBuilder.deleteCharAt(idQueryBuilder.length() - 1); // remove extra comma at the end

        URIBuilder uriBuilder = new URIBuilder(relatedEntitiesRequest.getURI())
          .addParameter("sysparm_limit", "10000")
          .addParameter("sysparm_query", idQueryBuilder.toString());
        extraParams.ifPresent(paramsMap -> paramsMap.forEach((key, value) -> uriBuilder.addParameter(key, value)));
        URI uri = uriBuilder.build();
        relatedEntitiesRequest.setURI(uri);
      }

      CloseableHttpResponse response = httpclient.execute(relatedEntitiesRequest);
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        JSONObject topObject = getJsonObjectFromResponse(response);
        String nextLink = getNextLinkFromHeaders(response);
        return new ImmutablePair<>(Optional.of(topObject), nextLink);
      } else {
        log.error("HTTP error: {} {}", statusCode, EntityUtils.toString(response.getEntity()));
      }

    } catch (IOException e) {
      log.error("Failed to autoclose CloseableHttpClient in SNOWClient", e);
    } catch (Exception e) {
      log.error("Exception in SNOWClient", e);
    }

    return new ImmutablePair<>(Optional.empty(), null);
  }

  Pair<Optional<JSONObject>, String> getRequestItemsEntriesIncrementally(List<String> entryIds, String nextUrlStr) {
    return getRelatedEntriesIncrementally(TABLE_SC_REQUEST_ITEMS, "request", entryIds, Optional.empty(), nextUrlStr);
  }

  Pair<Optional<JSONObject>, String> getCommentEntriesIncrementally(List<String> entryIds, String nextUrlStr) {
    HashMap<String, String> extraParams = new HashMap<>();
    extraParams.put("element", "comments");
    return getRelatedEntriesIncrementally("sys_journal_field", "element_id", entryIds,
      Optional.of(extraParams), nextUrlStr);
  }

  Pair<Optional<JSONObject>, String> getCMDBEntriesIncrementally(List<String> entryIds, String nextUrlStr) {
    return getRelatedEntriesIncrementally(TABLE_CMDB_CI, "sys_id", entryIds, Optional.empty(), nextUrlStr);
  }

  Pair<Optional<JSONObject>, String> getUserGroupEntriesIncrementally(List<String> entryIds, String nextUrlStr) {
    return getRelatedEntriesIncrementally(TABLE_USER_GROUP, "sys_id", entryIds, Optional.empty(), nextUrlStr);
  }

  Pair<Optional<JSONObject>, String> getUserEntriesIncrementally(List<String> entryIds, String nextUrlStr) {
    return getRelatedEntriesIncrementally(TABLE_USER, "sys_id", entryIds, Optional.empty(), nextUrlStr);
  }

  Optional<JSONObject> createTableEntry(String snowTable, String jsonStr) {

    String incidentsUri = getAdapterConfig().getUri() + "/api/now/table/" + snowTable;
    HttpPost createRequest = new HttpPost(incidentsUri);
    StringEntity postingString;
    try {
      postingString = new StringEntity(jsonStr);
    } catch (UnsupportedEncodingException e) {
      log.error("Failed to update entity", e);
      return Optional.empty();
    }
    createRequest.setEntity(postingString);
    IAdapterResponse adapterResponse = executeRequest(createRequest);
    return Optional.ofNullable(adapterResponse.getJSONObject());
  }

  Optional<JSONObject> updateTableEntry(String snowTable, String sys_id, String jsonStr) {
    String incidentsUri = getAdapterConfig().getUri() + "/api/now/table/" + snowTable + "/" + sys_id;
    HttpPut updateRequest = new HttpPut(incidentsUri);
    StringEntity postingString;
    try {
      postingString = new StringEntity(jsonStr);
    } catch (UnsupportedEncodingException e) {
      log.error("Failed to update entity", e);
      return Optional.empty();
    }
    updateRequest.setEntity(postingString);
    IAdapterResponse adapterResponse = executeRequest(updateRequest);
    return Optional.ofNullable(adapterResponse.getJSONObject());
  }

  // Autodesk specific
  Optional<String> getGenericServiceRequestId() {
    CredentialsProvider provider = new BasicCredentialsProvider();
    UsernamePasswordCredentials credentials
      = new UsernamePasswordCredentials(getAdapterConfig().getUsername(), getAdapterConfig().getPassword());
    provider.setCredentials(AuthScope.ANY, credentials);

    int statusCode = 0;
    try (CloseableHttpClient httpclient = HttpClientBuilder.create()
      .setDefaultCredentialsProvider(provider)
      .build()) {

      String uriStr = getAdapterConfig().getUri() + "/api/now/table/" + "sc_cat_item";
      HttpGet recordsRequest = new HttpGet(uriStr);

      URIBuilder uriBuilder = new URIBuilder(recordsRequest.getURI());
      uriBuilder.addParameter("sysparm_query", "name=Generic Service Request (I need...)");

      URI uri = uriBuilder.build();

      recordsRequest.setURI(uri);
      CloseableHttpResponse response = httpclient.execute(recordsRequest);
      statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        JSONObject topObject = getJsonObjectFromResponse(response);
        JSONArray results = (JSONArray) topObject.get("result");
        if (results.size() > 0) {
          JSONObject categoryItem = (JSONObject) results.get(0);
          String catalogItemSysId = (String) categoryItem.get("sys_id");
          return Optional.of(catalogItemSysId);
        }
      } else {
        log.error("HTTP error: {} {}", statusCode, EntityUtils.toString(response.getEntity()));
      }

    } catch (IOException e) {
      log.error("Failed to autoclose CloseableHttpClient in SNOWClient", e);
    } catch (Exception e) {
      log.error("Exception in SNOWClient", e);
    }

    return Optional.empty();
  }

  // Secondary entities
  // TODO: to be removed

  /**
   * works by mutating the JSONObjects contained in entries
   * @param entries a JSONArray of existing entries, eg incidents
   * @param entriesIdKey the JSON key containing the id of the entries
   * @param fetchedEntriesIdKey the JSON key containing the id to join on the fetched related entries,
   *                            eg id in comments pointing to parent incident
   * @param entriesAppendKey JSON key to append the fetched related entries, eg mutate incidents JSON by adding a "_comments" entry
   * @param fetchMethod method to fetch related entity
   */
  void fetchAndJoinEntries(JSONArray entries, String entriesIdKey, String fetchedEntriesIdKey,
                           String entriesAppendKey,
                           BiFunction<List<String>, String, Pair<Optional<JSONObject>, String>> fetchMethod) {
    ArrayList<String> entryIds = new ArrayList<>();
    HashMap<String, JSONArray> commentsMap = new HashMap<>();

    // special handling of incident comments: get all ids to fetch comments in one call
    // perform a hash join of comments JSON and incidents JSON
    entries.forEach(entry -> {
      String entryId = "";
      Object fetchedEntriesIdObject = ((JSONObject) entry).get(entriesIdKey);
      if (fetchedEntriesIdObject instanceof String) {
        entryId = (String) fetchedEntriesIdObject;
      } else if (fetchedEntriesIdObject instanceof JSONObject
        && ((JSONObject)fetchedEntriesIdObject).get("value") != null) {
        // special case for Requests and Request items and CMDB. Request item request id is in JSON path $request.value
        // In the future we must implement a full-blown JSONPath solution if more cases use that
        entryId = (String) ((JSONObject)fetchedEntriesIdObject).get("value");
      }
      entryIds.add(entryId);
    });
    AtomicLong commentCounter = new AtomicLong(0);
    String nextCommentUrl = null;
    do {
      Pair<Optional<JSONObject>, String> commentResponse = fetchMethod.apply(entryIds, nextCommentUrl);
      nextCommentUrl = null;
      if (commentResponse.getLeft().isPresent()) {
        JSONArray secondaryEntries = (JSONArray) commentResponse.getLeft().get().get("result");
        secondaryEntries.forEach(secondaryEntry -> {
          Object fetchedEntriesIdObject = ((JSONObject) secondaryEntry).get(fetchedEntriesIdKey);
          String incidentId = "";
          if (fetchedEntriesIdObject instanceof String) {
            incidentId = (String) fetchedEntriesIdObject;
          } else if (fetchedEntriesIdObject instanceof JSONObject
            && ((JSONObject)fetchedEntriesIdObject).get("value") != null) {
            // special case for Requests and Request items. Request item request id is in JSON path $request.value
            // In the future we must implement a full-blown JSONPath solution if more cases use that
            incidentId = (String) ((JSONObject)fetchedEntriesIdObject).get("value");
          }
          if (commentsMap.get(incidentId) == null) { // init the hash map, one bucket per incident sys_id
            commentsMap.put(incidentId, new JSONArray());
          }
          JSONArray comments = commentsMap.get(incidentId);
          comments.add(secondaryEntry);
          commentCounter.incrementAndGet();
        });
        nextCommentUrl = commentResponse.getRight();
      }
    } while (nextCommentUrl != null);
    if (commentCounter.get() > 0) {
      log.info("Read ({}) {} for entry ids {}", commentCounter.get(), entriesAppendKey, entryIds);
    }

    entries.forEach(entry -> {
      String entryId = "";
      Object fetchedEntriesIdObject = ((JSONObject) entry).get(entriesIdKey);
      if (fetchedEntriesIdObject instanceof String) {
        entryId = (String) fetchedEntriesIdObject;
      } else if (fetchedEntriesIdObject instanceof JSONObject
        && ((JSONObject)fetchedEntriesIdObject).get("value") != null) {
        // special case for Requests and Request items and CMDB. Request item request id is in JSON path $request.value
        // In the future we must implement a full-blown JSONPath solution if more cases use that
        entryId = (String) ((JSONObject)fetchedEntriesIdObject).get("value");
      }
      JSONArray comments = commentsMap.get(entryId);
      if (comments == null) return; // continue
      if (comments.size() > 0) {
        log.info("Read ({}) {} for entry id {}", comments.size(), entriesAppendKey, entryId);
      }
      ((JSONObject) entry).put(entriesAppendKey, comments);
    });
  }

  void fetchAndJoinCommentsForEntries(JSONArray entries) {
    fetchAndJoinEntries(entries, "sys_id", "element_id", "_comments",
      this::getCommentEntriesIncrementally);
  }

  private void fetchAndJoinItemsForRequests(JSONArray entries) {
    // Request id of item is in JSONPath "$request.value". For now custom logic will check for this case.
    fetchAndJoinEntries(entries, "sys_id", "request", SC_REQUEST_ITEMS_APPEND_KEY,
      this::getRequestItemsEntriesIncrementally);
  }

  private void fetchAndJoinCMDB(JSONArray entries) {
    // Request id of item is in JSONPath "$request.value". For now custom logic will check for this case.
    fetchAndJoinEntries(entries, "cmdb_ci", "sys_id", CMDB_CI_APPEND_KEY,
      this::getCMDBEntriesIncrementally);
  }

  private void fetchAndJoinAssignmentGroups(JSONArray entries) {
    // Request id of item is in JSONPath "$request.value". For now custom logic will check for this case.
    fetchAndJoinEntries(entries, "assignment_group", "sys_id", ASSIGNMENT_GROUP_APPEND_KEY,
      this::getUserGroupEntriesIncrementally);
  }

  private void fetchAndJoinUsers(JSONArray entries) {
    // Request id of item is in JSONPath "$request.value". For now custom logic will check for this case.
    fetchAndJoinEntries(entries, "assigned_to", "sys_id", ASSIGNED_TO_APPEND_KEY,
      this::getUserEntriesIncrementally);
    fetchAndJoinEntries(entries, "caller_id", "sys_id", REPORTER_APPEND_KEY,
      this::getUserEntriesIncrementally);
  }
}
