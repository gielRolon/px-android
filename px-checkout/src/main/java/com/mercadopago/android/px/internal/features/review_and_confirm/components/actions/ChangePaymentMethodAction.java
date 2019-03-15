package com.mercadopago.android.px.internal.features.review_and_confirm.components.actions;

import com.mercadopago.android.px.model.Action;

public class ChangePaymentMethodAction extends Action {

    private boolean shouldDisableLastPaymentMethod = false;

    public static ChangePaymentMethodAction createWithDisableLastPaymentMethod(){
        final ChangePaymentMethodAction instance = new ChangePaymentMethodAction();
        instance.shouldDisableLastPaymentMethod = true;
        return instance;
    }

    public boolean shouldDisableLastPaymentMethod() {
        return shouldDisableLastPaymentMethod;
    }

    @Override
    public String toString() {
        return "Cambiar medio de pago";
    }
}