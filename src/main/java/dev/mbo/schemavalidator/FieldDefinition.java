package dev.mbo.schemavalidator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldDefinition {
  private String type;
  private Map<String, FieldDefinition> nested;
  private Boolean nullable;
  private Boolean notBlank;
  private String pattern;
  private Long minValue;
  private Long maxValue;

  // override builder method
  public static class FieldDefinitionBuilder {

    public FieldDefinitionBuilder type(final String type) {
      if (null == type || type.isBlank()) {
        throw new IllegalArgumentException("invalid type: " + type);
      }
      this.type = type;
      return this;
    }

    public FieldDefinitionBuilder nullable(final boolean nullable) {
      this.nullable = nullable;
      return this;
    }

    public FieldDefinitionBuilder notBlank(final boolean notBlank) {
      this.notBlank = notBlank;
      if (notBlank == Boolean.TRUE) {
        this.nullable = false;
      }
      return this;
    }

    public FieldDefinitionBuilder pattern(final String pattern) {
      if (null == pattern || pattern.length() < 1) {
        throw new IllegalArgumentException("invalid pattern " + pattern);
      }
      this.pattern = pattern;
      return this;
    }

    public FieldDefinitionBuilder minValue(final long minValue) {
      this.minValue = minValue;
      return this;
    }

    public FieldDefinitionBuilder maxValue(final long maxValue) {
      this.maxValue = maxValue;
      return this;
    }
  }
}
