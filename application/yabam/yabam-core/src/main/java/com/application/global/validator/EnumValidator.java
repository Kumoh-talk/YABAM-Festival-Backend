package com.application.global.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<ValidEnum, Enum> {
	private ValidEnum annotation;

	@Override
	public void initialize(ValidEnum constraintAnnotation) {
		this.annotation = constraintAnnotation;
	}

	@Override
	public boolean isValid(Enum value, ConstraintValidatorContext context) {

		if (value == null) {
			return false;
		}

		Object[] enumValues = this.annotation.enumClass().getEnumConstants();
		if (enumValues != null) {
			for (Object enumValue : enumValues) {
				if (value.equals(enumValue)) {
					return true;
				}
			}
		}
		return false;
	}
}
