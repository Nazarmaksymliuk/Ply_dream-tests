package org.example.UI.PageObjectModels.Authorization.SignIn;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class MFASignInPage {
    private final Page page;

    public final Locator mfaScreenTitle;


    public MFASignInPage(Page page) {
        this.page = page;
        this.mfaScreenTitle = page.getByText("Enter 6-digit code").or(page.locator("[class^=_title_]"));

    }

    public Locator isMFATextPresent() {
        return mfaScreenTitle;
    }

}
