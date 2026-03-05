package com.bl.stockportfolioalerts.auth.validation;

import java.util.function.Predicate;

public class UserValidation {
    public static Predicate<String> isValidEmail =
            email -> email.contains("@") && email != null;

    public static Predicate<String> isStrongPassword =
            password -> password != null && password.length() >= 8;
}
