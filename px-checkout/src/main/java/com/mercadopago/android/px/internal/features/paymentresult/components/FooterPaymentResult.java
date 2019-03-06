package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.actions.ChangePaymentMethodAction;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.Button;
import com.mercadopago.android.px.internal.view.CompactComponent;
import com.mercadopago.android.px.internal.view.Footer;
import com.mercadopago.android.px.internal.view.NextAction;
import com.mercadopago.android.px.internal.view.RecoverPaymentAction;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentResult;
import javax.annotation.Nonnull;

public class FooterPaymentResult extends CompactComponent<PaymentResult, ActionDispatcher> {

    /* default */ FooterPaymentResult(@NonNull final PaymentResult props, @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        return new Footer(getFooterProps(parent.getContext()), getActions()).render(parent);
    }

    @VisibleForTesting
    Footer.Props getFooterProps(@NonNull final Context context) {

        Button.Props buttonAction = null;
        Button.Props linkAction = null;

        if (props.isApproved()
            || props.isStatusPending()
            || props.isStatusInProcess()) {

            linkAction = new Button.Props(context.getString(R.string.px_continue_shopping), new NextAction());
        } else if (props.isStatusRejected()) {

            if (Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CARD_DISABLED
                .equals(props.getPaymentStatusDetail())) {

                buttonAction =
                    new Button.Props(context.getString(R.string.px_text_card_enabled), new RecoverPaymentAction());
                linkAction =
                    new Button.Props(context.getString(R.string.px_text_pay_with_other_method),
                        new ChangePaymentMethodAction());
            } else if (Payment.StatusDetail.isBadFilled(props.getPaymentStatusDetail())) {

                buttonAction =
                    new Button.Props(String.format(context.getString(R.string.px_text_some_card_data_is_incorrect),
                        props.getPaymentData().getPaymentMethod().getName()),
                        new RecoverPaymentAction());
                linkAction =
                    new Button.Props(context.getString(R.string.px_text_pay_with_other_method),
                        new ChangePaymentMethodAction());
            } else if (Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_DUPLICATED_PAYMENT
                .equals(props.getPaymentStatusDetail())) {
                linkAction = new Button.Props(context.getString(R.string.px_continue_shopping), new NextAction());
            } else {
                buttonAction =
                    new Button.Props(context.getString(R.string.px_text_pay_with_other_method),
                        new ChangePaymentMethodAction());
            }
        }

        return new Footer.Props(buttonAction, linkAction);
    }
}