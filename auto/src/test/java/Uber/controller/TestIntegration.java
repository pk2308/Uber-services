package Uber.controller;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTitle;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * 僅使用HtmlUnit進行整合測試。
 *
 * @author Kent Yeh
 */
@Test(groups = {"integrate"})
public class TestIntegration {

    private static final Logger logger = LogManager.getLogger(TestIntegration.class);
    private int httpPort = 80;
    private String contextPath = "";
    private WebClient webClient;
    private String captcha;
//    private HtmlPage myInfoPage;

    @BeforeClass
    @Parameters({"http.port", "contextPath", "captcha"})
    public void setup(@Optional("http.port") int httpPort,
            @Optional("contextPath") String contextPath,
            @Optional("captcha") String captcha) {
        this.httpPort = httpPort;
        this.captcha = captcha;
        logger.debug("http port is {}", httpPort);
        this.contextPath = contextPath;
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setDownloadImages(true);//4 loading captcha
    }

    @AfterClass
    public void tearDown() {
        if (this.webClient != null) {
            this.webClient.close();
        }
    }

    @Test(expectedExceptions = FailingHttpStatusCodeException.class)
    public void test404() throws IOException {
        String url = String.format("http://localhost:%d/%s/unknownpath/404.html", httpPort, contextPath);
        logger.debug("Integration Test: test404 with {}", url);
        HtmlPage page404 = webClient.getPage(url);
    }

    @Test
    public void testMyInfo() throws IOException {
        String url = String.format("http://localhost:%d/%s/user/myinfo", httpPort, contextPath);
        logger.debug("Test myinfo with {}", url);
        HtmlPage beforeInfoPage = webClient.getPage(url);
        HtmlForm form = beforeInfoPage.getFirstByXPath("//form");
        form.getInputByName("username").setValueAttribute("admin");
        form.getInputByName("password").setValueAttribute("admin");
        form.getInputByName("captcha").setValueAttribute(captcha);
        HtmlPage myInfoPage = form.getOneHtmlElementByAttribute("button", "type", "submit").click();
        HtmlTitle title = myInfoPage.getFirstByXPath("//title");
        assertThat("Fail to get My Info", title.getTextContent(), is(containsString("admin")));

    }

    @Test(dependsOnMethods = "testMyInfo")
    public void testUserLike() throws IOException, InterruptedException {
        String url = String.format("http://localhost:%d/%s/user/myinfo", httpPort, contextPath);
        HtmlPage myInfoPage = webClient.getPage(url);
        myInfoPage.executeJavaScript("like()");
        myInfoPage.executeJavaScript("dislike()");
        synchronized (myInfoPage) {
            myInfoPage.wait(1000);
        }
        DomElement span = myInfoPage.getElementById("like");
        assertThat("Fail to get User Like", span.getTextContent(), is("1"));
        span = myInfoPage.getElementById("dislike");
        assertThat("Fail to get User Like", span.getTextContent(), is("1"));
    }

    @Test(dependsOnMethods = "testUserLike")
    public void logout() throws IOException {
        String url = String.format("http://localhost:%d/%s/", httpPort, contextPath);
        logger.debug("Integration Test: logout with {}", url);
        HtmlPage homePage = webClient.getPage(url);
        HtmlForm form = homePage.getFirstByXPath("//form");
        homePage = form.getElementsByTagName("button").get(0).click();
        logger.debug("logout redirect to {}", homePage.getUrl());
        assertThat("logout failed ", homePage.getUrl().toString(), is(containsString("/index")));
    }
}
