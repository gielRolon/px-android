package com.mercadopago.android.px.internal.features.uicontrollers.paymentmethodsearch;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.MercadoPagoUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.model.CustomSearchItem;

public class PaymentMethodSearchCustomOption implements PaymentMethodSearchViewController {
    protected CustomSearchItem item;
    protected Context context;
    protected View view;
    protected MPTextView description;
    protected MPTextView comment;
    protected MPTextView discountInfo;
    protected ImageView icon;
    protected ImageView badge;
    protected View.OnClickListener listener;

    public PaymentMethodSearchCustomOption(final Context context, final CustomSearchItem item) {
        this.context = context;
        this.item = item;
    }

    @Override
    public View inflateInParent(final ViewGroup parent, final boolean attachToRoot) {
        view = LayoutInflater.from(context)
            .inflate(R.layout.px_row_pm_search_item, parent, attachToRoot);
        if (listener != null) {
            view.setOnClickListener(listener);
        }
        return view;
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void initializeControls() {
        description = view.findViewById(R.id.mpsdkDescription);
        comment = view.findViewById(R.id.mpsdkComment);
        discountInfo = view.findViewById(R.id.mpsdkDiscountInfo);
        icon = view.findViewById(R.id.mpsdkImage);
        badge = view.findViewById(R.id.mpsdkIconBadge);
    }

    @Override
    public void draw() {
        description.setText(item.getDescription());

        int resourceId = 0;

        if (!TextUtils.isEmpty(item.getPaymentMethodId())) {
            resourceId = MercadoPagoUtil.getPaymentMethodSearchItemIcon(context, item.getPaymentMethodId());
        }

        if (resourceId != 0) {
            icon.setImageResource(resourceId);
        } else {
            icon.setVisibility(View.GONE);
        }

        if(item.isDisabled()){
            ViewUtils.grayScaleView(icon);
            badge.setVisibility(View.VISIBLE);
        }else{
            badge.setVisibility(View.GONE);
        }

        if (item.hasDiscountInfo()) {
            discountInfo.setVisibility(View.VISIBLE);
            discountInfo.setText(item.getDiscountInfo());
        } else {
            discountInfo.setVisibility(View.GONE);
        }

        comment.setVisibility(View.GONE);
    }

    @Override
    public void setOnClickListener(final View.OnClickListener listener) {
        this.listener = listener;
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }
}
