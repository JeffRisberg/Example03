package com.company.api;

import com.company.common.ContentType;
import com.company.common.FilterDescription;
import com.company.common.SortDescription;

import java.util.Date;
import java.util.List;

public interface IAdapterRequest {
  ContentType getContentType();

  void setContentType(ContentType contentType);

  Date getStartDate();

  void setStartDate(Date startDate);

  Date getEndDate();

  void setEndDate(Date endDate);

  List<FilterDescription> getFilterDescList();

  void setFilterDescList(List<FilterDescription> filterDescList);

  void addFilterDescription(FilterDescription filterDescription);

  List<SortDescription> getSortDescList();

  void setSortDescList(List<SortDescription> sortDescList);

  void addSortDescription(SortDescription sortDesc);

  long getLimit();

  void setLimit(long limit);
}
