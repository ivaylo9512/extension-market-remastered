package com.tick42.quicksilver.validators;

import com.tick42.quicksilver.models.specs.ExtensionSpec;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class ExtensionValidator implements Validator {

    private static final int MINIMUM_NAME_LENGTH = 6;

    @Override
    public boolean supports(Class<?> clazz) {
        return ExtensionSpec.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", String.format("Name must be at least %d characters long.", MINIMUM_NAME_LENGTH));
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "id", "No id present.");

        ExtensionSpec extensionSpec = (ExtensionSpec) target;
        if(extensionSpec.getGithub().length() == 0){
            extensionSpec.setGithub(null);

        }else if(!extensionSpec.getGithub().matches("^(www|http:|https:)+//github.com/.+/.+$")){
            errors.rejectValue("github", "Github must match http://github.com/:user/:repo");
        }
        if (errors.getFieldErrorCount("name") == 0 && extensionSpec.getName().trim().length() < MINIMUM_NAME_LENGTH) {
            errors.rejectValue("name", String.format("Name must be at least %d characters", MINIMUM_NAME_LENGTH));
        }
    }
}
