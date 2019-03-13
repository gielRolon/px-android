package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.EscManager;
import com.mercadopago.android.px.internal.util.EscUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

public class EscManagerImp implements EscManager {

    @NonNull private final MercadoPagoESC mercadoPagoESC;

    public EscManagerImp(@NonNull final MercadoPagoESC mercadoPagoESC) {
        this.mercadoPagoESC = mercadoPagoESC;
    }

    @Override
    public boolean hasEsc(@NonNull final Card card) {
        return !TextUtil.isEmpty(mercadoPagoESC.getESC(card.getId()));
    }

    @Override
    public boolean manageEscForPayment(final List<PaymentData> paymentDataList, final String paymentStatus,
        final String paymentStatusDetail) {

        boolean result = false;
        for (final PaymentData paymentData : paymentDataList) {
            if (EscUtil.shouldDeleteEsc(paymentData, paymentStatus,
                paymentStatusDetail)) {
                mercadoPagoESC.deleteESCWith(paymentData.getToken().getCardId());
            } else if (EscUtil.shouldStoreESC(paymentData, paymentStatus, paymentStatusDetail)) {
                mercadoPagoESC.saveESCWith(paymentData.getToken().getCardId(), paymentData.getToken().getEsc());
            }

            result |= EscUtil.isInvalidEscPayment(paymentData, paymentStatus, paymentStatusDetail);
        }

        return result;
    }

    @Override
    public boolean manageEscForError(final MercadoPagoError error, final List<PaymentData> paymentDataList) {
        boolean result = false;

        for (final PaymentData paymentData : paymentDataList) {
            final boolean isInvalidEsc = paymentData.containsCardInfo() && EscUtil.isErrorInvalidPaymentWithEsc(error);
            if (isInvalidEsc) {
                mercadoPagoESC.deleteESCWith(paymentData.getToken().getCardId());
            }
            result |= isInvalidEsc;
        }

        return result;
    }
}
