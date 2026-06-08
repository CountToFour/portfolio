package com.example.rentlyauth.service.utils;

public class EmailUtils {
    public static String getEmail(String name,String host, String token) {
        return "Hello " +
                name +
                "Your new account has been created. Please click the link below to verify your account." +
                "\n" +
                getVerificationUrl(host, token);

    }
    public static String getVerificationUrl(String host,String token) {
        return host + "/api/auth/ConfirmationEmail?token=" + token;
    }
    public static String getPassword(String host, String token)
    {
        return "Hello " +
                "Click the link below to reset yor password." +
                "\n" +
               getVerificationUrlForPassword(host, token);

    }
    public static String getVerificationUrlForPassword(String host,String token) {
        return host + "/api/auth/ConfirmationPassword?token=" + token;
    }
}
