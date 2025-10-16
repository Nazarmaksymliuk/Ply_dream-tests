package org.example.PageObjectModels.Authorization.SignIn;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class SignInPage {

    private final Page page;

    // Локатори
    private final Locator emailInput;
    private final Locator passwordInput;
    private final Locator signInButton;
    private final Locator title;
    private final Locator errorAlert;

    public SignInPage(Page page) {
        this.page = page;
        emailInput   = page.getByPlaceholder("Enter your company email address...");
        passwordInput= page.getByPlaceholder("Enter a password...");
        signInButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in"));
        title        = page.getByText("Welcome back, sign in!").first();
        errorAlert   = page.locator("div[role='alert']");
    }

    public void signIntoApplication(String email, String password) {
        emailInput.fill(email);
        passwordInput.fill(password);
        signInButton.click();
    }

    public String getTitle() {
        return title.innerText();
    }

    public void waitUntilErrorVisible() {
        errorAlert.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public boolean isErrorVisible() {
        return errorAlert.isVisible();
    }

    public String getTheErrorMessageText(){
        return errorAlert.innerText();
    }

    public Locator error() { return errorAlert; }
}
