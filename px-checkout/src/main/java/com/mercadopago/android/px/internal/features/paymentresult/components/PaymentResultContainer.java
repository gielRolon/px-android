package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.configuration.PaymentResultScreenConfiguration;
import com.mercadopago.android.px.internal.features.paymentresult.PaymentResultDecorator;
import com.mercadopago.android.px.internal.features.paymentresult.model.Badge;
import com.mercadopago.android.px.internal.features.paymentresult.props.HeaderProps;
import com.mercadopago.android.px.internal.features.paymentresult.props.PaymentResultBodyProps;
import com.mercadopago.android.px.internal.features.paymentresult.props.PaymentResultProps;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.Component;
import com.mercadopago.android.px.internal.view.LoadingComponent;
import com.mercadopago.android.px.internal.view.RendererFactory;
import com.mercadopago.android.px.internal.viewmodel.HeaderTitleFormatter;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentMethods;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.PaymentTypes;

public class PaymentResultContainer extends Component<PaymentResultProps, Void> {

    static {
        RendererFactory.register(PaymentResultContainer.class, PaymentResultRenderer.class);
    }

    private static final int DEFAULT_STATUS_BAR_COLOR = R.color.px_blue_status_bar;
    private static final int GREEN_STATUS_BAR_COLOR = R.color.px_green_status_bar;
    private static final int RED_STATUS_BAR_COLOR = R.color.px_red_status_bar;
    private static final int ORANGE_STATUS_BAR_COLOR = R.color.px_orange_status_bar;

    public static final int DEFAULT_ICON_IMAGE = R.drawable.px_icon_default;
    public static final int ITEM_ICON_IMAGE = R.drawable.px_icon_product;
    public static final int CARD_ICON_IMAGE = R.drawable.px_icon_card;
    public static final int BOLETO_ICON_IMAGE = R.drawable.px_icon_boleto;

    //armar componente Badge que va como hijo
    public static final int DEFAULT_BADGE_IMAGE = 0;
    public static final int CHECK_BADGE_IMAGE = R.drawable.px_badge_check;
    public static final int PENDING_BADGE_GREEN_IMAGE = R.drawable.px_badge_pending;
    public static final int PENDING_BADGE_ORANGE_IMAGE = R.drawable.px_badge_pending_orange;
    public static final int ERROR_BADGE_IMAGE = R.drawable.px_badge_error;
    public static final int WARNING_BADGE_IMAGE = R.drawable.px_badge_warning;


    public PaymentResultContainer(@NonNull final ActionDispatcher dispatcher,
        @NonNull final PaymentResultProps props) {
        super(props, dispatcher);
    }

    /* default */ boolean isLoading() {
        return props.loading;
    }

    /* default */ LoadingComponent getLoadingComponent() {
        return new LoadingComponent();
    }

    public Header getHeaderComponent(final Context context) {

        final HeaderProps headerProps = new HeaderProps.Builder()
            .setHeight(getHeaderMode())
            .setBackground(getBackground(props.paymentResult))
            .setStatusBarColor(getStatusBarColor(props.paymentResult))
            .setIconImage(getIconImage(props))
            .setIconUrl(getIconUrl(props))
            .setBadgeImage(getBadgeImage(props))
            .setTitle(getTitle(props, context))
            .setLabel(getLabel(props, context))
            .build();

        return new Header(headerProps, getDispatcher());
    }

    public boolean hasBodyComponent() {
        if (props.paymentResult != null) {
            final String status = props.paymentResult.getPaymentStatus();
            final String statusDetail = props.paymentResult.getPaymentStatusDetail();

            return !(Payment.StatusCodes.STATUS_REJECTED.equalsIgnoreCase(status) &&
                !Payment.StatusDetail.isRejectedWithDetail(statusDetail));
        }

        return true;
    }

    @Nullable
    public Body getBodyComponent() {
        Body body = null;
        if (props.paymentResult != null) {

            final PaymentResultBodyProps bodyProps =
                new PaymentResultBodyProps.Builder(props.getPaymentResultScreenPreference())
                    .setPaymentResult(props.paymentResult)
                    .setInstruction(props.instruction)
                    .setCurrencyId(props.currencyId)
                    .setProcessingMode(props.processingMode)
                    .build();
            body = new Body(bodyProps, getDispatcher());
        }
        return body;
    }

    /* default */ FooterPaymentResult getFooterContainer() {
        return new FooterPaymentResult(props.paymentResult, getDispatcher());
    }

    private String getHeaderMode() {
        final String headerMode;
        if (hasBodyComponent()) {
            headerMode = props.headerMode;
        } else {
            headerMode = HeaderProps.HEADER_MODE_STRETCH;
        }
        return headerMode;
    }

