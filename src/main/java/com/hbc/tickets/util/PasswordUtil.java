package com.hbc.tickets.util;

import java.security.SecureRandom;

public class PasswordUtil {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";

    public static String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            password.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return password.toString();
    }
}
