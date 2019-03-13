package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.Renderer;
import com.mercadopago.android.px.internal.view.RendererFactory;

public class PaymentResultRenderer extends Renderer<PaymentResultContainer> {

    @Override
    public View render(@NonNull final PaymentResultContainer component, @NonNull final Context context,
        final ViewGroup parent) {

        final View view;

        if (component.isLoading()) {
            view = RendererFactory.create(context, component.getLoadingComponent()).render(parent);
        } else {

            view = inflate(R.layout.px_payment_result_container, parent);

            final ViewGroup parentViewGroup = view.findViewById(R.id.mpsdkPaymentResultContainer);

            final Header headerComponent = component.getHeaderComponent(context);

            ViewUtils.addCancelToolbar(parentViewGroup, headerComponent.props.background);

            RendererFactory.create(context, headerComponent).render(parentViewGroup);

            if (component.hasBodyComponent()) {
                RendererFactory.create(context, component.getBodyComponent()).render(parentViewGroup);
            }

            parentViewGroup.addView(component.getFooterContainer().render(parentViewGroup));
        }

        return view;
    }
}