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
