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

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ValidateSchemaValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
@Documented
public @interface ValidateSchema {

  String message() default "{javax.validation.constraints.ValidateSchema.invalid}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  ValidateType type() default ValidateType.MAP;

  String schemaFieldName() default "";

  String dataFieldName();

  String jsonSchema() default "";

}