    @ColorRes
    private int getBackground(@NonNull final PaymentResult paymentResult) {
        if (PaymentResultDecorator.isSuccessBackground(paymentResult.getPaymentStatus(),
            paymentResult.getPaymentStatusDetail())) {
            return R.color.ui_components_success_color;
        } else if (PaymentResultDecorator.isErrorNonRecoverableBackground(paymentResult.getPaymentStatus(),
            paymentResult.getPaymentStatusDetail())) {
            return R.color.ui_components_error_color;
        } else if (PaymentResultDecorator.isPendingOrErrorRecoverableBackground(paymentResult.getPaymentStatus(),
            paymentResult.getPaymentStatusDetail())) {
            return R.color.ui_components_warning_color;
        } else {
            return R.color.px_colorPrimary;
        }
    }

    private int getStatusBarColor(@NonNull final PaymentResult paymentResult) {
        if (PaymentResultDecorator.isSuccessBackground(paymentResult.getPaymentStatus(),
            paymentResult.getPaymentStatusDetail())) {
            return GREEN_STATUS_BAR_COLOR;
        } else if (PaymentResultDecorator.isErrorNonRecoverableBackground(paymentResult.getPaymentStatus(),
            paymentResult.getPaymentStatusDetail())) {
            return RED_STATUS_BAR_COLOR;
        } else if (PaymentResultDecorator.isPendingOrErrorRecoverableBackground(paymentResult.getPaymentStatus(),
            paymentResult.getPaymentStatusDetail())) {
            return ORANGE_STATUS_BAR_COLOR;
        } else {
            return DEFAULT_STATUS_BAR_COLOR;
        }
    }

    @Nullable
    private String getIconUrl(@NonNull final PaymentResultProps props) {
        final PaymentResultScreenConfiguration paymentResultScreenConfiguration =
            props.getPaymentResultScreenPreference();
        final String paymentStatus = props.paymentResult.getPaymentStatus();
        final String paymentStatusDetail = props.paymentResult.getPaymentStatusDetail();
        return paymentResultScreenConfiguration.getPreferenceUrlIcon(paymentStatus, paymentStatusDetail);
    }

    private int getIconImage(@NonNull final PaymentResultProps props) {
        final PaymentResultScreenConfiguration paymentResultScreenConfiguration =
            props.getPaymentResultScreenPreference();
        final String paymentStatus = props.paymentResult.getPaymentStatus();
        final String paymentStatusDetail = props.paymentResult.getPaymentStatusDetail();

        if (paymentResultScreenConfiguration.hasCustomizedImageIcon(paymentStatus, paymentStatusDetail)) {
            return paymentResultScreenConfiguration.getPreferenceIcon(paymentStatus, paymentStatusDetail);
        } else if (isItemIconImage(props.paymentResult)) {
            return ITEM_ICON_IMAGE;
        } else if (isCardIconImage(props.paymentResult)) {
            return CARD_ICON_IMAGE;
        } else if (isBoletoIconImage(props.paymentResult)) {
            return BOLETO_ICON_IMAGE;
        } else {
            return DEFAULT_ICON_IMAGE;
        }
    }

