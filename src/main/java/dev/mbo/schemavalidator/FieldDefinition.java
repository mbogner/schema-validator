/*
 * Copyright 2021 mbo.dev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
