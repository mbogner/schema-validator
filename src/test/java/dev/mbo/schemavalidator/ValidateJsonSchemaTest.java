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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidateJsonSchemaTest extends AbstractValidatorTest {

  @Test
  protected void testValidObject() {
    final var toTest = ToTest.builder()
      .schema(FileUtil.slurpFromClasspath("json_schema.json"))
      .data(sharedData())
      .build();
    assertThat(validator.validate(toTest)).isEmpty();
  }

  @Builder
  @ValidateSchema(
    type = ValidateType.JSON_SCHEMA,
    jsonSchema = "schema",
    dataFieldName = "data"
  )
  public static class ToTest {
    @NotNull
    private String schema;
    @NotNull
    private Map<String, Object> data;

    private String otherField;
  }

}
