package com.mercadopago.android.px.internal.viewmodel;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class PostPaymentAction implements Parcelable {

    private static final String EXTRA_POST_PAYMENT_ACTION = "extra_post_payment_action";
    @NonNull private final RequiredAction requiredAction;
    @NonNull /* default */ final OriginAction originAction;

    public PostPaymentAction(@NonNull final RequiredAction requiredAction, @NonNull final OriginAction originAction) {
        this.requiredAction = requiredAction;
        this.originAction = originAction;
    }

    protected PostPaymentAction(final Parcel in) {
        requiredAction = RequiredAction.values()[in.readInt()];
        originAction = OriginAction.values()[in.readInt()];
    }

    public abstract void execute(@NonNull final ActionController actionController);

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(requiredAction.ordinal());
        dest.writeInt(originAction.ordinal());
    }

    public void addToIntent(@NonNull final Intent intent) {
        intent.putExtra(EXTRA_POST_PAYMENT_ACTION, this);
    }

    public static boolean hasPostPaymentAction(@Nullable final Intent intent) {
        return intent != null && intent.getExtras() != null && intent.hasExtra(EXTRA_POST_PAYMENT_ACTION);
    }

    @NonNull
    public static PostPaymentAction fromBundle(@NonNull final Bundle bundle) {
        final PostPaymentAction action = bundle.getParcelable(EXTRA_POST_PAYMENT_ACTION);
        if(action == null){
            throw new IllegalStateException("Impossible to obtain PostPaymentAction");
        }
        return action;
    }

    public enum RequiredAction {
        SELECT_OTHER_PAYMENT_METHOD, RECOVER_PAYMENT
    }

    public enum OriginAction {
        REVIEW_AND_CONFIRM, ONE_TAP, UNKNOWN
    }

    public interface ActionController {
        void recoverFromReviewAndConfirm(@NonNull final PostPaymentAction postPaymentAction);

        void recoverFromOneTap();

        void onChangePaymentMethod(final boolean shouldDisableLastPaymentMethod);
    }
}
