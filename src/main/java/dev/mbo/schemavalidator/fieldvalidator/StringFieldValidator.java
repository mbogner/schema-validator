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
