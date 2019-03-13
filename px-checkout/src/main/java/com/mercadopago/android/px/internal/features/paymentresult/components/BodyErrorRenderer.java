package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.internal.view.Renderer;

public class BodyErrorRenderer extends Renderer<BodyError> {

    @Override
    public View render(@NonNull final BodyError component, @NonNull final Context context, final ViewGroup parent) {
        final View bodyErrorView = inflate(R.layout.px_payment_result_body_error, parent);
        final ViewGroup bodyViewGroup = bodyErrorView.findViewById(R.id.bodyErrorContainer);
        final MPTextView titleTextView = bodyErrorView.findViewById(R.id.paymentResultBodyErrorTitle);
        final MPTextView descriptionTextView = bodyErrorView.findViewById(R.id.paymentResultBodyErrorDescription);
        final AppCompatButton actionButton = bodyErrorView.findViewById(R.id.paymentResultBodyErrorAction);
        final View middleDivider = bodyErrorView.findViewById(R.id.bodyErrorMiddleDivider);
        final MPTextView secondaryTitleTextView = bodyErrorView.findViewById(R.id.bodyErrorSecondaryTitle);
        final View bottomDivider = bodyErrorView.findViewById(R.id.bodyErrorBottomDivider);

        setText(titleTextView, component.getTitle(context));
        setText(descriptionTextView, component.getDescription(context));

        if (component.getTitle(context).isEmpty()) {
            final LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            final int marginTop = (int) context.getResources().getDimension(R.dimen.px_l_margin);
            params.setMargins(0, marginTop, 0, 0);
            descriptionTextView.setLayoutParams(params);
        }

        actionButton.setVisibility(View.GONE);
        middleDivider.setVisibility(View.GONE);
        secondaryTitleTextView.setVisibility(View.GONE);
        bottomDivider.setVisibility(View.GONE);

        ViewUtils.stretchHeight(bodyViewGroup);

        return bodyErrorView;
    }
}
