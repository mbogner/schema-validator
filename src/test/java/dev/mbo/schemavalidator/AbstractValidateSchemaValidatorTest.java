package dev.mbo.schemavalidator;

import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractValidateSchemaValidatorTest {

  protected final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  protected void isValid() {
    assertThat(validator).isNotNull();
  }

}
