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

package dev.mbo.schemavalidator.fieldvalidator;

import dev.mbo.schemavalidator.FieldDefinition;
import dev.mbo.schemavalidator.FieldValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringFieldValidator implements FieldValidator<String> {

  private static final Logger LOG = LoggerFactory.getLogger(StringFieldValidator.class);

  @Override
  public void validate(
    final String key,
    final Object data,
    final FieldDefinition fieldDefinition
  ) {
    LOG.debug("validate {}::{}: {}", key, fieldDefinition.getType(), data);
    if(fieldDefinition.getNullable() == Boolean.TRUE && null == data) {
      return;
    }
    if (data instanceof String) {
      final var value = (String) data;
      if (fieldDefinition.getNotBlank() != null && value.isBlank()) {
        throw new IllegalStateException("value of " + key + " must not be blank");
      }
      if (fieldDefinition.getPattern() != null && !value.matches(fieldDefinition.getPattern())) {
        throw new IllegalStateException(value + "value of " + key + " doesn not match pattern " + fieldDefinition.getPattern());
      }
    } else {
      throw new IllegalStateException(data + " is not an " + supportsType());
    }
  }

  @Override
  public Class<String> supportsType() {
    return String.class;
  }
}
