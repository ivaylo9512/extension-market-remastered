package com.tick42.quicksilver.validators;

import com.tick42.quicksilver.models.specs.UserSpec;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class RegisterValidator implements Validator {
    private static final int MINIMUM_NAME_LENGTH = 7;
    private static final int MAXIMUM_NAME_LENGTH = 22;
    private static final int MINIMUM_PASSWORD_LENGTH = 9;
    private static final int MAXIMUM_PASSWORD_LENGTH = 25;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserSpec.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "Name must be at least 7 characters long.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "Password must be at least 7 characters long.");

        UserSpec userSpec = (UserSpec)target;
        if(!userSpec.getPassword().equals(userSpec.getRepeatPassword())){
            errors.reject("Passwords must match.");
        }
        if (errors.getFieldErrorCount("name") == 0 && (userSpec.getUsername().trim().length() < MINIMUM_NAME_LENGTH || userSpec.getUsername().length() > MAXIMUM_NAME_LENGTH)) {
            errors.reject(String.format("Username must be between %d and %d", MINIMUM_NAME_LENGTH, MAXIMUM_NAME_LENGTH));
        }
        if (errors.getFieldErrorCount("password") == 0 && (userSpec.getPassword().trim().length() < MINIMUM_PASSWORD_LENGTH || userSpec.getPassword().length() > MAXIMUM_PASSWORD_LENGTH)) {
            errors.reject(String.format("Password must be between %d and %d characters", MINIMUM_PASSWORD_LENGTH, MAXIMUM_PASSWORD_LENGTH));
        }
    }
}
