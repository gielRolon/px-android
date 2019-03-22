package com.mercadopago.android.px.internal.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.viewmodel.PaymentMethodViewModel;
import com.mercadopago.android.px.model.CustomSearchItem;

public class CustomSearchOptionViewModelMapper extends Mapper<CustomSearchItem, PaymentMethodViewModel> {
    @Override
    public PaymentMethodViewModel map(@NonNull final CustomSearchItem val) {

        return new PaymentMethodViewModel() {

            @Override
            public String getDescription() {
                return val.getDescription();
            }

            @Override
            public String getPaymentMethodId() {
                return val.getPaymentMethodId();
            }

            @Override
            public String getDiscountInfo() {
                return val.getDiscountInfo();
            }

            @Override
            public String getComment() {
                return null;
            }

            @Override
            public int getIconResource() {
                return 0;
            }

            @Override
            public boolean isGroupOrPaymentType() {
                return false;
            }
        };
    }
}
