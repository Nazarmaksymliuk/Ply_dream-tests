package org.example.UI.Authorization.SignIn;

import com.microsoft.playwright.assertions.LocatorAssertions;
import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Authorization.SignIn.SignInPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class SignIntoApplicationTest extends PlaywrightUiLoginBaseTest {
    SignInPage signInPage;
    private static final String email = "maksimlukoleg56@gmail.com";
    private static final String password = "Test+1234";

    private static final String invalidEmail = "invalid@gmail.com";
    private static final String invalidPassword = "invalid_code";


    @BeforeEach
    public void setUp() {
        openPath("/sign-in");
        signInPage = new SignInPage(page);
    }

    @DisplayName("Sign into application")
    @Test
    public void signIntoApplicationTest(){
        //Check if we are in the page
        Assertions.assertThat(signInPage.getTitle()).contains("Welcome back, sign in!");
        //Sign in to application
        signInPage.signIntoApplication(email, password);
    }

    @DisplayName("Sign into application negative test")
    @Test
    public void signIntoApplicationNegativeTest(){
        Assertions.assertThat(signInPage.getTitle()).contains("Welcome back, sign in!");
        signInPage.signIntoApplication(invalidEmail, invalidPassword);

        signInPage.waitUntilErrorVisible();
        Assertions.assertThat(signInPage.isErrorVisible()).isTrue();
        Assertions.assertThat(signInPage.getTheErrorMessageText()).isEqualTo("Incorrect username or password.");
        assertThat(signInPage.error())
                .isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(10000));
    }

}
