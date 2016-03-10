package com.mercadopago.test.rules;

import android.app.Activity;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;

import com.mercadopago.test.MockedHttpClient;
import com.mercadopago.test.StaticMock;
import com.mercadopago.util.HttpClientUtil;
import com.mercadopago.util.JsonUtil;

/**
 * Created by mreverter on 29/2/16.
 */
public class MockedApiTestRule<A extends Activity> extends IntentsTestRule<A> {

    private MockedHttpClient mockedHttpClient;

    public MockedApiTestRule(Class<A> activityClass) {
        super(activityClass);
        setUpMockedClient();
    }

    public MockedApiTestRule(Class<A> activityClass, boolean initialTouchMode) {
        super(activityClass, initialTouchMode);
        setUpMockedClient();
    }

    public MockedApiTestRule(Class<A> activityClass, boolean initialTouchMode, boolean launchActivity) {
        super(activityClass, initialTouchMode, launchActivity);
        setUpMockedClient();
    }

    private void setUpMockedClient() {
        mockedHttpClient = new MockedHttpClient();
        HttpClientUtil.bindClient(mockedHttpClient);
    }

    public <T> void addApiResponseToQueue(T response, int statusCode, String reason) {
        String jsonResponse = JsonUtil.getInstance().toJson(response);
        mockedHttpClient.addResponseToQueue(jsonResponse, statusCode, reason);
    }

    public void addApiResponseToQueue(String jsonResponse, int statusCode, String reason) {
        mockedHttpClient.addResponseToQueue(jsonResponse, statusCode, reason);
    }

    @Override
    protected void afterActivityFinished() {
        super.afterActivityFinished();
        HttpClientUtil.unbindClient();
    }
}