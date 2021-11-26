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

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import lombok.Value;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonSchemaCache {

  private final Map<Integer, CacheEntry> jsonSchemaCache;
  private final JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
  private final int initialCapacity;
  private final int timeoutSeconds;

  public JsonSchemaCache() {
    this.initialCapacity = 128;
    this.timeoutSeconds = 3600;
    jsonSchemaCache = new HashMap<>(initialCapacity);
  }

  public JsonSchemaCache(final int initialCapacity, final int timeoutSeconds) {
    this.initialCapacity = initialCapacity;
    this.timeoutSeconds = timeoutSeconds;
    jsonSchemaCache = new HashMap<>(initialCapacity);
  }

  public JsonSchema add(final String jsonSchemaStr) {
    requiredStringArgument(jsonSchemaStr);
    return add(jsonSchemaStr.hashCode(), jsonSchemaStr);
  }

  private JsonSchema add(
    final int hashCode,
    final String jsonSchemaStr
  ) {
    final var now = now();
    clean(now);
    final var jsonSchema = jsonSchemaFactory.getSchema(jsonSchemaStr);
    jsonSchemaCache.put(hashCode, new CacheEntry(now.plusSeconds(timeoutSeconds), jsonSchema));
    return jsonSchema;
  }

  public Optional<JsonSchema> get(final int hashCode) {
    clean(now());
    final var cacheEntry = jsonSchemaCache.get(hashCode);
    if (null == cacheEntry) {
      return Optional.empty();
    }
    return Optional.of(cacheEntry.jsonSchema);
  }

  public JsonSchema getOrAdd(final String jsonSchemaStr) {
    requiredStringArgument(jsonSchemaStr);
    final var hashCode = jsonSchemaStr.hashCode();
    final var optionalJsonSchema = get(hashCode);
    return optionalJsonSchema.orElseGet(() -> add(hashCode, jsonSchemaStr));
  }

  private static void requiredStringArgument(final String arg) {
    if (null == arg || arg.isBlank()) {
      throw new IllegalArgumentException("argument must not be null or blank");
    }
  }

  private Instant now() {
    return Instant.now();
  }

  private void clean(final Instant ts) {
    final List<Integer> toRemove = new ArrayList<>(initialCapacity);
    jsonSchemaCache.forEach((hashCode, entry) -> {
      if (ts.isAfter(entry.validUntil)) {
        toRemove.add(hashCode);
      }
    });
    for (final Integer i : toRemove) {
      jsonSchemaCache.remove(i);
    }
  }

  @Value
  public static class CacheEntry {
    Instant validUntil;
    JsonSchema jsonSchema;
  }

}
