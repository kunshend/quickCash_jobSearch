package com.example.quickcash;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.quickcash.utilities.Validator;

import org.junit.Test;
public class ValidatorTest {

    @Test
    public void checkValidEmail_Empty() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidEmail(""));
    }

    @Test
    public void checkValidEmail_NoAt() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidEmail("a.b"));
    }

    @Test
    public void checkValidEmail_NoDot() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidEmail("a@b"));
    }

    @Test
    public void checkValidEmail_NoLetters() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidEmail("@."));
    }



    @Test
    public void checkValidEmail_SpecialCharacter() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidEmail("ⵣ@b.ca"));
    }

    @Test
    public void checkValidEmail_1LetterTLD() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidEmail("a@b.c"));
    }

    @Test
    public void checkValidEmail_SpecialCharacterAllowed() {

        Validator v1 = new Validator();
        assertTrue(v1.checkValidEmail("a*@b.ca"));
    }

    @Test
    public void checkValidEmail_SpecialCharacterNotAllowed() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidEmail("a\"b.ca"));
    }

    @Test
    public void checkValidEmail_Valid() {

        Validator v1 = new Validator();
        assertTrue(v1.checkValidEmail("a@b.ca"));
    }

    @Test
    public void checkValidEmail_ValidCapitals() {

        Validator v1 = new Validator();
        assertTrue(v1.checkValidEmail("A@b.cA"));
    }

    @Test
    public void checkValidEmail_Valid_Subdomain() {

        Validator v1 = new Validator();
        assertTrue(v1.checkValidEmail("a@cs.dal.ca"));
    }
    @Test
    public void checkInputEmpty_Empty(){
        Validator v1 = new Validator();
        assertTrue(v1.checkInputEmpty(""));
    }

    @Test
    public void checkInputEmpty_OnlySpace(){
        Validator v1 = new Validator();
        assertTrue(v1.checkInputEmpty(" "));
    }

    @Test
    public void checkInputEmpty_Newline(){
        Validator v1 = new Validator();
        assertTrue(v1.checkInputEmpty("\n"));
    }

    @Test
    public void checkInputEmpty_NotEmpty(){
        Validator v1 = new Validator();
        assertFalse(v1.checkInputEmpty("a"));
    }

    @Test
    public void checkInputEmpty_NotEmpty_WithSpaces(){
        Validator v1 = new Validator();
        assertFalse(v1.checkInputEmpty(" a "));
    }
    @Test
    public void checkInputEmpty_NotEmpty_WithWhitespace(){
        Validator v1 = new Validator();
        assertFalse(v1.checkInputEmpty("a\n"));
    }

    @Test
    public void checkValidPassword_Empty() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidPassword(""));
    }

    @Test
    public void checkValidPassword_BelowMinimumCharacters() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidPassword("#aA4567"));
    }

    @Test
    public void checkValidPassword_NoSymbol() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidPassword("aA34567"));
    }

    @Test
    public void checkValidPassword_NoNumber() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidPassword("#Bcdefgh"));
    }

    @Test
    public void checkValidPassword_NoCapital() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidPassword("#1cdefgh"));
    }

    @Test
    public void checkValidPassword_NoLowercase() {

        Validator v1 = new Validator();
        assertFalse(v1.checkValidPassword("#1CDEFGH"));
    }

    @Test
    public void checkValidPassword_Valid() {

        Validator v1 = new Validator();
        assertTrue(v1.checkValidPassword("#1Cdefgh"));
    }
}