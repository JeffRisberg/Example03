package com.company.api;

import lombok.Data;

@Data
public class BaseAdapterConfig {
  public static final int DEFAULT_TIMEOUT = 60000;
  public static final int RESPONSE_TIMEOUT = 50000;

  protected String uri;
  protected String username; // not needed if accessToken is provided
  protected String password; // not needed if accessToken is provided
  protected String accessToken; // if non-null, then use this Oauth token, instead of user/password

  protected boolean appendCustomFields;

  protected boolean isDebugMode = false;
}
