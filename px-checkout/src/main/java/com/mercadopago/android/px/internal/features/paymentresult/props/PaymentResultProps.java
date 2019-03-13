package com.mercadopago.android.px.internal.features.paymentresult.props;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.configuration.PaymentResultScreenConfiguration;
import com.mercadopago.android.px.model.Instruction;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentResult;

public class PaymentResultProps {

    public final PaymentResult paymentResult;
    public final Instruction instruction;
    public final String headerMode;
    public final boolean loading;
    public final String processingMode;
    public final String currencyId;
    @NonNull private final PaymentResultScreenConfiguration paymentResultScreenPreferences;

    /* default */ PaymentResultProps(@NonNull final Builder builder) {
        paymentResult = builder.paymentResult;
        headerMode = builder.headerMode;
        instruction = builder.instruction;
        loading = builder.loading;
        processingMode = builder.processingMode;
        currencyId = builder.currencyId;
        paymentResultScreenPreferences = builder.paymentResultScreenConfiguration;
    }

    public Builder toBuilder() {
        return new Builder(paymentResultScreenPreferences)
            .setCurrencyId(currencyId)
            .setPaymentResult(paymentResult)
            .setHeaderMode(headerMode)
            .setInstruction(instruction)
            .setLoading(loading)
            .setProcessingMode(processingMode);
    }


    private boolean isStatusApproved() {
        return paymentResult != null && paymentResult.getPaymentStatus().equals(Payment.StatusCodes.STATUS_APPROVED);
    }

    private boolean isStatusRejected() {
        return paymentResult != null && paymentResult.getPaymentStatus().equals(Payment.StatusCodes.STATUS_REJECTED);
    }

    public boolean hasCustomizedLabel() {

        if (isApprovedLabelValidState()) {
            return paymentResultScreenPreferences.getApprovedLabelText() != null &&
                !paymentResultScreenPreferences.getApprovedLabelText().isEmpty();
        } else if (isRejectedLabelValidState()) {
            return !paymentResultScreenPreferences.isRejectedLabelTextEnabled();
        }

        return false;
    }

    public String getPreferenceLabel() {

        if (isApprovedLabelValidState()) {
            return paymentResultScreenPreferences.getApprovedLabelText();
        } else if (isRejectedLabelValidState() && !paymentResultScreenPreferences.isRejectedLabelTextEnabled()) {
            return "";
        }
        return "";
    }

    private boolean isApprovedLabelValidState() {
        return isStatusApproved();
    }

    private boolean isRejectedLabelValidState() {
        return isStatusRejected();
    }

    public boolean hasCustomizedBadge() {
        if (isStatusApproved()) {
            return paymentResultScreenPreferences.getApprovedBadge() != null &&
                !paymentResultScreenPreferences.getApprovedBadge().isEmpty();
        }
        return false;
    }

    public String getPreferenceBadge() {
        if (isStatusApproved()) {
            return paymentResultScreenPreferences.getApprovedBadge();
        }
        return "";
    }

    public boolean hasInstructions() {
        return instruction != null;
    }

    public String getInstructionsTitle() {
        if (hasInstructions()) {
            return instruction.getTitle();
        } else {
            return "";
        }
    }

    @NonNull
    public PaymentResultScreenConfiguration getPaymentResultScreenPreference() {
        return paymentResultScreenPreferences;
    }

    public static class Builder {

        /* default */ @NonNull final PaymentResultScreenConfiguration paymentResultScreenConfiguration;
        /* default */ PaymentResult paymentResult;
        /* default */ Instruction instruction;
        /* default */ String headerMode = HeaderProps.HEADER_MODE_WRAP;
        /* default */ boolean loading = true;
        /* default */ String processingMode;
        /* default */ String currencyId;

        public Builder(@NonNull final PaymentResultScreenConfiguration paymentResultScreenConfiguration) {
            this.paymentResultScreenConfiguration = paymentResultScreenConfiguration;
        }

        public Builder setPaymentResult(@NonNull final PaymentResult paymentResult) {
            this.paymentResult = paymentResult;
            return this;
        }

        public Builder setHeaderMode(@NonNull final String headerMode) {
            this.headerMode = headerMode;
            return this;
        }

        public Builder setInstruction(@NonNull final Instruction instruction) {
            this.instruction = instruction;
            return this;
        }

        public Builder setLoading(final boolean loading) {
            this.loading = loading;
            return this;
        }

        public Builder setProcessingMode(final String processingMode) {
            this.processingMode = processingMode;
            return this;
        }

        public PaymentResultProps build() {
            return new PaymentResultProps(this);
        }

        public Builder setCurrencyId(final String currencyId) {
            this.currencyId = currencyId;
            return this;
        }
    }
}
