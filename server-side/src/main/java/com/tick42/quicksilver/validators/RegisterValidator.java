package com.tick42.quicksilver.validators;

import com.tick42.quicksilver.models.Spec.ExtensionSpec;
import com.tick42.quicksilver.models.Spec.UserSpec;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UserValidator implements Validator {

    private static final int MINIMUM_NAME_LENGTH = 7;
    private static final int MAXIMUM_NAME_LENGTH = 22;

    @Override
    public boolean supports(Class<?> clazz) {
        return ExtensionSpec.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "Name must be at least 7 characters long.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "Password must be at least 7 characters long.");
        UserSpec userSpec = (UserSpec) target;
        if(!userSpec.getPassword().equals(userSpec.getRepeatPassword())){
            errors.reject("Passwords must match.");
        }
        if (errors.getFieldErrorCount("name") == 0 && (userSpec.getUsername().trim().length() < MINIMUM_NAME_LENGTH || userSpec.getUsername().length() > MAXIMUM_NAME_LENGTH)) {
            errors.reject("Username must be between 7 and 22 characters");
        }
    }
}
