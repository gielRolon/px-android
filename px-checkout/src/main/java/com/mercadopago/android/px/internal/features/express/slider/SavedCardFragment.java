package com.mercadopago.android.px.internal.features.express.slider;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.ResourceUtil;
import com.mercadopago.android.px.internal.view.DynamicTextViewRowView;
import com.mercadopago.android.px.internal.viewmodel.drawables.SavedCardDrawableFragmentItem;

public class SavedCardFragment extends PaymentMethodFragment {

    protected static final String ARG_CARD = "ARG_CARD";

    @SuppressWarnings("TypeMayBeWeakened")
    @NonNull
    public static Fragment getInstance(final SavedCardDrawableFragmentItem savedCard) {
        final SavedCardFragment savedCardFragment = new SavedCardFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_CARD, savedCard);
        savedCardFragment.setArguments(bundle);
        return savedCardFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.px_fragment_saved_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ARG_CARD)) {
            final SavedCardDrawableFragmentItem drawableCard =
                (SavedCardDrawableFragmentItem) arguments.getSerializable(ARG_CARD);
            tintBackground(view, drawableCard);
            setCardInformation(view, drawableCard);
            setPaymentMethodIcon(view, drawableCard);
            setIssuerIcon(view, drawableCard);
        } else {
            throw new IllegalStateException("SavedCardFragment does not contains card information");
        }
    }


    protected void setIssuerIcon(@NonNull final View view, @NonNull final SavedCardDrawableFragmentItem drawableCard) {
        final ImageView issuerIcon = view.findViewById(R.id.card_issuer_logo);
        final int issuerResource = ResourceUtil.getCardIssuerImage(view.getContext(), drawableCard.card.issuerImage);

        if (issuerResource > 0) {
            issuerIcon.setImageResource(issuerResource);
        }
    }

    private void setPaymentMethodIcon(@NonNull final View view,
        @NonNull final SavedCardDrawableFragmentItem drawableCard) {
        final ImageView paymentMethodIcon = view.findViewById(R.id.card_payment_type_logo);
        // set card brand image
        final int paymentMethodResource = ResourceUtil.getCardImage(view.getContext(), drawableCard.paymentMethodId);

        if (paymentMethodResource > 0) {
            paymentMethodIcon.setImageResource(paymentMethodResource);
        }
    }

    private void setCardInformation(@NonNull final View view,
        @NonNull final SavedCardDrawableFragmentItem drawableCard) {
        final TextView cardHolderName = view.findViewById(R.id.card_holder_name);
        final TextView expDate = view.findViewById(R.id.exp_date);
        final int fontColor = Color.parseColor(drawableCard.card.fontColor);
        cardHolderName.setTextColor(fontColor);
        expDate.setTextColor(fontColor);
        cardHolderName.setText(drawableCard.card.cardholderName);
        expDate.setText(drawableCard.card.expiration);
        setCardNumber(view, drawableCard, fontColor);
    }

    protected void setCardNumber(@NonNull final View view,
        @NonNull final SavedCardDrawableFragmentItem drawableCard, final int fontColor) {
        final DynamicTextViewRowView cardNumber = view.findViewById(R.id.card_number_view);
        cardNumber.setColor(fontColor);
        cardNumber.setText(drawableCard.card.getCardPattern(), DynamicTextViewRowView.SPACE);
    }

    private void tintBackground(@NonNull final View view, @NonNull final SavedCardDrawableFragmentItem drawableCard) {
        final ImageView background = view.findViewById(R.id.background);

        final int backgroundColor = Color.parseColor(drawableCard.card.color);

        final int alpha = Color.alpha(backgroundColor);
        final int blue = Color.blue(backgroundColor);
        final int green = Color.green(backgroundColor);
        final int red = Color.red(backgroundColor);

        final int lighterBackgroundColor =
            Color.argb((int) (alpha * 0.7f), (int) (red * 0.8f), (int) (green * 0.8f), (int) (blue * 0.8f));
        Color.argb(0, 0, 0, 0);
        final int[] ints = { backgroundColor, lighterBackgroundColor };
        final GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BL_TR,
            ints);

        gradientDrawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.px_xs_margin));
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setDither(true);

        background.setImageDrawable(gradientDrawable);
    }
}
