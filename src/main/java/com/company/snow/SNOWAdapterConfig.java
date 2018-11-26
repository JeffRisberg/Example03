package com.company.snow;

import com.aisera.connector.api.BaseAdapterConfig;
import com.company.api.BaseAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SNOWAdapterConfig extends BaseAdapterConfig {

  static final String TABLE_CHANGE_REQUEST = "change_request";
  static final String TABLE_INCIDENT = "incident";
  static final String TABLE_KB = "kb_knowledge";
  static final String TABLE_PROBLEM = "problem";
  static final String TABLE_SC_REQUEST = "sc_request";
  static final String TABLE_SC_REQUEST_ITEMS = "sc_req_item";
  static final String TABLE_CMDB_CI = "cmdb_ci";

  static final String TABLE_CATALOG = "catalogs";
  static final String TABLE_CATEGORY = "categories";
  static final String TABLE_ITEM = "items";

  static final String TABLE_USER = "sys_user";
  static final String TABLE_USER_GROUP = "sys_user_group";

  static final String SC_REQUEST_ITEMS_APPEND_KEY = "_items";
  static final String CMDB_CI_APPEND_KEY = "_cmdb_ci";
  static final String ASSIGNMENT_GROUP_APPEND_KEY = "_assignment_group";
  static final String ASSIGNED_TO_APPEND_KEY = "_user_assigned_to";
  static final String REPORTER_APPEND_KEY = "_user_reporter";

  private String catalogId;
  private String categoryId;
  private String itemId;
  private List<String> kbTitles;
  private String kbUrlPath;
  private String scUrlPath;
}
