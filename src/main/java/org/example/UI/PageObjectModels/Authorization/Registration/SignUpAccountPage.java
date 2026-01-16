package org.example.UI.PageObjectModels.Authorization.Registration;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class SignUpAccountPage {
    private final Page page;

    private final Locator firstNameInput;
    private final Locator lastNameInput;
    private final Locator businessNameInput;
    private final Locator passwordInput;
    private final Locator confirmPasswordInput;
    private final Locator continueBtn;

    public SignUpAccountPage(Page page) {
        this.page = page;

        firstNameInput = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter your first name"));

        lastNameInput = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter your last name"));

        businessNameInput = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter business name"));

        passwordInput = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter a password"));

        confirmPasswordInput = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Confirm your password"));

        continueBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Continue"));
    }

    public SignUpAccountPage setFirstName(String v) { firstNameInput.fill(v); return this; }
    public SignUpAccountPage setLastName(String v) { lastNameInput.fill(v); return this; }
    public SignUpAccountPage setBusinessName(String v) { businessNameInput.fill(v); return this; }
    public SignUpAccountPage setPassword(String v) { passwordInput.fill(v); return this; }
    public SignUpAccountPage setConfirmPassword(String v) { confirmPasswordInput.fill(v); return this; }

    public SignUpVerifyPage continueToVerify() {
        continueBtn.click();
        return new SignUpVerifyPage(page);
    }
}
