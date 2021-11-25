package dev.mbo.schemavalidator.fieldvalidator;

import dev.mbo.schemavalidator.FieldDefinition;
import dev.mbo.schemavalidator.FieldValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class BigDecimalFieldValidator implements FieldValidator<BigDecimal> {

  private static final Logger LOG = LoggerFactory.getLogger(BigDecimalFieldValidator.class);

  @Override
  public void validate(
    final String key,
    final Object data,
    final FieldDefinition fieldDefinition
  ) {
    LOG.debug("validate {}::{}: {}", key, fieldDefinition.getType(), data);
    if (fieldDefinition.getNullable() == Boolean.TRUE && null == data) {
      return;
    }
    final BigDecimal value;
    if (data instanceof String) {
      value = new BigDecimal((String) data);
    } else if (data instanceof Double) {
      LOG.warn("you should use string representation for getting BigDecimal from Double");
      value = BigDecimal.valueOf((Double) data);
    } else if (data instanceof Integer) {
      value = BigDecimal.valueOf((Integer) data);
    } else if (data instanceof Long) {
      value = BigDecimal.valueOf((Long) data);
    } else {
      throw new IllegalStateException(data + " is not parsable to " + supportsType());
    }

    if (fieldDefinition.getMinValue() != null && value.compareTo(BigDecimal.valueOf(fieldDefinition.getMinValue())) < 0) {
      throw new IllegalStateException(value + " is smaller than min value of " + fieldDefinition.getMinValue());
    }
    if (fieldDefinition.getMaxValue() != null && value.compareTo(BigDecimal.valueOf(fieldDefinition.getMaxValue())) > 0) {
      throw new IllegalStateException(value + " is bigger than max value of" + fieldDefinition.getMaxValue());
    }
  }

  @Override
  public Class<BigDecimal> supportsType() {
    return BigDecimal.class;
  }
}
