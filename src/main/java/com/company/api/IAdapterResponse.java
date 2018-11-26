package com.company.api;

import com.company.common.TicketContent;
import org.json.simple.JSONObject;

public interface IAdapterResponse {
  boolean isDone();

  RequestStatus getRequestStatus();

  String getStatusMessage();

  TicketContent getTicketContent();

  JSONObject getJSONObject();

  String getCreatedId();
}
