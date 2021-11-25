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

import dev.mbo.schemavalidator.fieldvalidator.BigDecimalFieldValidator;
import dev.mbo.schemavalidator.fieldvalidator.DoubleFieldValidator;
import dev.mbo.schemavalidator.fieldvalidator.IntegerFieldValidator;
import dev.mbo.schemavalidator.fieldvalidator.LongFieldValidator;
import dev.mbo.schemavalidator.fieldvalidator.StringFieldValidator;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.Map;

public class ValidateSchemaValidator implements ConstraintValidator<ValidateSchema, Object> {

  private static final Logger LOG = LoggerFactory.getLogger(ValidateSchemaValidator.class);

  public static final FieldValidator<?>[] DEFAULT_FIELD_VALIDATORS = {
    new StringFieldValidator(),
    new IntegerFieldValidator(),
    new DoubleFieldValidator(),
    new BigDecimalFieldValidator(),
    new LongFieldValidator()
  };
  private static Map<String, FieldValidator<?>> VALIDATOR_MAP = null;

  /**
   * Call this method in application setup phase to override all default validator.
   *
   * @param validators Custom validators to override the default list. If this array contains at least one object this
   *                   replaces ALL default validators. You can access the default validators via
   *                   <code>DEFAULT_FIELD_VALIDATORS</code>.
   */
  public static void initValidators(final FieldValidator<?>... validators) {
    if (null == VALIDATOR_MAP) {
      LOG.debug("init validators");
      if (null == validators || validators.length < 1) {
        LOG.debug("using default validators");
        initValidatorsShared(DEFAULT_FIELD_VALIDATORS);
      } else {
        LOG.debug("using provided validators");
        initValidatorsShared(validators);
      }
    }
  }

  private static void initValidatorsShared(final FieldValidator<?>[] validators) {
    VALIDATOR_MAP = new HashMap<>(validators.length);
    for (final var validator : validators) {
      if (null != VALIDATOR_MAP.put(validator.supportsType().getSimpleName(), validator)) {
        throw new IllegalStateException("duplicate validator for type " + validator.supportsType());
      }
    }
  }

  @Override
  public boolean isValid(
    final Object o,
    final ConstraintValidatorContext context
  ) {
    initValidators();
    try {
      final var dataWithSchema = loadDataWithSchema(o);
      validate(dataWithSchema);
    } catch (final IllegalStateException exc) {
      LOG.debug("validation failed", exc);
      return false;
    }
    return true;
  }

  // called recursively for nested fields
  private void validate(final DataWithSchema dataWithSchema) {
    for (final String schemaKey : dataWithSchema.schema.keySet()) {
      final var schema = dataWithSchema.schema.get(schemaKey);
      final var data = dataWithSchema.data.get(schemaKey);
      validateField(schemaKey, schema, data);
    }
  }

  // validate single field with schema
  private void validateField(
    final String schemaKey,
    final FieldDefinition fieldDefinition,
    final Object data
  ) {
    if (fieldDefinition.getNullable() == Boolean.FALSE && data == null) {
      throw new IllegalStateException("data is missing required field " + schemaKey);
    }
    if (fieldDefinition.getType().equals(Object.class.getSimpleName())) {
      validateNestedObject(data, fieldDefinition);
    } else {
      validateFieldWithValidator(schemaKey, data, fieldDefinition);
    }
  }

  private void validateNestedObject(
    final Object data,
    final FieldDefinition fieldDefinition
  ) {
    try {
      LOG.debug("validate nested object");
      final var nestedSchema = fieldDefinition.getNested();
      @SuppressWarnings("unchecked") final var nestedObject = (Map<String, Object>) data;
      validate(new DataWithSchema(nestedSchema, nestedObject));
    } catch (final ClassCastException exc) {
      throw new IllegalStateException(exc);
    }
  }

  private void validateFieldWithValidator(
    final String schemaKey,
    final Object data,
    final FieldDefinition fieldDefinition
  ) {
    final var validator = VALIDATOR_MAP.get(fieldDefinition.getType());
    if (null == validator) {
      throw new IllegalStateException("no validator for " + fieldDefinition.getType());
    } else {
      validator.validate(schemaKey, data, fieldDefinition);
    }
  }

  private DataWithSchema loadDataWithSchema(final Object o) {
    final var clazz = o.getClass();
    final var annotation = clazz.getDeclaredAnnotation(ValidateSchema.class);

    final Map<String, FieldDefinition> schema;
    final Map<String, Object> data;
    try {
      schema = getFieldFromAnnotatedClass(clazz, annotation.schemaFieldName(), o);
      data = getFieldFromAnnotatedClass(clazz, annotation.dataFieldName(), o);
      return new DataWithSchema(schema, data);
    } catch (final ClassCastException | NoSuchFieldException | IllegalAccessException exc) {
      throw new IllegalStateException(exc);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T getFieldFromAnnotatedClass(
    Class<?> clazz,
    String fieldName,
    Object o
  ) throws NoSuchFieldException, IllegalAccessException {
    final var field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    return (T) field.get(o);
  }

  @Value
  private static class DataWithSchema {
    Map<String, FieldDefinition> schema;
    Map<String, Object> data;
  }

}