    private CharSequence getTitle(@NonNull final PaymentResultProps props, @NonNull final Context context) {
        if (props.hasInstructions()) { // Si el medio off tiene instrucciones, tomo las del titulo.
            return props.getInstructionsTitle();
        } else if (isPaymentMethodOff(props.paymentResult)) { // Caso off, sin instrucciones.
            return TextUtil.EMPTY;
        } else {

            final String status = props.paymentResult.getPaymentStatus();
            final String statusDetail = props.paymentResult.getPaymentStatusDetail();

            if (Payment.StatusCodes.STATUS_APPROVED.equalsIgnoreCase(status)) {
                return context.getString(R.string.px_title_approved_payment);
            } else if (Payment.StatusCodes.STATUS_IN_PROCESS.equalsIgnoreCase(status) ||
                Payment.StatusCodes.STATUS_PENDING.equalsIgnoreCase(status)) {
                return context.getString(R.string.px_title_pending_payment);
            } else if (Payment.StatusCodes.STATUS_REJECTED.equalsIgnoreCase(status)) {

                if (Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_OTHER_REASON.equals(statusDetail)
                    || Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_PLUGIN_PM.equals(statusDetail)) {
                    return context.getString(R.string.px_title_other_reason_rejection);
                } else if (Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_INSUFFICIENT_AMOUNT
                    .equalsIgnoreCase(statusDetail)) {
                    return context.getString(R.string.px_text_insufficient_amount);
                } else if (Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_DUPLICATED_PAYMENT
                    .equalsIgnoreCase(statusDetail)) {
                    return context.getString(R.string.px_title_duplicated_reason_rejection);
                } else if (Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CARD_DISABLED
                    .equalsIgnoreCase(statusDetail)) {
                    return context.getString(R.string.px_text_active_card);
                } else if (Payment.StatusDetail.STATUS_DETAIL_REJECTED_HIGH_RISK.equalsIgnoreCase(statusDetail)) {
                    return context.getString(R.string.px_title_rejection_high_risk);
                } else if (Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_MAX_ATTEMPTS.equalsIgnoreCase(statusDetail)) {
                    return context.getString(R.string.px_title_rejection_max_attempts);
                } else if (
                    Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_OTHER.equalsIgnoreCase(statusDetail) ||
                        Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_CARD_NUMBER
                            .equalsIgnoreCase(statusDetail) ||
                        Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_SECURITY_CODE
                            .equalsIgnoreCase(statusDetail) ||
                        Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_DATE.equalsIgnoreCase(statusDetail)) {
                    return context.getString(R.string.px_text_some_card_data_is_incorrect);
                } else if (Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_BLACKLIST.equalsIgnoreCase(statusDetail)) {
                    return context.getString(R.string.px_title_rejection_blacklist);
                } else if (Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_FRAUD.equalsIgnoreCase(statusDetail)) {
                    return context.getString(R.string.px_title_rejection_fraud);
                } else if (Payment.StatusDetail.STATUS_DETAIL_REJECTED_REJECTED_BY_BANK.equalsIgnoreCase(statusDetail)
                    || Payment.StatusDetail.STATUS_DETAIL_REJECTED_REJECTED_INSUFFICIENT_DATA
                    .equalsIgnoreCase(statusDetail)) {
                    return context.getString(R.string.px_bolbradesco_rejection);
                } else if (props.paymentResult.isCallForAuthorize()) {
                    return getCallForAuthFormattedTitle(props, context);
                } else {
                    return context.getString(R.string.px_title_bad_filled_other);
                }
            }
        }

        return TextUtil.EMPTY;
    }


    private CharSequence getCallForAuthFormattedTitle(@NonNull final PaymentResultProps props, @NonNull final Context context) {
        final String rejectedCallForAuthorizeTitle = context.getString(R.string.px_title_activity_call_for_authorize);
        final HeaderTitleFormatter headerTitleFormatter = new HeaderTitleFormatter(props.currencyId,
            props.paymentResult.getPaymentData().getTransactionAmount(),
            props.paymentResult.getPaymentData().getPaymentMethod().getName());
        return headerTitleFormatter.formatTextWithAmount(rejectedCallForAuthorizeTitle);
    }


    public String getLabel(@NonNull PaymentResultProps props, @NonNull final Context context) {
        if (props.hasCustomizedLabel()) {
            return props.getPreferenceLabel();
        } else if (props.paymentResult == null) {
            return TextUtil.EMPTY;
        } else {
            if (isLabelEmpty(props)) {
                return TextUtil.EMPTY;
            } else if (isLabelPending(props.paymentResult)) {
                return context.getString(R.string.px_pending_label);
            } else if (isLabelError(props.paymentResult)) {
                return context.getString(R.string.px_rejection_label);
            }
        }
        return TextUtil.EMPTY;
    }

    private boolean isLabelEmpty(@NonNull PaymentResultProps props) {
        final String status = props.paymentResult.getPaymentStatus();
        final String statusDetail = props.paymentResult.getPaymentStatusDetail();

        return Payment.StatusCodes.STATUS_APPROVED.equalsIgnoreCase(status) ||
            Payment.StatusCodes.STATUS_IN_PROCESS.equalsIgnoreCase(status) ||
            (Payment.StatusCodes.STATUS_PENDING.equalsIgnoreCase(status)
                && (!Payment.StatusDetail.STATUS_DETAIL_PENDING_WAITING_PAYMENT.equalsIgnoreCase(statusDetail)
                || isPaymentMethodOff(props.paymentResult)));
    }

    private boolean isLabelPending(@NonNull final PaymentResult paymentResult) {
        final String status = paymentResult.getPaymentStatus();
        final String statusDetail = paymentResult.getPaymentStatusDetail();
        return Payment.StatusCodes.STATUS_PENDING.equalsIgnoreCase(status)
            && Payment.StatusDetail.STATUS_DETAIL_PENDING_WAITING_PAYMENT.equalsIgnoreCase(statusDetail);
    }

