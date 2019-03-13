package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.paymentresult.props.BodyErrorProps;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.Component;
import com.mercadopago.android.px.internal.view.RecoverPaymentAction;
import com.mercadopago.android.px.model.Payment;

public class BodyError extends Component<BodyErrorProps, Void> {

    public BodyError(@NonNull final BodyErrorProps props,
        @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }

    public String getTitle(final Context context) {
        if (isCallForAuthorize()) {
            return context.getString(R.string.px_text_how_can_authorize);
        }
        if (isRejectedWithTitle() || isPendingWithTitle()) {
            return context.getString(R.string.px_what_can_do);
        }
        return TextUtil.EMPTY;
    }

    public String getDescription(final Context context) {
        switch (props.status) {
        case Payment.StatusCodes.STATUS_PENDING:
        case Payment.StatusCodes.STATUS_IN_PROCESS:
            return getPendingOrInProcessDescription(context);
        case Payment.StatusCodes.STATUS_REJECTED:
            return getRejectedDescription(context);
        default:
            return TextUtil.EMPTY;
        }
    }

    @NonNull
    private String getPendingOrInProcessDescription(final Context context) {
        switch (props.statusDetail) {
        case Payment.StatusDetail.STATUS_DETAIL_PENDING_CONTINGENCY:
            return context.getString(R.string.px_error_description_contingency);
        case Payment.StatusDetail.STATUS_DETAIL_PENDING_REVIEW_MANUAL:
            return context.getString(R.string.px_error_description_review_manual);
        default:
            return TextUtil.EMPTY;
        }
    }

    private String getRejectedDescription(final Context context) {
        switch (props.statusDetail) {
        case Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE:
            return context.getString(R.string.px_error_description_call);
        case Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CARD_DISABLED:
            return context.getString(R.string.px_error_description_card_disabled, props.paymentMethodName);
        case Payment.StatusDetail.STATUS_DETAIL_REJECTED_REJECTED_BY_BANK:
            return context.getString(R.string.px_error_description_by_bank);
        case Payment.StatusDetail.STATUS_DETAIL_REJECTED_REJECTED_INSUFFICIENT_DATA:
            return context.getString(R.string.px_error_description_insufficient_data);
        case Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_DUPLICATED_PAYMENT:
            return context.getString(R.string.px_error_description_duplicated_payment);
        case Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_MAX_ATTEMPTS:
            return context.getString(R.string.px_error_description_max_attempts);
        case Payment.StatusDetail.STATUS_DETAIL_REJECTED_HIGH_RISK:
            return context.getString(R.string.px_error_description_high_risk);
        case Payment.StatusDetail.STATUS_DETAIL_REJECTED_BY_REGULATIONS:
            return context.getString(R.string.px_error_description_rejected_by_regulations);
        default:
            return TextUtil.EMPTY;
        }
    }

    private boolean isRejectedWithTitle() {
        return (props.status.equals(Payment.StatusCodes.STATUS_REJECTED) &&
            (props.statusDetail.equals(Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE) ||
                props.statusDetail.equals(Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CARD_DISABLED) ||
                props.statusDetail.equals(Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_INSUFFICIENT_AMOUNT) ||
                props.statusDetail.equals(Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_OTHER_REASON) ||
                props.statusDetail.equals(Payment.StatusDetail.STATUS_DETAIL_REJECTED_REJECTED_BY_BANK) ||
                props.statusDetail.equals(Payment.StatusDetail.STATUS_DETAIL_REJECTED_REJECTED_INSUFFICIENT_DATA) ||
                props.statusDetail.equals(Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_MAX_ATTEMPTS) ||
                props.statusDetail.equals(Payment.StatusDetail.STATUS_DETAIL_REJECTED_HIGH_RISK)));
    }

    private boolean isPendingWithTitle() {
        return ((props.status.equals(Payment.StatusCodes.STATUS_IN_PROCESS) ||
            props.status.equals(Payment.StatusCodes.STATUS_PENDING)) &&
            (props.statusDetail.equals(Payment.StatusDetail.STATUS_DETAIL_PENDING_CONTINGENCY) ||
                props.statusDetail.equals(Payment.StatusDetail.STATUS_DETAIL_PENDING_REVIEW_MANUAL)));
    }

    private boolean isCallForAuthorize() {
        return props.status.equals(Payment.StatusCodes.STATUS_REJECTED)
            && (props.statusDetail.equals(Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE));
    }

    public boolean hasActionForCallForAuth() {
        return isCallForAuthorize() && props.paymentMethodName != null && !props.paymentMethodName.isEmpty();
    }

    /* default */ void recoverPayment() {
        getDispatcher().dispatch(new RecoverPaymentAction());
    }
}
