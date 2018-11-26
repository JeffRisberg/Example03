package com.company.api;

import com.company.common.ContentType;
import com.company.common.FilterDescription;
import com.company.common.SortDescription;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Data
public class BaseAdapterRequest implements IAdapterRequest {
  protected ContentType contentType;
  protected Date startDate;
  protected Date endDate;

  protected List<FilterDescription> filterDescList = new ArrayList<>();
  protected List<SortDescription> sortDescList = new ArrayList<>();

  protected long limit;

  public BaseAdapterRequest() {
  }

  public BaseAdapterRequest(ContentType contentType) {
    this.contentType = contentType;
  }

  public BaseAdapterRequest(ContentType contentType, Date startDate, Date endDate) {
    this(contentType);

    this.startDate = startDate;
    this.endDate = endDate;
  }

  public BaseAdapterRequest(ContentType contentType, String startDateStr, String endDateStr) throws Exception {
    this(contentType);

    try {
      String pattern = "yyyy-MM-dd";
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

      if (startDateStr != null) {
        this.startDate = simpleDateFormat.parse(startDateStr);
      }
      if (endDateStr != null) {
        this.endDate = simpleDateFormat.parse(endDateStr);
      }
    } catch (ParseException e) {
      throw new RuntimeException("format error");
    }
  }

  @Override
  public void addFilterDescription(FilterDescription filterDescription) {
    filterDescList.add(filterDescription);
  }

  @Override
  public void addSortDescription(SortDescription sortDesc) {
    sortDescList.add(sortDesc);
  }
}
