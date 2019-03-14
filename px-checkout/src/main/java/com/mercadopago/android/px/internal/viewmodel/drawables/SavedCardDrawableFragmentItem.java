package com.mercadopago.android.px.internal.viewmodel.drawables;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import com.mercadopago.android.px.internal.viewmodel.CardHeaderConfiguration;

public class SavedCardDrawableFragmentItem implements DrawableFragmentItem {

    @NonNull public final String paymentMethodId;
    @NonNull public final CardHeaderConfiguration card;

    public SavedCardDrawableFragmentItem(@NonNull final String paymentMethodId, @NonNull final CardHeaderConfiguration card) {
        this.paymentMethodId = paymentMethodId;
        this.card = card;
    }

    @Override
    public Fragment draw(@NonNull final PaymentMethodFragmentDrawer drawer) {
        return drawer.draw(this);
    }
}
