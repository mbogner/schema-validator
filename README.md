schema-validator
----------------

Simple bean validator that takes two parameter:

- First one is a map of field names to FieldDefinitions that define how to validate the second map.
- The second map is a simple map of strings to objects.

These maps are compatible to be stored as jsonb in PostgreSQL and can be used with the jsonb types from Vlad Mihalcea.

This whole project is just a small proof of concept and hasn't been tested in production. If you find any bugs please
create issues or even better merge requests.

# Example

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

Check the test folder for how to define the two maps.
