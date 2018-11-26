package com.company.snow;

import com.company.api.*;
import com.company.common.ContentType;
import com.google.protobuf.Message;

import java.util.function.Consumer;

public class SNOWAdapter implements IAdapter {
  @Override
  public BaseAdapterConfig getConfig() {
    return null;
  }

  @Override
  public IAdapterResponse getTableEntries(IAdapterRequest request) throws Exception {
    return null;
  }

  @Override
  public IAdapterResponse getTableEntries(IAdapterRequest request, Consumer consumer) throws Exception {
    return null;
  }

  @Override
  public IAdapterResponse getTableEntry(IAdapterRequest request, String key) throws Exception {
    return null;
  }

  @Override
  public IAdapterResponse createTableEntry(Message record, ContentType contentType, ISchemaMapping schemaMapping) throws Exception {
    return null;
  }

  @Override
  public IAdapterResponse updateTableEntry(Message record, ContentType contentType, ISchemaMapping schemaMapping) throws Exception {
    return null;
  }
}
