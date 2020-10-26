package com.tick42.quicksilver.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;

public class NewPasswordValidator implements Validator {
    private static final int MINIMUM_NAME_LENGTH = 7;
    private static final int MAXIMUM_NAME_LENGTH = 22;
    private static final int MINIMUM_PASSWORD_LENGTH = 9;
    private static final int MAXIMUM_PASSWORD_LENGTH = 25;

    @Override
    public boolean supports(Class<?> clazz) {
        return NewPasswordSpec.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", String.format("Username must be between %d and %d characters.", MINIMUM_NAME_LENGTH, MAXIMUM_NAME_LENGTH));
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPassword", String.format("New password must be between %d and %d characters.", MINIMUM_PASSWORD_LENGTH, MAXIMUM_PASSWORD_LENGTH));

        NewPasswordSpec passwordSpec = (NewPasswordSpec)target;
        if(!passwordSpec.getNewPassword().equals(passwordSpec.getRepeatNewPassword())){
            errors.reject("Passwords must match.");
        }
        if (errors.getFieldErrorCount("newPassword") == 0 && (passwordSpec.getNewPassword().trim().length() < MINIMUM_PASSWORD_LENGTH || passwordSpec.getNewPassword().length() > MAXIMUM_PASSWORD_LENGTH)) {
            errors.reject(String.format("Password must be between %d and %d", MINIMUM_PASSWORD_LENGTH, MAXIMUM_PASSWORD_LENGTH));
        }
    }
}
