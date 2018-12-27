package com.company.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ContentType {

  protected String name;

  protected String protobufClassName;

  protected boolean creatable;
  protected boolean modifiable;

  @JsonIgnore
  protected ContentType parent;

  @JsonIgnore
  protected Map<String, ContentType> childContentTypes;

  public ContentType(String name) {
    this.name = name;
    this.childContentTypes = new HashMap<String, ContentType>();
  }

  public boolean hasAncestor(String name) {
    ContentType ct = this;

    while (ct != null) {
      if (ct.getName().equalsIgnoreCase(name)) {
        return true;
      }
      ct = ct.getParent();
    }
    return false;
  }

  public Map<String, ContentType> getChildren() {
    return childContentTypes;
  }

  public ContentType getChild(String name) {
    return childContentTypes.get(name);
  }

  public void addChild(ContentType childContentType) {
    this.childContentTypes.put(childContentType.getName(), childContentType);
  }
}
