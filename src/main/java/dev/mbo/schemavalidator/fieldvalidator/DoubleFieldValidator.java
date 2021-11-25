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

public class DoubleFieldValidator implements FieldValidator<Double> {

  private static final Logger LOG = LoggerFactory.getLogger(DoubleFieldValidator.class);

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
    if (data instanceof Double) {
      final var value = (Double) data;
      if (fieldDefinition.getMinValue() != null && value < fieldDefinition.getMinValue()) {
        throw new IllegalStateException(value + " is smaller than min value of " + fieldDefinition.getMinValue());
      }
      if (fieldDefinition.getMaxValue() != null && value > fieldDefinition.getMaxValue()) {
        throw new IllegalStateException(value + " is bigger than max value of" + fieldDefinition.getMaxValue());
      }
    } else {
      throw new IllegalStateException(data + " is not an " + supportsType());
    }
  }

  @Override
  public Class<Double> supportsType() {
    return Double.class;
  }
}
