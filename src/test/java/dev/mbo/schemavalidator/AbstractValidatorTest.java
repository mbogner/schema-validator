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

import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractValidatorTest {

  protected final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  protected void isValid() {
    assertThat(validator).isNotNull();
  }

  protected Map<String, Object> sharedData() {
    return Map.of(
      "name", "stefan",
      "age", 30,
      "time", System.currentTimeMillis(),
      "zero", 0.0,
      "salary", "2549.50",
      "nested", Map.of("test", "val1", "test2", 2)
    );
  }

  protected Map<String, Object> sharedData(
    final Map<String, Object> toChange
  ) {
    final var data = new HashMap<>(sharedData());
    data.putAll(toChange);
    return Collections.unmodifiableMap(data);
  }

}
