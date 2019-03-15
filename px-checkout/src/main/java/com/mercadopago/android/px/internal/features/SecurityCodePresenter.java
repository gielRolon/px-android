package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.datasource.IESCManager;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardView;
import com.mercadopago.android.px.internal.repository.CardTokenRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.CardToken;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.SavedCardToken;
import com.mercadopago.android.px.model.SavedESCCardToken;
import com.mercadopago.android.px.model.Setting;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.CardTokenException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;
import com.mercadopago.android.px.tracking.internal.views.CvvAskViewTracker;

public class SecurityCodePresenter extends BasePresenter<SecurityCodeActivityView> implements SecurityCode.Actions {

    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final CardTokenRepository cardTokenRepository;
    @NonNull private final IESCManager IESCManager;
    private FailureRecovery mFailureRecovery;

    //Card Info
    private int securityCodeLength;
    private String securityCodeLocation;
    private int cardNumberLength;
    private String securityCode;

    //Activity parameters
    private PaymentMethod paymentMethod;
    private CardInfo cardInfo;
    private Card card;
    private Token token;
    private PaymentRecovery paymentRecovery;

    public SecurityCodePresenter(@NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final CardTokenRepository cardTokenRepository,
        @NonNull final IESCManager IESCManager) {
        this.paymentSettingRepository = paymentSettingRepository;
        this.cardTokenRepository = cardTokenRepository;
        this.IESCManager = IESCManager;
    }

    public void setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setToken(final Token token) {
        this.token = token;
    }

    public void setCard(final Card card) {
        this.card = card;
    }

    public void setCardInfo(final CardInfo cardInfo) {
        this.cardInfo = cardInfo;
    }

    public void setPaymentRecovery(final PaymentRecovery paymentRecovery) {
        this.paymentRecovery = paymentRecovery;
    }

