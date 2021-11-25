schema-validator
----------------

Simple bean validator that takes two parameter:

- First one is a map of field names to FieldDefinitions that define how to validate the second map.
- The second map is a simple map of strings to objects.

These maps are compatible to be stored as jsonb in PostgreSQL and can be used with the jsonb types from Vlad Mihalcea.

This whole project is just a small proof of concept and hasn't been tested in production. If you find any bugs please
create issues or even better merge requests.

# Example

Here a sample class using the validator:

```java

@ValidateSchema(
  schemaFieldName = "schema",
  dataFieldName = "data"
)
public static class ToTest {
  @NotNull
  private Map<String, FieldDefinition> schema;
  @NotNull
  private Map<String, Object> data;
}
```

And her is how to define the fields:

Schema:

```java
  Map.of(
  "name",FieldDefinition.builder().type(String.class.getSimpleName()).notBlank(true).build(),
  "age",FieldDefinition.builder().type(Integer.class.getSimpleName()).nullable(false).minValue(0L).build(),
  "time",FieldDefinition.builder().type(Long.class.getSimpleName()).nullable(false).build(),
  "zero",FieldDefinition.builder().type(Double.class.getSimpleName()).nullable(false).build(),
  "salary",FieldDefinition.builder().type(BigDecimal.class.getSimpleName()).nullable(false).minValue(0L).maxValue(10000L).build(),
  "nested",FieldDefinition.builder().type(Object.class.getSimpleName()).nested(
  Map.of(
  "test",FieldDefinition.builder().type(String.class.getSimpleName()).notBlank(true).build(),
  "test2",FieldDefinition.builder().type(Integer.class.getSimpleName()).minValue(0L).build()
  )
  ).build());
```

Data:

```java
Map.of(
  "name","stefan",
  "age",30,
  "time",System.currentTimeMillis(),
  "zero",0.0,
  "salary","2549.50",
  "nested",Map.of("test","val1","test2",2)
  );
```

The tests include these samples if you want to see it in action.
