package org.example.UI.PageObjectModels.Authorization.Registration;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class SignUpEmailPage {
    private final Page page;

    private final Locator emailInput;
    private final Locator termsCheckbox;
    private final Locator getStartedBtn;

    public SignUpEmailPage(Page page) {
        this.page = page;

        emailInput = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter your company email"));

        termsCheckbox = page.getByRole(AriaRole.CHECKBOX,
                new Page.GetByRoleOptions().setName("I've read and accepted the"));

        getStartedBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Get started"));
    }

    public SignUpEmailPage setEmail(String email) {
        emailInput.fill(email);
        return this;
    }

    public SignUpEmailPage acceptTerms() {
        termsCheckbox.check();
        return this;
    }

    public SignUpAccountPage clickGetStarted() {
        getStartedBtn.click();
        return new SignUpAccountPage(page);
    }
}
