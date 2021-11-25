package dev.mbo.schemavalidator;

public interface FieldValidator<T> {
  void validate(
    final String key,
    final Object data,
    final FieldDefinition fieldDefinition
  );

  Class<T> supportsType();
}
