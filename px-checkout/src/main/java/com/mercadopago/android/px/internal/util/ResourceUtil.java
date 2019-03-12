package com.mercadopago.android.px.internal.util;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.core.PaymentMethodPlugin;
import com.mercadopago.android.px.internal.di.Session;

public final class ResourceUtil {

    public static final int NEUTRAL_CARD_COLOR = R.color.px_white;
    public static final int FULL_TEXT_VIEW_COLOR = R.color.px_base_text_alpha;
    public static final String NEUTRAL_CARD_COLOR_NAME = "px_white";
    public static final String FULL_TEXT_VIEW_COLOR_NAME = "px_base_text_alpha";
    public static final String CARD_ISSUER_IMAGE_PREFIX = "px_issuer_";
    private static final String SDK_PREFIX = "px_";
    private static final String DEF_TYPE_DRAWABLE = "drawable";
    public static final String BANK_SUFFIX = "bank";
    public static final String TINT_PREFIX = "grey_";

    private ResourceUtil() {
    }

    @DrawableRes
    private static int getPaymentMethodIcon(final Context context, String id) {
        int resource;
        id = SDK_PREFIX + id;
        resource = context.getResources().getIdentifier(id, DEF_TYPE_DRAWABLE, context.getPackageName());
        if (resource == 0) {
            resource = context.getResources()
                .getIdentifier(SDK_PREFIX + BANK_SUFFIX, DEF_TYPE_DRAWABLE, context.getPackageName());
            if (resource == 0) {
                resource = R.drawable.px_none;
            }
        }
        return resource;
    }

    @DrawableRes
    public static int getIconResource(final Context context, final String id) {
        try {
            final PaymentMethodPlugin paymentMethodPlugin =
                Session.getSession(context).getPluginRepository().getPlugin(id);
            return paymentMethodPlugin.getPaymentMethodInfo(context).icon;
        } catch (final Exception e) {
            return getPaymentMethodIcon(context, id);
        }
    }

    public static int getCardColor(final String paymentMethodId, final Context context) {
        final String colorName = "px_" + paymentMethodId.toLowerCase();
        int color = context.getResources().getIdentifier(colorName, "color", context.getPackageName());
        if (color == 0) {
            color = context.getResources().getIdentifier(NEUTRAL_CARD_COLOR_NAME, "color", context.getPackageName());
        }
        return color;
    }

    public static int getCardFontColor(final String paymentMethodId, final Context context) {
        if (TextUtil.isEmpty(paymentMethodId)) {
            return FULL_TEXT_VIEW_COLOR;
        }
        final String colorName = "px_font_" + paymentMethodId.toLowerCase();
        int color = context.getResources().getIdentifier(colorName, "color", context.getPackageName());
        if (color == 0) {
            color = context.getResources().getIdentifier(FULL_TEXT_VIEW_COLOR_NAME, "color", context.getPackageName());
        }
        return color;
    }

    public static int getCardImage(@NonNull final Context context, @Nullable String paymentMethodId) {
        final String imageName = "px_ico_card_" + paymentMethodId.toLowerCase();
        return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
    }

    public static int getIssuerImage(@NonNull final Context context, final long issueId) {
        final String imageName = CARD_ISSUER_IMAGE_PREFIX + String.valueOf(issueId);
        return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
    }

    public static int getCardIssuerImage(@NonNull final Context context, final String issuerImageName) {
        if (issuerImageName == null) {
            return 0;
        }
        final String imageName = SDK_PREFIX + issuerImageName.replace("-", "_");
        return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
    }
}