    public PaymentRecovery getPaymentRecovery() {
        return paymentRecovery;
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    private void setFailureRecovery(final FailureRecovery failureRecovery) {
        mFailureRecovery = failureRecovery;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public Token getToken() {
        return token;
    }

    public Card getCard() {
        return card;
    }

    public int getSecurityCodeLength() {
        return securityCodeLength;
    }

    public String getSecurityCodeLocation() {
        return securityCodeLocation;
    }

    public int getCardNumberLength() {
        return cardNumberLength;
    }

    public void validate() throws IllegalStateException {
        if (token == null && card == null) {
            throw new IllegalStateException("Token and card can't both be null");
        }

        if (token != null && card != null && paymentRecovery == null) {
            throw new IllegalStateException("Can't set token and card at the same time without payment recovery");
        }

        if (paymentMethod == null) {
            throw new IllegalStateException("Payment method not set");
        }

        if (cardInfo == null) {
            throw new IllegalStateException("Card info can't be null");
        }
    }

    public void initialize() {
        try {
            validate();
            getView().initialize();
            getView().showTimer();
            trackView();
        } catch (final IllegalStateException exception) {
            getView().showStandardErrorMessage();
        }
    }

    private void trackView() {
        final CvvAskViewTracker cvvAskViewTracker = new CvvAskViewTracker(card, getPaymentMethod().getPaymentTypeId());
        setCurrentViewTracker(cvvAskViewTracker);
    }

    public void recoverFromFailure() {
        if (mFailureRecovery != null) {
            mFailureRecovery.recover();
        }
    }

    public void initializeSettings() {
        if (cardInfo != null) {
            final Setting setting =
                Setting.getSettingByPaymentMethodAndBin(paymentMethod, cardInfo.getFirstSixDigits());
            initializeSecurityCodeSettings(setting);
            initializeCardNumberSettings(setting);
            getView().setSecurityCodeInputMaxLength(securityCodeLength);
        }
    }

    private boolean securityCodeSettingsAvailable() {
        return cardInfo != null && cardInfo.getSecurityCodeLength() != null &&
            cardInfo.getSecurityCodeLocation() != null;
    }

    private void initializeSecurityCodeSettings(final Setting setting) {
        if (securityCodeSettingsAvailable()) {
            securityCodeLength = cardInfo.getSecurityCodeLength();
            securityCodeLocation = cardInfo.getSecurityCodeLocation();
        } else if (setting != null && setting.getSecurityCode() != null) {
            securityCodeLength = setting.getSecurityCode().getLength();
            securityCodeLocation = setting.getSecurityCode().getCardLocation();
        } else {
            securityCodeLength = Card.CARD_DEFAULT_SECURITY_CODE_LENGTH;
            securityCodeLocation = CardView.CARD_SIDE_BACK;
        }
    }

    private void initializeCardNumberSettings(final Setting setting) {
        if (setting != null && setting.getCardNumber() != null) {
            cardNumberLength = setting.getCardNumber().getLength();
        } else {
            cardNumberLength = Card.CARD_NUMBER_MAX_LENGTH;
        }
    }

    public void saveSecurityCode(final String securityCode) {
        this.securityCode = securityCode;
    }

    public void validateSecurityCodeInput() {
        try {
            if (hasToCloneToken() && validateSecurityCodeFromToken()) {
                cloneToken();
            } else if (isSavedCardWithESC() || hasToRecoverTokenFromESC()) {
                createTokenWithESC();
            } else if (isSavedCardWithoutESC()) {
                createTokenWithoutESC();
            }
        } catch (final CardTokenException exception) {

            FrictionEventTracker.with(FrictionEventTracker.Id.INVALID_CVV,
                new CvvAskViewTracker(getCard(), getPaymentMethod().getPaymentTypeId()),
                FrictionEventTracker.Style.CUSTOM_COMPONENT,
                getPaymentMethod()).track();

            getView().setErrorView(exception);
        }
    }

    private void createTokenWithESC() throws CardTokenException {
        final SavedESCCardToken savedESCCardToken;
        if (card != null) {
            savedESCCardToken = SavedESCCardToken.createWithSecurityCode(card.getId(), securityCode);
            savedESCCardToken.validateSecurityCode(card);
            createESCToken(savedESCCardToken);
        } else if (token != null) {
            savedESCCardToken = SavedESCCardToken.createWithSecurityCode(token.getCardId(), securityCode);
            validateSecurityCodeFromToken();
            createESCToken(savedESCCardToken);
        }
    }

    private void createTokenWithoutESC() throws CardTokenException {
        SavedCardToken savedCardToken = new SavedCardToken(card.getId(), securityCode);
        savedCardToken.validateSecurityCode(card);
        createToken(savedCardToken);
    }

    private boolean hasToCloneToken() {
        return paymentRecovery != null && (paymentRecovery.isStatusDetailCallForAuthorize() ||
            paymentRecovery.isStatusDetailCardDisabled()) && token != null;
    }

    private boolean hasToRecoverTokenFromESC() {
        return paymentRecovery != null && paymentRecovery.isStatusDetailInvalidESC() &&
            ((token != null && token.getCardId() != null && !token.getCardId().isEmpty()) ||
                (card != null && card.getId() != null && !card.getId().isEmpty()));
    }

    private boolean isSavedCardWithESC() {
        return card != null && IESCManager.isESCEnabled();
    }

    private boolean isSavedCardWithoutESC() {
        return card != null && !IESCManager.isESCEnabled();
    }

    private boolean validateSecurityCodeFromToken() {
        try {
            if (!TextUtil.isEmpty(token.getFirstSixDigits())) {
                CardToken.validateSecurityCode(securityCode, paymentMethod, token.getFirstSixDigits());
            } else if (!CardToken.validateSecurityCode(securityCode)) {
                throw new CardTokenException(CardTokenException.INVALID_FIELD);
            }
            getView().clearErrorView();
            return true;
        } catch (final CardTokenException exception) {
            getView().setErrorView(exception);
            return false;
        }
    }

    private void cloneToken() {
        getView().showLoadingView();

        cardTokenRepository.cloneToken(token.getId())
            .enqueue(new TaggedCallback<Token>(ApiUtil.RequestOrigin.CREATE_TOKEN) {
            @Override
            public void onSuccess(final Token token) {
                SecurityCodePresenter.this.token = token;
                paymentSettingRepository.configure(SecurityCodePresenter.this.token);
                putSecurityCode();
            }

            @Override
            public void onFailure(final MercadoPagoError error) {
                if (isViewAttached()) {
                    setFailureRecovery(new FailureRecovery() {
                        @Override
                        public void recover() {
                            cloneToken();
                        }
                    });
                    getView().stopLoadingView();
                    getView().showError(error, ApiUtil.RequestOrigin.CREATE_TOKEN);
                }
            }
        });
    }

    public void putSecurityCode() {

        cardTokenRepository.putSecurityCode(securityCode, token.getId()).enqueue(
            new TaggedCallback<Token>(ApiUtil.RequestOrigin.CREATE_TOKEN) {
                @Override
                public void onSuccess(final Token token) {
                    resolveTokenCreation(token);
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                cloneToken();
                            }
                        });
                        getView().stopLoadingView();
                        getView().showError(error, ApiUtil.RequestOrigin.CREATE_TOKEN);
                    }
                }
            });
    }

    private void createToken(final SavedCardToken savedCardToken) {
        getView().showLoadingView();

        cardTokenRepository
            .createToken(savedCardToken).enqueue(new TaggedCallback<Token>(ApiUtil.RequestOrigin.CREATE_TOKEN) {
            @Override
            public void onSuccess(Token token) {
                resolveTokenCreation(token);
            }

            @Override
            public void onFailure(MercadoPagoError error) {
                if (isViewAttached()) {
                    setFailureRecovery(new FailureRecovery() {
                        @Override
                        public void recover() {
                            createToken(savedCardToken);
                        }
                    });
                    getView().stopLoadingView();
                    getView().showError(error, ApiUtil.RequestOrigin.CREATE_TOKEN);
                }
            }
        });
    }

    private void createESCToken(final SavedESCCardToken savedESCCardToken) {
        getView().showLoadingView();

        cardTokenRepository
            .createToken(savedESCCardToken).enqueue(new TaggedCallback<Token>(ApiUtil.RequestOrigin.CREATE_TOKEN) {
            @Override
            public void onSuccess(final Token token) {
                resolveTokenCreation(token);
            }

            @Override
            public void onFailure(final MercadoPagoError error) {
                setFailureRecovery(() -> createESCToken(savedESCCardToken));
                getView().stopLoadingView();
                getView().showError(error, ApiUtil.RequestOrigin.CREATE_TOKEN);
            }
        });
    }

    private void resolveTokenCreation(final Token token) {
        this.token = token;
        if (cardInfo != null) {
            this.token.setLastFourDigits(cardInfo.getLastFourDigits());
        }
        paymentSettingRepository.configure(this.token);
        getView().finishWithResult();
    }

    public void setSecurityCodeCardType() {
        if (getSecurityCodeLocation().equals(CardView.CARD_SIDE_BACK)) {
            getView().showBackSecurityCodeCardView();
        } else {
            getView().showFrontSecurityCodeCardView();
        }
    }

    @Override
    public void trackAbort() {
        tracker.trackAbort();
    }
}