package dev.mbo.schemavalidator;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ValidateSchemaValidator.class)
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface ValidateSchema {

  String message() default "{javax.validation.constraints.ValidateSchema.invalid}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String schemaFieldName();

  String dataFieldName();

}
