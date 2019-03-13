package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.configuration.PaymentResultScreenConfiguration;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.actions.ChangePaymentMethodAction;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.Footer;
import com.mercadopago.android.px.internal.view.NextAction;
import com.mercadopago.android.px.internal.view.RecoverPaymentAction;
import com.mercadopago.android.px.mocks.PaymentResults;
import com.mercadopago.android.px.model.PaymentResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FooterPaymentResultTest {

    private static final String LABEL_CONTINUE = "Continue";
    private static final String LABEL_CHANGE = "Change payment method";
    private static final String LABEL_ALREADY_AUTHORIZED = "Already authorized";
    private static final String LABEL_REVIEW_TC_INFO = "REVIEW INFO";

    @Mock private Context context;
    @Mock private ActionDispatcher actionDispatcher;

    @Before
    public void setup() {

        new PaymentResultScreenConfiguration.Builder().build();
    }

    @Test
    public void testApproved() {

        when(context.getString(R.string.px_continue_shopping)).thenReturn(LABEL_CONTINUE);
        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
        final FooterPaymentResult footerPaymentResult =
            new FooterPaymentResult(paymentResult, actionDispatcher);

        final Footer.Props props = footerPaymentResult.getFooterProps(context);

        assertNotNull(props);
        assertNull(props.buttonAction);
        assertNotNull(props.linkAction);

        assertEquals(LABEL_CONTINUE, props.linkAction.label);
        assertNotNull(props.linkAction.action);
        assertTrue(props.linkAction.action instanceof NextAction);
    }

    @Test
    public void testRejectedBadFilledDatePaymentResult() {

        when(context.getString(R.string.px_text_pay_with_other_method)).thenReturn(LABEL_CHANGE);
        when(context.getString(R.string.px_text_some_card_data_is_incorrect)).thenReturn(LABEL_REVIEW_TC_INFO);

        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledDatePaymentResult();
        final FooterPaymentResult footerPaymentResult =
            new FooterPaymentResult(paymentResult, actionDispatcher);

        final Footer.Props props = footerPaymentResult.getFooterProps(context);

        assertNotNull(props);
        assertNotNull(props.buttonAction);
        assertEquals(LABEL_REVIEW_TC_INFO, props.buttonAction.label);
        assertNotNull(props.buttonAction.action);
        assertTrue(props.buttonAction.action instanceof RecoverPaymentAction);

        assertNotNull(props.linkAction);
        assertEquals(LABEL_CHANGE, props.linkAction.label);
        assertNotNull(props.linkAction.action);
        assertTrue(props.linkAction.action instanceof ChangePaymentMethodAction);
    }

    @Test
    public void testRejectedCallForAuth() {
        when(context.getString(R.string.px_text_authorized_call_for_authorize)).thenReturn(LABEL_ALREADY_AUTHORIZED);
        final PaymentResult paymentResult = PaymentResults.getStatusCallForAuthPaymentResult();
        final FooterPaymentResult footerPaymentResult = new FooterPaymentResult(paymentResult, actionDispatcher);
        final Footer.Props props = footerPaymentResult.getFooterProps(context);

        assertNotNull(props);
        assertNotNull(props.buttonAction);
        assertEquals(LABEL_ALREADY_AUTHORIZED, props.buttonAction.label);
        assertNotNull(props.buttonAction.action);
        assertTrue(props.buttonAction.action instanceof RecoverPaymentAction);
        assertNotNull(props.linkAction);
        assertTrue(LABEL_CHANGE, props.linkAction.action instanceof ChangePaymentMethodAction);
    }

    @Test
    public void testRejected() {
        when(context.getString(R.string.px_text_pay_with_other_method)).thenReturn(LABEL_CHANGE);
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedOtherPaymentResult();
        final FooterPaymentResult footerPaymentResult = new FooterPaymentResult(paymentResult, actionDispatcher);
        final Footer.Props props = footerPaymentResult.getFooterProps(context);

        assertNotNull(props);
        assertNotNull(props.buttonAction);
        assertEquals(LABEL_CHANGE, props.buttonAction.label);
        assertNotNull(props.buttonAction.action);
        assertTrue(props.buttonAction.action instanceof ChangePaymentMethodAction);
        assertNull(props.linkAction);
    }
}