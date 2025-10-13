package org.example;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;

import java.util.Arrays;

public class HeadlessChromeOptions implements OptionsFactory {

    @Override
    public Options getOptions() {
        return new Options()
                .setHeadless(false) //показує вікно
                .setLaunchOptions(
                new BrowserType.LaunchOptions()
                        //.setSlowMo(1000) //
                        .setArgs(Arrays.asList("--start-maximized"))
        )
                .setContextOptions(new Browser.NewContextOptions().setViewportSize(null));  // КЛЮЧОВЕ: вимикаємо фіксований viewport
    }
}
