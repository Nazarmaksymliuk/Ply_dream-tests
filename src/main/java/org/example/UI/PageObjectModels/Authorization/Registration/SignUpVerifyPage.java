package org.example.UI.PageObjectModels.Authorization.Registration;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.example.UI.PageObjectModels.Dashboard.DashboardPage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class SignUpVerifyPage {
    private final Page page;

    private final Locator otp1;
    private final Locator otp2;
    private final Locator otp3;
    private final Locator otp4;
    private final Locator otp5;
    private final Locator otp6;

    private final Locator businessTypeCheckbox; // назва може відрізнятись у тебе в UI
    private final Locator continueBtn;
    private final Locator getStartedBtn;

    public SignUpVerifyPage(Page page) {
        this.page = page;

        otp1 = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please enter OTP character 1"));
        otp2 = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please enter OTP character 2"));
        otp3 = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please enter OTP character 3"));
        otp4 = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please enter OTP character 4"));
        otp5 = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please enter OTP character 5"));
        otp6 = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Please enter OTP character 6"));

        // ⚠️ тут часто інша назва. Якщо не матчиться — заміниш setName(...)
        businessTypeCheckbox = page.getByRole(AriaRole.CHECKBOX,
                new Page.GetByRoleOptions().setName("Plumbing"));

        continueBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue"));
        getStartedBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Get started"));
    }

    public SignUpVerifyPage setOtpCode(String code6digits) {
        if (code6digits == null || !code6digits.matches("\\d{6}")) {
            throw new IllegalArgumentException("OTP code must be 6 digits, got: " + code6digits);
        }

        otp1.fill(String.valueOf(code6digits.charAt(0)));
        otp2.fill(String.valueOf(code6digits.charAt(1)));
        otp3.fill(String.valueOf(code6digits.charAt(2)));
        otp4.fill(String.valueOf(code6digits.charAt(3)));
        otp5.fill(String.valueOf(code6digits.charAt(4)));
        otp6.fill(String.valueOf(code6digits.charAt(5)));

        return this;
    }

    public SignUpVerifyPage chooseBusinessType() {
        // якщо чекбокс не видимий — тест впаде з нормальним меседжем
        assertThat(businessTypeCheckbox).isVisible();
        businessTypeCheckbox.check();
        return this;
    }

    public SignUpVerifyPage clickContinue() {
        continueBtn.click();
        return this;
    }

    public DashboardPage clickGetStarted() {
        getStartedBtn.click();
        return new DashboardPage(page);
    }
}
