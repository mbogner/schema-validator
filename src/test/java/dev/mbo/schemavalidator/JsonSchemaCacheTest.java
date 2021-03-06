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

import static org.assertj.core.api.Assertions.assertThat;

class JsonSchemaCacheTest {

  private final JsonSchemaCache cache = new JsonSchemaCache();

  @Test
  void getOrAdd() {
    final var jsonSchemaStr = FileUtil.slurpFromClasspath("json_schema.json");
    final var jsonSchema = cache.getOrAdd(jsonSchemaStr);
    assertThat(jsonSchema).isNotNull();
    final var jsonSchemaGet = cache.get(jsonSchemaStr.hashCode());
    assertThat(jsonSchemaGet).isNotEmpty();
    assertThat(jsonSchema).isEqualTo(jsonSchemaGet.get());
  }

}
