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

import com.fasterxml.jackson.databind.ObjectMapper;
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

  // ---------- MAP --------------

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
  public static void initMapValidators(final FieldValidator<?>... validators) {
    if (null == VALIDATOR_MAP) {
      LOG.debug("init map validators");
      if (null == validators || validators.length < 1) {
        LOG.debug("using default validators");
        initMapValidatorsShared(DEFAULT_FIELD_VALIDATORS);
      } else {
        LOG.debug("using provided validators");
        initMapValidatorsShared(validators);
      }
    }
  }

  private static void initMapValidatorsShared(final FieldValidator<?>[] validators) {
    VALIDATOR_MAP = new HashMap<>(validators.length);
    for (final var validator : validators) {
      if (null != VALIDATOR_MAP.put(validator.supportsType().getSimpleName(), validator)) {
        throw new IllegalStateException("duplicate validator for type " + validator.supportsType());
      }
    }
  }

  // ---------- JSON SCHEMA --------------

  private static ObjectMapper objectMapper = null;
  private static JsonSchemaCache jsonSchemaCache = null;

  private static void initJsonSchemaValidator() {
    if (null == jsonSchemaCache) {
      LOG.debug("init json schema cache");
      jsonSchemaCache = new JsonSchemaCache();
      if (null == objectMapper) {
        objectMapper = new ObjectMapper();
      }
    }
  }

  public static void setObjectMapper(final ObjectMapper externalOM) {
    objectMapper = externalOM;
  }

  @Override
  public boolean isValid(
    final Object o,
    final ConstraintValidatorContext context
  ) {
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
    if (dataWithSchema.type == ValidateType.MAP) {
      initMapValidators();
      for (final String schemaKey : dataWithSchema.schema.keySet()) {
        final var schema = dataWithSchema.schema.get(schemaKey);
        final var data = dataWithSchema.data.get(schemaKey);
        validateField(schemaKey, schema, data);
      }
    } else if (dataWithSchema.type == ValidateType.JSON_SCHEMA) {
      initJsonSchemaValidator();
      validateJsonSchema(dataWithSchema);
    }
  }

  private void validateJsonSchema(final DataWithSchema dataWithSchema) {
    final var jsonSchema = jsonSchemaCache.getOrAdd(dataWithSchema.jsonSchema);
    final var jsonData = objectMapper.valueToTree(dataWithSchema.data);

    final var result = jsonSchema.validate(jsonData);
    if (!result.isEmpty()) {
      throw new IllegalStateException("validation had errors: " + result);
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
      validate(new DataWithSchema(ValidateType.MAP, nestedSchema, null, nestedObject));
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

    final ValidateType validateType = annotation.type();
    final Map<String, Object> data;
    Map<String, FieldDefinition> schema = null;
    String jsonSchema = null;
    try {
      data = getFieldFromAnnotatedClass(clazz, annotation.dataFieldName(), o);
      switch (validateType) {
        case MAP:
          schema = getFieldFromAnnotatedClass(clazz, annotation.schemaFieldName(), o);
          break;
        case JSON_SCHEMA:
          jsonSchema = getFieldFromAnnotatedClass(clazz, annotation.jsonSchema(), o);
          break;
        default:
          throw new IllegalStateException("unsupported type: " + validateType);
      }
      return new DataWithSchema(validateType, schema, jsonSchema, data);
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
    ValidateType type;
    Map<String, FieldDefinition> schema;
    String jsonSchema;
    Map<String, Object> data;
  }

}
