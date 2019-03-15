package com.mercadopago.android.px.internal.viewmodel;

import android.os.Parcel;
import android.support.annotation.NonNull;

public class ChangePaymentMethodPostPaymentAction extends PostPaymentAction {

    public static ChangePaymentMethodPostPaymentAction create(final boolean shouldDisableLastPaymentMethod) {
        final ChangePaymentMethodPostPaymentAction instance = new ChangePaymentMethodPostPaymentAction();
        instance.shouldDisableLastPaymentMethod = shouldDisableLastPaymentMethod;
        return instance;
    }

    private boolean shouldDisableLastPaymentMethod = false;

    /* default */ ChangePaymentMethodPostPaymentAction() {
        super(RequiredAction.SELECT_OTHER_PAYMENT_METHOD, OriginAction.UNKNOWN);
    }

    /* default */ ChangePaymentMethodPostPaymentAction(final Parcel in) {
        super(in);
        shouldDisableLastPaymentMethod = in.readByte() == 1;
    }

    @Override
    public void execute(@NonNull final ActionController actionController) {
        actionController.onChangePaymentMethod(shouldDisableLastPaymentMethod);
    }

    public static final Creator<ChangePaymentMethodPostPaymentAction> CREATOR =
        new Creator<ChangePaymentMethodPostPaymentAction>() {
            @Override
            public ChangePaymentMethodPostPaymentAction createFromParcel(final Parcel in) {
                return new ChangePaymentMethodPostPaymentAction(in);
            }

            @Override
            public ChangePaymentMethodPostPaymentAction[] newArray(final int size) {
                return new ChangePaymentMethodPostPaymentAction[size];
            }
        };

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (shouldDisableLastPaymentMethod ? 1 : 0));
    }
}