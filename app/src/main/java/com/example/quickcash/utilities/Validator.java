package com.example.quickcash.utilities;

import java.util.regex.Pattern;

public class Validator {

    public static final int MINIMUM_PASSWORD_LENGTH = 8;

    /**
     * Method to check validity of an email according to the following rules:
     *      1. The email must contain an '@' and at least 1 '.' following the @
     *      2. The part of the email preceding the '.' may only contain letters (capital or lowercase),
     *          numerals, and any of the following: !#$%&'*+-/=?^_`{|}
     *      3. The part after the '@' must only contain letters A-Z
     *
     * @param email email to check
     * @return true if the email is valid according to the rules, false otherwise
     */
    public boolean checkValidEmail(String email){
        return Pattern.matches("[a-zA-Z!#$%&'*+-/=?^_`{|}~0-9]*@[a-zA-Z.]+.[a-zA-z][a-zA-z]+", email);
    }

    /**
     * Method to check if a given password is valid. A password is valid if it contains a mix of
     * uppercase and lowercase letters, symbols and numbers and is at least 8 characters long
     * @param password The password to check the validity of.
     * @return True if the password is valid, and false otherwise
     */
    public boolean checkValidPassword(String password){
        boolean aboveMinimumCharacters = password.length()>= MINIMUM_PASSWORD_LENGTH;
        boolean hasSymbol = Pattern.matches(".*[!@#$%^&*()_+=`~].*", password);
        boolean hasNumber = Pattern.matches(".*[0-9].*", password);
        boolean hasLowercase = Pattern.matches(".*[a-z].*", password);
        boolean hasUppercase = Pattern.matches(".*[A-Z].*", password);
        return aboveMinimumCharacters && hasSymbol && hasNumber && hasLowercase && hasUppercase;

    }

    /**
     * Method to check if a given input is empty. It is considered empty if it contains exclusively
     * spaces and whitespace, or nothing at all
     *
     * @param input
     * @return True if the input is considered empty, false otherwise
     */
    public boolean checkInputEmpty(String input){
        return Pattern.matches("[ \n]*", input);
    }
}
