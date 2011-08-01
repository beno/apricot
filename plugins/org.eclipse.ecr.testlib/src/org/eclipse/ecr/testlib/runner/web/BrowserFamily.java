/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.eclipse.ecr.testlib.runner.web;

import org.openqa.selenium.Speed;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public enum BrowserFamily {

    FIREFOX, IE, CHROME, HTML_UNIT, HTML_UNIT_JS;

    public DriverFactory getDriverFactory() {
        switch (this) {
        case FIREFOX:
            return new FirefoxDriverFactory();
        case IE:
            return new IEDriverFactory();
        case CHROME:
            return new ChromeDriverFactory();
        case HTML_UNIT_JS:
            return new HtmlUnitJsDriverFactory();
        default:
            return new HtmlUnitDriverFactory();
        }
    }

    class FirefoxDriverFactory implements DriverFactory {
        @Override
        public WebDriver createDriver() {
            FirefoxDriver ff = new FirefoxDriver();
            ff.manage().setSpeed(Speed.FAST);
            return ff;
        }
        @Override
        public void disposeDriver(WebDriver driver) {
        }
        @Override
        public BrowserFamily getBrowserFamily() {
            return BrowserFamily.this;
        }
    }

    class ChromeDriverFactory implements DriverFactory {
        @Override
        public WebDriver createDriver() {
            ChromeDriver ff = new ChromeDriver();
            ff.manage().setSpeed(Speed.FAST);
            return ff;
        }
        @Override
        public void disposeDriver(WebDriver driver) {
        }
        @Override
        public BrowserFamily getBrowserFamily() {
            return BrowserFamily.this;
        }
    }

    class IEDriverFactory implements DriverFactory {
        @Override
        public WebDriver createDriver() {
            InternetExplorerDriver driver = new InternetExplorerDriver();
            driver.manage().setSpeed(Speed.FAST);
            return driver;
        }
        @Override
        public void disposeDriver(WebDriver driver) {
        }
        @Override
        public BrowserFamily getBrowserFamily() {
            return BrowserFamily.this;
        }
    }

    class HtmlUnitDriverFactory implements DriverFactory {
        @Override
        public WebDriver createDriver() {
            return new HtmlUnitDriver();
        }
        @Override
        public void disposeDriver(WebDriver driver) {
        }
        @Override
        public BrowserFamily getBrowserFamily() {
            return BrowserFamily.this;
        }
    }

    class HtmlUnitJsDriverFactory implements DriverFactory {
        @Override
        public WebDriver createDriver() {
            HtmlUnitDriver driver = new HtmlUnitDriver();
            driver.setJavascriptEnabled(true);
            return driver;
        }
        @Override
        public void disposeDriver(WebDriver driver) {
        }
        @Override
        public BrowserFamily getBrowserFamily() {
            return BrowserFamily.this;
        }
    }

//    private WebDriver _old_makeFirefoxDriver() {
//        String Xport = System.getProperty("nuxeo.xvfb.id", ":0");
//        File firefoxPath = new File(System.getProperty("firefox.path",
//                "/usr/bin/firefox"));
//        FirefoxBinary firefox = new FirefoxBinary(firefoxPath);
//        firefox.setEnvironmentProperty("DISPLAY", Xport);
//        WebDriver driver = new FirefoxDriver(firefox, null);
//        //driver.setVisible(false);
//        driver.manage().setSpeed(Speed.FAST);
//        return driver;
//    }

}
