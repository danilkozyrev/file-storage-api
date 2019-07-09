package com.github.danilkozyrev.filestorageapi.validation;

import org.passay.*;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * Checks that a given string is a valid password. Constraints: no whitespaces, length between 5 and 30 symbols, at
 * least one uppercase character, at least one lowercase character and at least one digit.
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Valid for null
        if (password == null) {
            return true;
        }

        PasswordValidator passwordValidator = new PasswordValidator(List.of(
                // No whitespaces.
                new WhitespaceRule(),
                // Min length = 5, max = 30.
                new LengthRule(5, 30),
                // At least one uppercase character.
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                // At least one lowercase character.
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                // At least one digit.
                new CharacterRule(EnglishCharacterData.Digit, 1)));

        RuleResult result = passwordValidator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        } else {
            StringBuilder errorMessageBuilder = new StringBuilder();
            List<String> errorMessageList = passwordValidator.getMessages(result);
            for (int i = 0; i < errorMessageList.size(); i++) {
                errorMessageBuilder.append(errorMessageList.get(i));
                if (i != errorMessageList.size() - 1) {
                    errorMessageBuilder.append(" ");
                }
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorMessageBuilder.toString()).addConstraintViolation();
            return false;
        }
    }

}
