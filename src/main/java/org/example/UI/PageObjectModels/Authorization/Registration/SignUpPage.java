package org.example.UI.PageObjectModels.Authorization.Registration;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class SignUpPage {
    private final Page page;

    private final Locator signUpLink;
    private final Locator registerNewBusinessRadio;
    private final Locator continueBtn;

    public SignUpPage(Page page) {
        this.page = page;

        signUpLink = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Don't have an account? Sign Up"));

        registerNewBusinessRadio = page.getByRole(AriaRole.RADIO,
                new Page.GetByRoleOptions().setName("Register new Business Create"));

        continueBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Continue"));
    }

    public SignUpPage openSignUp() {
        signUpLink.click();
        return this;
    }

    public SignUpPage chooseRegisterNewBusiness() {
        registerNewBusinessRadio.check();
        return this;
    }

    public SignUpEmailPage continueToEmailStep() {
        continueBtn.click();
        return new SignUpEmailPage(page);
    }
}
