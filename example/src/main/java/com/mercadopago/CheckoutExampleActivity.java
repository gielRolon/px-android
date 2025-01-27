package com.mercadopago;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.mercadopago.android.px.core.CheckoutLazyInit;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.internal.view.MPButton;
import com.mercadopago.android.px.utils.ExamplesUtils;
import com.mercadopago.example.R;

import static com.mercadopago.android.px.utils.ExamplesUtils.resolveCheckoutResult;

public class CheckoutExampleActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private View mRegularLayout;
    private MPButton continueSimpleCheckout;
    private static final int REQ_CODE_CHECKOUT = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog()
            .build());

        setContentView(R.layout.activity_checkout_example);

        mRegularLayout = findViewById(R.id.regularLayout);

        final View lazy = findViewById(R.id.lazy_init);
        final View progress = findViewById(R.id.progress_bar);
        lazy.setOnClickListener(v ->  {
            progress.setVisibility(View.VISIBLE);
            new CheckoutLazyInit(ExamplesUtils.createBase()) {
                @Override
                public void fail(@NonNull final MercadoPagoCheckout mercadoPagoCheckout) {
                    progress.setVisibility(View.GONE);
                }

                @Override
                public void success(@NonNull final MercadoPagoCheckout mercadoPagoCheckout) {
                    progress.setVisibility(View.GONE);
                    mercadoPagoCheckout.startPayment(v.getContext(), REQUEST_CODE);
                }
            }.fetch(v.getContext());
        });

        continueSimpleCheckout = findViewById(R.id.continueButton);

        final View selectCheckoutButton = findViewById(R.id.select_checkout);

        selectCheckoutButton.setOnClickListener(
            v -> startActivity(new Intent(CheckoutExampleActivity.this, SelectCheckoutActivity.class)));

        continueSimpleCheckout.setOnClickListener(
            v -> ExamplesUtils.createBase().build().startPayment(CheckoutExampleActivity.this, REQUEST_CODE));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        resolveCheckoutResult(this, requestCode, resultCode, data, REQ_CODE_CHECKOUT);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        showRegularLayout();
    }

    private void showRegularLayout() {
        mRegularLayout.setVisibility(View.VISIBLE);
    }
}