package com.mercadopago.android.px.internal.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.MercadoPagoUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.internal.viewmodel.PaymentMethodViewModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PaymentMethodSearchItemAdapter extends RecyclerView.Adapter<PaymentMethodSearchItemAdapter.ViewHolder> {

    private final List<PaymentMethodViewModel> mItems;

    public PaymentMethodSearchItemAdapter() {
        mItems = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
        return new ViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.px_row_pm_search_item, parent, false));
    }

    @Override
    public int getItemViewType(final int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.populate(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(@NonNull final Collection<PaymentMethodViewModel> searchItems) {
        mItems.clear();
        mItems.addAll(searchItems);
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {

        private final MPTextView description;
        private final MPTextView comment;
        private final MPTextView discountInfo;
        private final ImageView icon;

        public ViewHolder(final View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.mpsdkDescription);
            comment = itemView.findViewById(R.id.mpsdkComment);
            icon = itemView.findViewById(R.id.mpsdkImage);
            discountInfo = itemView.findViewById(R.id.mpsdkDiscountInfo);
        }

        void populate(@NonNull final PaymentMethodViewModel model) {
            ViewUtils.loadOrGone(model.getDescription(), description);
            ViewUtils.loadOrGone(model.getComment(), comment);
            ViewUtils.loadOrGone(MercadoPagoUtil.getPaymentMethodSearchItemIcon(itemView.getContext(), model.getPaymentMethodId()), icon);
            ViewUtils.loadOrGone(model.getDiscountInfo(), discountInfo);
            if (needsTint(itemView.getContext(), model)) {
                icon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.px_paymentMethodTint),
                    PorterDuff.Mode.MULTIPLY);
            }
        }

        private boolean needsTint(final Context context, final PaymentMethodViewModel model) {
            return !isMeliOrMpIntegration(context) && model.isGroupOrPaymentType();
        }

        private boolean isMeliOrMpIntegration(final Context context) {
            final int mpMainColor = ContextCompat.getColor(context, R.color.px_mp_blue);
            final int meliMainColor = ContextCompat.getColor(context, R.color.meli_yellow);
            final int integrationColor = ContextCompat.getColor(context, R.color.px_paymentMethodTint);
            return (mpMainColor == integrationColor) || (meliMainColor == integrationColor);
        }
    }
}