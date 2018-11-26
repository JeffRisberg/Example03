package com.company.api;

import com.company.common.TicketContent;
import lombok.Data;
import org.json.simple.JSONObject;

@Data
public class BaseAdapterResponse implements IAdapterResponse {
  protected boolean done;
  protected RequestStatus requestStatus;
  protected String statusMessage;
  protected JSONObject jsonObject;
  protected TicketContent ticketContent;
  protected String createdId;

  // Needed because Lombok doesn't generate correct capitalization.
  public JSONObject getJSONObject() {
    return jsonObject;
  }

  public void setJSONObject(JSONObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  public static BaseAdapterResponse failureResponse(String message) {
    BaseAdapterResponse failureResp = new BaseAdapterResponse();
    failureResp.setDone(true);
    failureResp.setRequestStatus(RequestStatus.Failed);
    failureResp.setStatusMessage(message);
    return failureResp;
  }

  public static BaseAdapterResponse successResponse(JSONObject jsonObject, String message) {
    BaseAdapterResponse response = new BaseAdapterResponse();
    response.setDone(true);
    response.setRequestStatus(RequestStatus.Succeeded);
    response.setStatusMessage(message);
    response.setJSONObject(jsonObject);
    return response;
  }
}
