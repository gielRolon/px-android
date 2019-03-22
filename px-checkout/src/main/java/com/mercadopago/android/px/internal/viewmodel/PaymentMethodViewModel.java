package com.mercadopago.android.px.internal.viewmodel;

public interface PaymentMethodViewModel {

    String getDescription();

    String getPaymentMethodId();

    String getDiscountInfo();

    String getComment();

    int getIconResource();

    boolean isGroupOrPaymentType();
}