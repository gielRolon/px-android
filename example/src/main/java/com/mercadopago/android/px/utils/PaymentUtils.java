package com.mercadopago.android.px.utils;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.ExitAction;
import com.mercadopago.android.px.model.GenericPayment;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.example.R;

public final class PaymentUtils {

    private PaymentUtils() {
        //Do nothing
    }

    @NonNull
    public static BusinessPayment getBusinessPaymentApproved() {
        return new BusinessPayment.Builder(BusinessPayment.Decorator.APPROVED, Payment.StatusCodes.STATUS_APPROVED,
            Payment.StatusDetail.STATUS_DETAIL_ACCREDITED,
            R.drawable.px_icon_card, "Title")
            .setPrimaryButton(new ExitAction("Button Name", 23))
            .build();
    }

    @NonNull
    public static GenericPayment getGenericPaymentApproved() {
        return new GenericPayment.Builder(
            Payment.StatusCodes.STATUS_REJECTED,
            Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE
        ).setPaymentId(123L).createGenericPayment();
    }
}
