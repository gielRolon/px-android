package com.mercadopago.android.px.internal.viewmodel.mappers;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import com.mercadolibre.android.card.header.model.CardUI;
import com.mercadopago.android.px.internal.viewmodel.CardHeaderConfiguration;
import com.mercadopago.android.px.internal.viewmodel.drawables.AccountMoneyDrawableFragmentItem;
import com.mercadopago.android.px.internal.viewmodel.drawables.AddNewCardFragmentDrawableFragmentItem;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.internal.viewmodel.drawables.SavedCardDrawableFragmentItem;
import com.mercadopago.android.px.model.CardDisplayInfo;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.PaymentTypes;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodDrawableItemMapper extends Mapper<List<ExpressMetadata>, List<DrawableFragmentItem>> {
    @Override
    public List<DrawableFragmentItem> map(@NonNull final List<ExpressMetadata> val) {
        final List<DrawableFragmentItem> result = new ArrayList<>();

        for (final ExpressMetadata expressMetadata : val) {
            if (expressMetadata.isCard()) {
                result.add(new SavedCardDrawableFragmentItem(expressMetadata.getPaymentMethodId(),
                    getCardUI(expressMetadata.getCard().getDisplayInfo())));
            } else if (PaymentTypes.isAccountMoney(expressMetadata.getPaymentMethodId())) {
                result.add(new AccountMoneyDrawableFragmentItem(expressMetadata.getAccountMoney()));
            }
        }

        result.add(new AddNewCardFragmentDrawableFragmentItem());

        return result;
    }

    private CardHeaderConfiguration getCardUI(@NonNull final CardDisplayInfo cardInfo) {
        //final int paymentMethodResource = ResourceUtil.getCardImage(view.getContext(), drawableCard.paymentMethodId);
        Log.d("guche", "card issuer: " + cardInfo.issuerId);
        Log.d("guche", "card font color: " + cardInfo.fontColor);
        return new CardHeaderConfiguration(cardInfo);
    }
}
