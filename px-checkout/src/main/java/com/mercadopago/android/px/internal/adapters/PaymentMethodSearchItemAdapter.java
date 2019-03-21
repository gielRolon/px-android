package com.mercadopago.android.px.internal.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.mercadopago.android.px.internal.features.uicontrollers.CustomViewController;
import com.mercadopago.android.px.internal.features.uicontrollers.paymentmethodsearch.PaymentMethodSearchViewController;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodSearchItemAdapter extends RecyclerView.Adapter<PaymentMethodSearchItemAdapter.ViewHolder> {

    private final List<PaymentMethodSearchViewController> mItems;

    public PaymentMethodSearchItemAdapter() {
        mItems = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
        final CustomViewController item = mItems.get(position);
        item.inflateInParent(parent, false);
        return new ViewHolder(item);
    }

    @Override
    public int getItemViewType(final int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        PaymentMethodSearchViewController viewController = mItems.get(position);
        viewController.draw();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addItems(final List<PaymentMethodSearchViewController> items) {
        mItems.addAll(items);
    }

    public void clear() {
        final int size = mItems.size();
        mItems.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void notifyItemInserted() {
        notifyItemInserted(mItems.size() - 1);
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {

        private final CustomViewController mViewController;

        public ViewHolder(final CustomViewController viewController) {
            super(viewController.getView());
            mViewController = viewController;
            mViewController.initializeControls();
        }
    }
}
