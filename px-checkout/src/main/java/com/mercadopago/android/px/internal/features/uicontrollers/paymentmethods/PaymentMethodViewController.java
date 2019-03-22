package com.mercadopago.android.px.internal.features.uicontrollers.paymentmethods;

import android.view.View;
import com.mercadopago.android.px.internal.features.uicontrollers.CustomViewController;

public interface PaymentMethodViewController extends CustomViewController {
    void draw();

    void showSeparator();

    void setOnClickListener(View.OnClickListener listener);
}
