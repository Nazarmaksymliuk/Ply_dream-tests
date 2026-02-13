package org.example.UI.Authorization.SignIn;

import com.microsoft.playwright.assertions.LocatorAssertions;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Authorization.SignIn.SignInPage;
import org.example.creds.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Epic("Authorization")
@Feature("Sign In UI")
public class SignIntoApplicationTest extends PlaywrightUiLoginBaseTest {
    SignInPage signInPage;

    @BeforeEach
    public void setUp() {
        openPath("/sign-in");
        signInPage = new SignInPage(page);
    }

    @DisplayName("Sign into application")
    @Test
    public void signIntoApplicationTest(){
        Assertions.assertThat(signInPage.getTitle()).contains("Welcome back, sign in!");
        signInPage.signIntoApplication(Users.ADMIN.email(), Users.ADMIN.password());
    }

    @DisplayName("Sign into application negative test")
    @Test
    public void signIntoApplicationNegativeTest(){
        Assertions.assertThat(signInPage.getTitle()).contains("Welcome back, sign in!");
        signInPage.signIntoApplication(Users.INVALID_USER.email(), Users.INVALID_USER.password());

        signInPage.waitUntilErrorVisible();
        Assertions.assertThat(signInPage.isErrorVisible()).isTrue();
        Assertions.assertThat(signInPage.getTheErrorMessageText()).isEqualTo("Incorrect username or password.");
        assertThat(signInPage.error())
                .isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(10000));
    }
}
