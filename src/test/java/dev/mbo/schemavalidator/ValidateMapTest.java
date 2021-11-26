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

import lombok.Builder;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateMapTest extends AbstractValidatorTest {

  public static final Map<String, FieldDefinition> SHARED_SCHEMA = Map.of(
    "name", FieldDefinition.builder().type(String.class.getSimpleName()).notBlank(true).build(),
    "age", FieldDefinition.builder().type(Integer.class.getSimpleName()).nullable(false).minValue(0L).build(),
    "time", FieldDefinition.builder().type(Long.class.getSimpleName()).nullable(false).build(),
    "zero", FieldDefinition.builder().type(Double.class.getSimpleName()).nullable(false).build(),
    "salary", FieldDefinition.builder().type(BigDecimal.class.getSimpleName()).nullable(false).minValue(0L).maxValue(10000L).build(),
    "nested", FieldDefinition.builder().type(Object.class.getSimpleName()).nested(
      Map.of(
        "test", FieldDefinition.builder().type(String.class.getSimpleName()).notBlank(true).build(),
        "test2", FieldDefinition.builder().type(Integer.class.getSimpleName()).minValue(0L).build()
      )
    ).build()
  );

  @Test
  protected void testValidObject() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData())
      .build();
    assertThat(validator.validate(toTest)).isEmpty();
  }

  @Test
  protected void testBlankName() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("name", "")))
      .build();
    assertThat(validator.validate(toTest)).hasSize(1);
  }

  @Test
  protected void testAgeNegative() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("age", -1)))
      .build();
    assertThat(validator.validate(toTest)).hasSize(1);
  }

  @Test
  protected void testAgeWrongType() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("age", "foo")))
      .build();
    assertThat(validator.validate(toTest)).hasSize(1);
  }

  @Test
  protected void testTimeWrongTypeInt() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("time", 1)))
      .build();
    assertThat(validator.validate(toTest)).hasSize(1);
  }

  @Test
  protected void testTimeWrongTypeStr() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("time", "a")))
      .build();
    assertThat(validator.validate(toTest)).hasSize(1);
  }

  @Test
  protected void testZeroWrongTypeStr() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("zero", "0.0")))
      .build();
    assertThat(validator.validate(toTest)).hasSize(1);
  }

  @Test
  protected void testSalaryTooHigh() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("salary", "100000")))
      .build();
    assertThat(validator.validate(toTest)).hasSize(1);
  }

  @Test
  protected void testSalaryTooLow() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("salary", "-100000")))
      .build();
    assertThat(validator.validate(toTest)).hasSize(1);
  }

  @Test
  protected void testSalaryInt() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("salary", 1000)))
      .build();
    assertThat(validator.validate(toTest)).hasSize(0);
  }

  @Test
  protected void testSalaryDouble() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("salary", 1000.0)))
      .build();
    assertThat(validator.validate(toTest)).hasSize(0);
  }

  @Test
  protected void testSalaryLong() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("salary", 1000L)))
      .build();
    assertThat(validator.validate(toTest)).hasSize(0);
  }

  @Test
  protected void testNestedTestBlank() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("nested", Map.of("test", "", "test2", 1))))
      .build();
    assertThat(validator.validate(toTest)).hasSize(1);
  }

  @Test
  protected void testNestedTest2TooLow() {
    final var toTest = ToTest.builder()
      .schema(SHARED_SCHEMA)
      .data(sharedData(Map.of("nested", Map.of("test", "foo", "test2", -1))))
      .build();
    assertThat(validator.validate(toTest)).hasSize(1);
  }

  @Builder
  @ValidateSchema(
    type = ValidateType.MAP,
    schemaFieldName = "schema",
    dataFieldName = "data"
  )
  public static class ToTest {
    @NotNull
    private Map<String, FieldDefinition> schema;
    @NotNull
    private Map<String, Object> data;

    private String otherField;
  }

}