    private boolean isLabelError(@NonNull final PaymentResult paymentResult) {
        return Payment.StatusCodes.STATUS_REJECTED.equalsIgnoreCase(paymentResult.getPaymentStatus());
    }

    private boolean isPaymentMethodOff(@NonNull final PaymentResult paymentResult) {
        final String paymentTypeId = paymentResult.getPaymentData().getPaymentMethod().getPaymentTypeId();
        return PaymentTypes.TICKET.equalsIgnoreCase(paymentTypeId) ||
            PaymentTypes.ATM.equalsIgnoreCase(paymentTypeId) ||
            PaymentTypes.BANK_TRANSFER.equalsIgnoreCase(paymentTypeId);
    }

    private boolean isItemIconImage(@NonNull final PaymentResult paymentResult) {
        final String status = paymentResult.getPaymentStatus();
        final String statusDetail = paymentResult.getPaymentStatusDetail();

        return Payment.StatusCodes.STATUS_APPROVED.equalsIgnoreCase(status) ||
            Payment.StatusCodes.STATUS_PENDING.equalsIgnoreCase(status) &&
                Payment.StatusDetail.STATUS_DETAIL_PENDING_WAITING_PAYMENT.equalsIgnoreCase(statusDetail);
    }

    private boolean isCardIconImage(@NonNull final PaymentResult paymentResult) {
        if (isPaymentMethodIconImage(paymentResult)) {
            final String paymentTypeId = paymentResult.getPaymentData().getPaymentMethod().getPaymentTypeId();

            return PaymentTypes.PREPAID_CARD.equalsIgnoreCase(paymentTypeId) ||
                PaymentTypes.DEBIT_CARD.equalsIgnoreCase(paymentTypeId) ||
                PaymentTypes.CREDIT_CARD.equalsIgnoreCase(paymentTypeId);
        }
        return false;
    }

    private boolean isBoletoIconImage(@NonNull final PaymentResult paymentResult) {
        if (isPaymentMethodIconImage(paymentResult)) {
            final String paymentMethodId = paymentResult.getPaymentData().getPaymentMethod().getId();
            return PaymentMethods.BRASIL.BOLBRADESCO.equalsIgnoreCase(paymentMethodId);
        }
        return false;
    }

    private boolean isPaymentMethodIconImage(@NonNull final PaymentResult paymentResult) {
        final String status = paymentResult.getPaymentStatus();
        final String statusDetail = paymentResult.getPaymentStatusDetail();

        return ((Payment.StatusCodes.STATUS_PENDING).equalsIgnoreCase(status) &&
            !Payment.StatusDetail.STATUS_DETAIL_PENDING_WAITING_PAYMENT.equalsIgnoreCase(statusDetail) ||
            Payment.StatusCodes.STATUS_IN_PROCESS.equalsIgnoreCase(status) ||
            Payment.StatusCodes.STATUS_REJECTED.equalsIgnoreCase(status));
    }

    private int getBadgeImage(@NonNull final PaymentResultProps props) {
        if (props.hasCustomizedBadge()) {
            final String badge = props.getPreferenceBadge();
            switch (badge) {
            case Badge.CHECK_BADGE_IMAGE:
                return CHECK_BADGE_IMAGE;
            case Badge.PENDING_BADGE_IMAGE:
                return PENDING_BADGE_GREEN_IMAGE;
            default:
                return DEFAULT_BADGE_IMAGE;
            }
        } else if (props.paymentResult == null) {
            return DEFAULT_BADGE_IMAGE;
        } else if (PaymentResultDecorator.isCheckBagde(props.paymentResult.getPaymentStatus())) {
            return CHECK_BADGE_IMAGE;
        } else if (PaymentResultDecorator.isPendingSuccessBadge(props.paymentResult.getPaymentStatus(),
            props.paymentResult.getPaymentStatusDetail())) {
            return PENDING_BADGE_GREEN_IMAGE;
        } else if (PaymentResultDecorator.isPendingWarningBadge(props.paymentResult.getPaymentStatus(),
            props.paymentResult.getPaymentStatusDetail())) {
            return PENDING_BADGE_ORANGE_IMAGE;
        } else if (PaymentResultDecorator.isErrorRecoverableBadge(props.paymentResult.getPaymentStatus(),
            props.paymentResult.getPaymentStatusDetail())) {
            return WARNING_BADGE_IMAGE;
        } else if (PaymentResultDecorator.isErrorNonRecoverableBadge(props.paymentResult.getPaymentStatus(),
            props.paymentResult.getPaymentStatusDetail())) {
            return ERROR_BADGE_IMAGE;
        } else {
            return DEFAULT_BADGE_IMAGE;
        }
    }
}