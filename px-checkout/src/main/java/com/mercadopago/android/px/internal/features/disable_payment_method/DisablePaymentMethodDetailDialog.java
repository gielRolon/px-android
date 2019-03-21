package com.mercadopago.android.px.internal.features.disable_payment_method;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import com.mercadolibre.android.ui.widgets.MeliDialog;
import com.mercadopago.android.px.R;

public class DisablePaymentMethodDetailDialog extends MeliDialog {

    private static final String TAG = DisablePaymentMethodDetailDialog.class.getName();

    public static void showDialog(@NonNull final FragmentManager supportFragmentManager) {
        final DisablePaymentMethodDetailDialog discountDetailDialog = new DisablePaymentMethodDetailDialog();
        discountDetailDialog.show(supportFragmentManager, TAG);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final View closeButton = view.findViewById(R.id.ui_melidialog_close_button);
        closeButton.setVisibility(View.GONE);
        final View linkText = view.findViewById(R.id.px_dialog_detail_payment_method_disable_link);
        linkText.setOnClickListener(v -> dismiss());
    }

    @Override
    public int getContentView() {
        return R.layout.px_dialog_detail_payment_method_disable;
    }

}