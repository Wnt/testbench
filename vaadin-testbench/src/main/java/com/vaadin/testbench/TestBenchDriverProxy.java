package com.vaadin.testbench;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;

import com.vaadin.testbench.By.ByVaadin;
import com.vaadin.testbench.commands.TestBenchCommandExecutor;
import com.vaadin.testbench.screenshot.ImageComparison;
import com.vaadin.testbench.screenshot.ReferenceNameGenerator;

public class TestBenchDriverProxy extends TestBenchCommandExecutor implements
        WebDriver, WrapsDriver, HasTestBenchCommandExecutor, HasDriver {

    private final WebDriver actualDriver;

    /**
     * Constructs a TestBenchDriverProxy using the provided web driver for the
     * actual driving.
     * 
     * @param webDriver
     */
    protected TestBenchDriverProxy(WebDriver webDriver) {
        super(webDriver, new ImageComparison(), new ReferenceNameGenerator());
        actualDriver = webDriver;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.internal.WrapsDriver#getWrappedDriver()
     */
    @Override
    public WebDriver getWrappedDriver() {
        return actualDriver;
    }

    // ----------------- WebDriver methods for convenience.

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#close()
     */
    @Override
    public void close() {
        actualDriver.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#findElement(org.openqa.selenium.By)
     */
    @Override
    public WebElement findElement(By arg0) {
        if (arg0 instanceof ByVaadin) {
            return TestBenchElement.wrapElement(arg0.findElement(this), this);
        }
        return TestBenchElement.wrapElement(actualDriver.findElement(arg0),
                this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#findElements(org.openqa.selenium.By)
     */
    @Override
    public List<WebElement> findElements(By arg0) {

        List<WebElement> elements = new ArrayList<WebElement>();

        // We can Wrap It!
        if (arg0 instanceof ByVaadin) {
            elements.addAll(TestBenchElement.wrapElements(
                    arg0.findElements(this), this));
        } else {
            elements.addAll(TestBenchElement.wrapElements(
                    actualDriver.findElements(arg0), this));
        }

        return elements;
    }

    /**
     * Finds a list of elements by a Vaadin selector string.
     * 
     * @param selector
     *            TestBench4 style Vaadin selector.
     * @param context
     *            a suitable search context - either a
     *            {@link TestBenchDriverProxy} or a {@link TestBenchElement}
     *            instance.
     * @return the list of elements identified by the selector
     */
    protected static List<WebElement> findElementsByVaadinSelector(
            String selector, SearchContext context) {

        final String errorString = "Vaadin could not find elements with the selector "
                + selector;

        // Construct elementSelectionString script fragment based on type of
        // search context
        String elementSelectionString = "var element = clients[client].getElementsByPath";
        if (context instanceof WebDriver) {
            elementSelectionString += "(arguments[0]);";
        } else {
            elementSelectionString += "StartingAt(arguments[0], arguments[1]);";
        }

        String findByVaadinScript = "var clients = window.vaadin.clients;"
                + "for (client in clients) {" + elementSelectionString
                + "  if (element) {" + " return element;" + "  }" + "}"
                + "return null;";

        WebDriver driver = ((HasDriver) context).getDriver();

        JavascriptExecutor jse = (JavascriptExecutor) driver;
        List<WebElement> elements = new ArrayList<WebElement>();

        if (selector.contains("::")) {
            // We've been given specifications to access a specific client on
            // the page; the client ApplicationConnection is managed by the
            // JavaScript running on the page, so we use the driver's
            // JavaScriptExecutor to query further...
            String client = selector.substring(0, selector.indexOf("::"));
            String path = selector.substring(selector.indexOf("::") + 2);
            try {
                Object output = jse
                        .executeScript("return window.vaadin.clients." + client
                                + ".getElementsByPath(\"" + path + "\");");
                if (output instanceof List) {
                    elements.addAll(extractWebElementsFromList((List<?>) output));
                }
            } catch (Exception e) {
                throw new NoSuchElementException(errorString, e);
            }
        } else {
            try {
                if (context instanceof WebDriver) {
                    Object output = jse.executeScript(findByVaadinScript,
                            selector);
                    if (output instanceof List) {
                        elements.addAll(extractWebElementsFromList((List<?>) output));
                    }
                } else {
                    Object output = jse.executeScript(findByVaadinScript,
                            selector, context);
                    if (output instanceof List) {
                        elements.addAll(extractWebElementsFromList((List<?>) output));
                    }
                }
            } catch (Exception e) {
                throw new NoSuchElementException(errorString, e);
            }
        }

        if (elements.isEmpty()) {
            throw new NoSuchElementException(
                    errorString,
                    new Exception(
                            "Client could not identify elements with the provided selector"));
        }

        return elements;
    }

    private static List<WebElement> extractWebElementsFromList(
            List<?> elementList) {
        List<WebElement> result = new ArrayList<WebElement>();
        for (Object o : elementList) {
            if (null != o && o instanceof WebElement) {
                result.add((WebElement) o);
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#get(java.lang.String)
     */
    @Override
    public void get(String arg0) {
        actualDriver.get(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#getCurrentUrl()
     */
    @Override
    public String getCurrentUrl() {
        return actualDriver.getCurrentUrl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#getPageSource()
     */
    @Override
    public String getPageSource() {
        return actualDriver.getPageSource();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#getTitle()
     */
    @Override
    public String getTitle() {
        return actualDriver.getTitle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#getWindowHandle()
     */
    @Override
    public String getWindowHandle() {
        return actualDriver.getWindowHandle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#getWindowHandles()
     */
    @Override
    public Set<String> getWindowHandles() {
        return actualDriver.getWindowHandles();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#manage()
     */
    @Override
    public Options manage() {
        return actualDriver.manage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#navigate()
     */
    @Override
    public Navigation navigate() {
        return actualDriver.navigate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#quit()
     */
    @Override
    public void quit() {
        actualDriver.quit();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openqa.selenium.WebDriver#switchTo()
     */
    @Override
    public TargetLocator switchTo() {
        return actualDriver.switchTo();
    }

    @Override
    public SearchContext getContext() {
        return this;
    }

    @Override
    public TestBenchCommandExecutor getTestBenchCommandExecutor() {
        return this;
    }

    @Override
    public WebDriver getDriver() {
        return this;
    }

}