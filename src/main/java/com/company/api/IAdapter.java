package com.company.api;

import com.company.common.ContentType;
import com.google.protobuf.Message;
import lombok.NonNull;

import java.util.function.Consumer;

/**
 * All Adapters will implement this.  The constructor for an implementation will take an
 * instance of BaseAdapterConfig.
 */
public interface IAdapter {

  /**
   * Get the configuration
   *
   * @return
   */
  BaseAdapterConfig getConfig();

  IAdapterResponse getTableEntries(IAdapterRequest request) throws Exception;

  IAdapterResponse getTableEntries(IAdapterRequest request, @NonNull Consumer consumer) throws Exception;

  /**
   * Get one entry and return it as a stream.
   *
   * @param request
   * @param key
   * @return
   * @throws Exception
   */
  IAdapterResponse getTableEntry(IAdapterRequest request, String key) throws Exception;

  /**
   * Create a table entry after applying a mapping
   *
   * @return
   * @throws Exception
   */
  IAdapterResponse createTableEntry(Message record, ContentType contentType, ISchemaMapping schemaMapping)
    throws Exception;

  /**
   * Update a table entry after applying a mapping
   *
   * @throws Exception
   */
  IAdapterResponse updateTableEntry(Message record, ContentType contentType, ISchemaMapping schemaMapping)
    throws Exception;
}
