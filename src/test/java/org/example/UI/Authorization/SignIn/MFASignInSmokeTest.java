package org.example.UI.Authorization.SignIn;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.assertj.core.api.Assertions;
import org.example.BaseUITestExtension.PlaywrightUiLoginBaseTest;
import org.example.UI.PageObjectModels.Authorization.SignIn.MFASignInPage;
import org.example.UI.PageObjectModels.Authorization.SignIn.SignInPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class MFASignInSmokeTest extends PlaywrightUiLoginBaseTest {
    SignInPage signInPage;
    MFASignInPage mfasignInPage;

    private static final String email = "myvegy@denipl.com";
    private static final String password = "Test+1234";

    @BeforeEach
    public void setUp() {
        openPath("/sign-in");
        signInPage = new SignInPage(page);
        mfasignInPage = new MFASignInPage(page);
    }

    @DisplayName("MFA User presence")
    @Test
    public void mfaUserPresenceTest(){
        Assertions.assertThat(signInPage.getTitle()).contains("Welcome back, sign in!");
        signInPage.signIntoApplication(email, password);
        mfasignInPage.isMFATextPresent();
        PlaywrightAssertions.assertThat(mfasignInPage.isMFATextPresent()).isVisible();

    }

}
