package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.PaymentMethodPlugin;
import com.mercadopago.android.px.internal.adapters.PaymentMethodSearchItemAdapter;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.viewmodel.PaymentMethodViewModel;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CustomSearchItem;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearchItem;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.PaymentPreference;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface PaymentVaultView extends MvpView {

    void startSavedCardFlow(Card card);

    void showSelectedItem(PaymentMethodSearchItem item);

    void showProgress();

    void hideProgress();

    void showSearchItems(List<PaymentMethodViewModel> searchItems);

    void showError(MercadoPagoError mercadoPagoError, String requestOrigin);

    void setTitle(String title);

    void startCardFlow(Boolean automaticallySelection);

    void startPaymentMethodsSelection(final PaymentPreference paymentPreference);

    void finishPaymentMethodSelection(PaymentMethod selectedPaymentMethod);

    void showAmount(@NonNull final DiscountConfigurationModel discountModel,
        @NonNull final BigDecimal totalAmount,
        @NonNull final Site site);

    void hideAmountRow();

    void collectPayerInformation();

    void showDetailDialog(@NonNull final DiscountConfigurationModel discountModel);

    void showEmptyPaymentMethodsError();

    void showMismatchingPaymentMethodError();
}