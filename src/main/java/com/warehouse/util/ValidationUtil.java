package com.warehouse.util;

public class ValidationUtil {

    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrBlank(email)) return true; // email is optional
        return email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$");
    }

    public static boolean isValidPhone(String phone) {
        if (isNullOrBlank(phone)) return true; // phone is optional
        return phone.matches("^[0-9+\\-() ]{7,20}$");
    }

    public static boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeInteger(String value) {
        try {
            return Integer.parseInt(value) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDecimal(String value) {
        try {
            double d = Double.parseDouble(value);
            return d >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
