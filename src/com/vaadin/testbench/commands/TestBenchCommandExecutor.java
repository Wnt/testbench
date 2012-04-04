package com.vaadin.testbench.commands;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;

import com.google.common.collect.ImmutableMap;
import com.vaadin.testbench.Parameters;
import com.vaadin.testbench.screenshot.ImageComparison;
import com.vaadin.testbench.screenshot.ReferenceNameGenerator;

/**
 * @author jonatan
 * 
 */
public class TestBenchCommandExecutor implements TestBenchCommands {
    private static Logger getLogger() {
        return Logger.getLogger(TestBenchCommandExecutor.class.getName());
    }

    private final WebDriver actualDriver;
    private final ImageComparison imageComparison;
    private final ReferenceNameGenerator referenceNameGenerator;

    public TestBenchCommandExecutor(WebDriver actualDriver,
            ImageComparison imageComparison,
            ReferenceNameGenerator referenceNameGenerator) {
        this.actualDriver = actualDriver;
        this.imageComparison = imageComparison;
        this.referenceNameGenerator = referenceNameGenerator;
    }

    protected Response execute(String driverCommand, Map<String, ?> parameters) {
        try {
            Method exec = RemoteWebDriver.class.getMethod("execute",
                    String.class, Map.class);
            exec.setAccessible(true);
            return (Response) exec.invoke(actualDriver, driverCommand,
                    parameters);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.testbench.commands.TestBenchCommands#setTestName(java.lang
     * .String)
     */
    @Override
    public void setTestName(String testName) {
        if (actualDriver instanceof RemoteWebDriver) {
            execute(TestBenchCommands.SET_TEST_NAME,
                    ImmutableMap.of("name", testName));
        } else {
            getLogger().info(
                    String.format("Currently running \"%s\"", testName));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.testbench.commands.TestBenchCommands#getRemoteControlName()
     */
    @Override
    public String getRemoteControlName() {
        InetAddress ia = null;
        try {
            if (actualDriver instanceof RemoteWebDriver) {
                RemoteWebDriver rwd = (RemoteWebDriver) actualDriver;
                if (rwd.getCommandExecutor() instanceof HttpCommandExecutor) {
                    ia = InetAddress.getByName(((HttpCommandExecutor) rwd
                            .getCommandExecutor()).getAddressOfRemoteServer()
                            .getHost());
                }
            } else {
                ia = InetAddress.getLocalHost();
            }
        } catch (UnknownHostException e) {
            getLogger().log(Level.WARNING,
                    "Could not find name of remote control", e);
            return "unknown";
        }

        if (ia != null) {
            return String.format("%s (%s)", ia.getCanonicalHostName(),
                    ia.getHostAddress());
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.testbench.commands.TestBenchElementCommands#expectDialog()
     */
    @Override
    public void expectDialog(WebElement element, Keys... modifierKeysPressed) {
        Actions actions = new Actions(actualDriver);
        // Press modifier key(s)
        for (Keys key : modifierKeysPressed) {
            actions = actions.keyDown(key);
        }
        actions = actions.click(element);
        // Release modifier key(s)
        for (Keys key : modifierKeysPressed) {
            actions = actions.keyUp(key);
        }
        actions.perform();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.testbench.commands.TestBenchCommands#closeNotification(org
     * .openqa.selenium.WebElement)
     */
    @Override
    public boolean closeNotification(WebElement element) {
        element.click();
        // Wait for 5000 ms or until the element is no longer visible.
        int times = 0;
        while (element.isDisplayed() || times > 25) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            times++;
        }
        return element.isDisplayed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.testbench.commands.TestBenchElementCommands#showTooltip()
     */
    @Override
    public void showTooltip(WebElement element) {
        new Actions(actualDriver).moveToElement(element).perform();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.testbench.commands.TestBenchCommands#scroll(org.openqa.selenium
     * .WebElement, int)
     */
    @Override
    public void scroll(WebElement element, int scrollTop) {
        JavascriptExecutor js = (JavascriptExecutor) actualDriver;
        js.executeScript("arguments[0].setScrollTop(arguments[1])", element,
                scrollTop);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.testbench.commands.TestBenchCommands#scrollLeft(org.openqa
     * .selenium.WebElement, int)
     */
    @Override
    public void scrollLeft(WebElement element, int scrollLeft) {
        JavascriptExecutor js = (JavascriptExecutor) actualDriver;
        js.executeScript("arguments[0].setScrollLeft(arguments[1])", element,
                scrollLeft);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.testbench.commands.TestBenchCommands#waitForVaadin()
     */
    @Override
    public void waitForVaadin() {
        // @formatter:off
        String isVaadinFinished =
                "if (window.vaadin == null) {" + 
                "  return true;" +
                "}" +
                "var clients = window.vaadin.clients;" + 
                "if (clients) {" +
                "  for (var client in clients) {" + 
                "    if (clients[client].isActive()) {" + 
                "      return false;" +
                "    }" +
                "  }" + 
                "  return true;" +
                "} else {" + 
                   // A Vaadin connector was found so this is most likely a Vaadin
                   // application. Keep waiting.
                "  return false;" +
                "}";
        // @formatter:on
        JavascriptExecutor js = (JavascriptExecutor) actualDriver;
        long timeoutTime = System.currentTimeMillis() + 20000;
        boolean finished = false;
        while (System.currentTimeMillis() < timeoutTime && !finished) {
            finished = (Boolean) js.executeScript(isVaadinFinished);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.testbench.commands.TestBenchCommands#screenshotEqualToReference
     * (java.lang.String)
     */
    @Override
    public boolean compareScreen(String referenceId) throws IOException {
        String referenceName = referenceNameGenerator.generateName(referenceId,
                ((HasCapabilities) actualDriver).getCapabilities());

        for (int times = 0; times < Parameters.getMaxRetries(); times++) {
            BufferedImage screenshotImage = ImageIO
                    .read(new ByteArrayInputStream(
                            ((TakesScreenshot) actualDriver)
                                    .getScreenshotAs(OutputType.BYTES)));
            boolean equal = imageComparison.imageEqualToReference(
                    screenshotImage, referenceName,
                    Parameters.getScreenshotComparisonTolerance(),
                    Parameters.isCaptureScreenshotOnFailure());
            if (equal) {
                return true;
            }
        }
        return false;
    }

}
