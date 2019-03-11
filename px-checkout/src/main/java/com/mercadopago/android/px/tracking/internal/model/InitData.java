package com.mercadopago.android.px.tracking.internal.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.preferences.CheckoutPreference;

@SuppressWarnings("unused")
public final class InitData extends TrackingMapModel {

    @Nullable private final String checkoutPreferenceId;
    @Nullable private final CheckoutPreference checkoutPreference;
    private final boolean escEnabled;
    private final boolean expressEnabled;

    private InitData(@Nullable final String preferenceId,
        @Nullable final CheckoutPreference preference,
        @NonNull final AdvancedConfiguration advancedConfiguration) {
        checkoutPreferenceId = preferenceId;
        checkoutPreference = preference;
        escEnabled = advancedConfiguration.isEscEnabled();
        expressEnabled = advancedConfiguration.isExpressPaymentEnabled();
    }

    public static InitData from(@NonNull final PaymentSettingRepository paymentSettingRepository) {
        return new InitData(paymentSettingRepository.getCheckoutPreferenceId(),
            paymentSettingRepository.getCheckoutPreference(),
            paymentSettingRepository.getAdvancedConfiguration());
    }
}
