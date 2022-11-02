package com.example.transshift;

import static org.hamcrest.CoreMatchers.is;

import com.csse.transshift.RegisterActivity;

import org.junit.Assert;
import org.junit.Test;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testEmailValidity() {
        String testEmail = "manulthisuraka@gmail.com";
        Assert.assertThat(String.format("Email Address Validity Test failed for %s ", testEmail), RegisterActivity.checkEmailForValidity(testEmail), is(true));
    }

    @Test
    public void testEmailValidity1() {
        String testEmail = "manulthisurakagmailcom";
        Assert.assertThat(String.format("Email Address Validity Test failed for %s ", testEmail), RegisterActivity.checkEmailForValidity(testEmail), is(false));
    }
}