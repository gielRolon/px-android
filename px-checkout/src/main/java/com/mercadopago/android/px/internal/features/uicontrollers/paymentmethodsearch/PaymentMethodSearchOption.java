package com.mercadopago.android.px.internal.features.uicontrollers.paymentmethodsearch;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.ResourceUtil;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.model.PaymentMethodSearchItem;
import com.mercadopago.android.px.model.PaymentTypes;

public class PaymentMethodSearchOption implements PaymentMethodSearchViewController {



    protected PaymentMethodSearchItem mItem;
    protected Context mContext;
    protected View mView;
    protected MPTextView mDescription;
    protected MPTextView mComment;
    protected ImageView mIcon;
    protected View.OnClickListener mListener;

    public PaymentMethodSearchOption(Context context, PaymentMethodSearchItem item) {
        mContext = context;
        mItem = item;
    }

    @Override
    public View inflateInParent(ViewGroup parent, boolean attachToRoot) {
        mView = LayoutInflater.from(mContext)
            .inflate(R.layout.px_row_pm_search_item, parent, attachToRoot);
        if (mListener != null) {
            mView.setOnClickListener(mListener);
        }
        return mView;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public void initializeControls() {

    }

    @Override
    public void draw() {
        if (mItem.hasDescription()) {
            mDescription.setVisibility(View.VISIBLE);
            mDescription.setText(mItem.getDescription());
        } else {
            mDescription.setVisibility(View.GONE);
        }
        if (mItem.hasToShowComment()) {
            mComment.setText(mItem.getComment());
        }

        int resourceId = 0;

        final boolean needsTint = needsTint();

        final StringBuilder imageName = new StringBuilder();
        if (!mItem.getId().isEmpty()) {
            if (needsTint) {
                imageName.append(ResourceUtil.TINT_PREFIX);
            }
            imageName.append(mItem.getId());
        }

        if (mItem.isIconRecommended()) {
            resourceId = ResourceUtil.getIconResource(mContext, imageName.toString());
        }

        if (resourceId != 0) {
            mIcon.setImageResource(resourceId);
        } else {
            mIcon.setVisibility(View.GONE);
        }
    }

    private boolean needsTint() {
        return !isMeliOrMpIntegration() && (mItem.isGroup() || mItem.isPaymentType());
    }

    private boolean isMeliOrMpIntegration() {
        final int mpMainColor = ContextCompat.getColor(mContext, R.color.px_mp_blue);
        final int meliMainColor = ContextCompat.getColor(mContext, R.color.meli_yellow);
        final int integrationColor = ContextCompat.getColor(mContext, R.color.px_paymentMethodTint);
        return (mpMainColor == integrationColor) || (meliMainColor == integrationColor);
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        mListener = listener;
        if (mView != null) {
            mView.setOnClickListener(listener);
        }
    }
}
