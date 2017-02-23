package com.mercadopago;

import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mercadopago.adapters.IdentificationTypesAdapter;
import com.mercadopago.callbacks.PaymentMethodSelectionCallback;
import com.mercadopago.callbacks.card.CardExpiryDateEditTextCallback;
import com.mercadopago.callbacks.card.CardIdentificationNumberEditTextCallback;
import com.mercadopago.callbacks.card.CardNumberEditTextCallback;
import com.mercadopago.callbacks.card.CardSecurityCodeEditTextCallback;
import com.mercadopago.callbacks.card.CardholderNameEditTextCallback;
import com.mercadopago.constants.PaymentTypes;
import com.mercadopago.controllers.CheckoutTimer;
import com.mercadopago.controllers.PaymentMethodGuessingController;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.core.MercadoPagoUI;
import com.mercadopago.customviews.MPEditText;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.listeners.card.CardExpiryDateTextWatcher;
import com.mercadopago.listeners.card.CardIdentificationNumberTextWatcher;
import com.mercadopago.listeners.card.CardNumberTextWatcher;
import com.mercadopago.listeners.card.CardSecurityCodeTextWatcher;
import com.mercadopago.listeners.card.CardholderNameTextWatcher;
import com.mercadopago.model.ApiException;
import com.mercadopago.model.BankDeal;
import com.mercadopago.model.CardInfo;
import com.mercadopago.model.CardToken;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Identification;
import com.mercadopago.model.IdentificationType;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentRecovery;
import com.mercadopago.model.PaymentType;
import com.mercadopago.model.Token;
import com.mercadopago.mptracker.MPTracker;
import com.mercadopago.observers.TimerObserver;
import com.mercadopago.preferences.DecorationPreference;
import com.mercadopago.preferences.PaymentPreference;
import com.mercadopago.presenters.GuessingCardPresenter;
import com.mercadopago.uicontrollers.card.CardRepresentationModes;
import com.mercadopago.uicontrollers.card.CardView;
import com.mercadopago.uicontrollers.card.IdentificationCardView;
import com.mercadopago.uicontrollers.discounts.DiscountRowView;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.ColorsUtil;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.MPAnimationUtils;
import com.mercadopago.util.MPCardMaskUtil;
import com.mercadopago.util.MercadoPagoUtil;
import com.mercadopago.util.ScaleUtil;
import com.mercadopago.views.GuessingCardActivityView;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by vaserber on 10/13/16.
 */

public class GuessingCardActivity extends MercadoPagoBaseActivity implements GuessingCardActivityView, TimerObserver {

    public static final String CARD_NUMBER_INPUT = "cardNumber";
    public static final String CARDHOLDER_NAME_INPUT = "cardHolderName";
    public static final String CARD_EXPIRYDATE_INPUT = "cardExpiryDate";
    public static final String CARD_SECURITYCODE_INPUT = "cardSecurityCode";
    public static final String CARD_IDENTIFICATION_INPUT = "cardIdentification";
    public static final String CARD_IDENTIFICATION = "identification";

    public static final String ERROR_STATE = "textview_error";
    public static final String NORMAL_STATE = "textview_normal";

    public static final String CARD_SIDE_STATE_BUNDLE = "mCardSideState";
    public static final String PAYMENT_METHOD_BUNDLE = "mPaymentMethod";
    public static final String ID_REQUIRED_BUNDLE = "mIdentificationNumberRequired";
    public static final String SEC_CODE_REQUIRED_BUNDLE = "mIsSecurityCodeRequired";
    public static final String SEC_CODE_LENGTH_BUNDLE = "mCardSecurityCodeLength";
    public static final String CARD_NUMBER_LENGTH_BUNDLE = "mCardNumberLength";
    public static final String SEC_CODE_LOCATION_BUNDLE = "mSecurityCodeLocation";
    public static final String CARD_TOKEN_BUNDLE = "mCardToken";
    public static final String CARD_INFO_BIN_BUNDLE = "mBin";
    public static final String PAYMENT_METHOD_LIST_BUNDLE = "mPaymentMethodList";
    public static final String EXPIRY_MONTH_BUNDLE = "mExpiryMonth";
    public static final String EXPIRY_YEAR_BUNDLE = "mExpiryYear";
    public static final String CARD_NUMBER_BUNDLE = "mCardNumber";
    public static final String CARD_NAME_BUNDLE = "mCardName";
    public static final String IDENTIFICATION_BUNDLE = "mIdentification";
    public static final String IDENTIFICATION_NUMBER_BUNDLE = "mIdentificationNumber";
    public static final String IDENTIFICATION_TYPE_BUNDLE = "mIdentificationType";
    public static final String PAYMENT_TYPES_LIST_BUNDLE = "mPaymentTypesList";
    public static final String BANK_DEALS_LIST_BUNDLE = "mBankDealsList";
    public static final String IDENTIFICATION_TYPES_LIST_BUNDLE = "mIdTypesList";
    public static final String PAYMENT_RECOVERY_BUNDLE = "mPaymentRecovery";
    public static final String LOW_RES_BUNDLE = "mLowRes";

    //ViewMode
    protected boolean mLowResActive;
    protected GuessingCardPresenter mPresenter;
    private Activity mActivity;

    //View controls
    private DecorationPreference mDecorationPreference;
    private ScrollView mScrollView;

    //View Low Res
    private Toolbar mLowResToolbar;
    private MPTextView mLowResTitleToolbar;
    //View Normal
    private Toolbar mNormalToolbar;
    private MPTextView mBankDealsTextView;
    private FrameLayout mCardBackground;
    private FrameLayout mCardViewContainer;
    private FrameLayout mIdentificationCardContainer;
    private CardView mCardView;
    private IdentificationCardView mIdentificationCardView;
    private MPTextView mTimerTextView;

    //Input Views
    private ProgressBar mProgressBar;
    private LinearLayout mInputContainer;
    private Spinner mIdentificationTypeSpinner;
    private LinearLayout mIdentificationTypeContainer;
    private FrameLayout mNextButton;
    private FrameLayout mBackButton;
    private FrameLayout mBackInactiveButton;
    private FrameLayout mDiscountFrameLayout;
    private LinearLayout mButtonContainer;
    private MPEditText mCardNumberEditText;
    private MPEditText mCardHolderNameEditText;
    private MPEditText mCardExpiryDateEditText;
    private MPEditText mSecurityCodeEditText;
    private MPEditText mIdentificationNumberEditText;
    private LinearLayout mCardNumberInput;
    private LinearLayout mCardholderNameInput;
    private LinearLayout mCardExpiryDateInput;
    private LinearLayout mCardIdentificationInput;
    private LinearLayout mCardSecurityCodeInput;
    private FrameLayout mErrorContainer;
    private MPTextView mErrorTextView;
    private String mErrorState;
    private TextView mNextButtonText;
    private TextView mBackButtonText;
    private TextView mBackInactiveButtonText;

    //Input Controls
    private String mCurrentEditingEditText;
    private String mCardSideState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPresenter == null) {
            mPresenter = new GuessingCardPresenter(getBaseContext());
        }
        mPresenter.setView(this);
        mActivity = this;
        getActivityParameters();
        if (isCustomColorSet()) {
            setTheme(R.style.Theme_MercadoPagoTheme_NoActionBar);
        }
        analizeLowRes();
        setContentView();
        mPresenter.validateActivityParameters();
    }

    private boolean isCustomColorSet() {
        return mDecorationPreference != null && mDecorationPreference.hasColors();
    }

    private void getActivityParameters() {

        String publicKey = getIntent().getStringExtra("merchantPublicKey");
        String privateKey = getIntent().getStringExtra("payerAccessToken");
        PaymentPreference paymentPreference = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("paymentPreference"), PaymentPreference.class);
        mDecorationPreference = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("decorationPreference"), DecorationPreference.class);

        PaymentRecovery paymentRecovery = JsonUtil.getInstance().fromJson(this.getIntent().getStringExtra("paymentRecovery"), PaymentRecovery.class);

        BigDecimal transactionAmount = JsonUtil.getInstance().fromJson(this.getIntent().getStringExtra("transactionAmount"), BigDecimal.class);
        Boolean discountEnabled = this.getIntent().getBooleanExtra("discountEnabled", true);
        Discount discount = JsonUtil.getInstance().fromJson(this.getIntent().getStringExtra("discount"), Discount.class);
        String payerEmail = this.getIntent().getStringExtra("payerEmail");

        Token token = null;
        PaymentMethod paymentMethod = null;

        List<PaymentMethod> paymentMethodList;
        try {
            Type listType = new TypeToken<List<PaymentMethod>>() {
            }.getType();
            paymentMethodList = JsonUtil.getInstance().getGson().fromJson(this.getIntent().getStringExtra("paymentMethodList"), listType);
        } catch (Exception ex) {
            paymentMethodList = null;
        }
        Identification identification = new Identification();
        boolean identificationNumberRequired = false;

        Boolean showBankDeals = this.getIntent().getBooleanExtra("showBankDeals", true);

        mPresenter.setPrivateKey(privateKey);
        mPresenter.setPublicKey(publicKey);
        mPresenter.setToken(token);
        mPresenter.setShowBankDeals(showBankDeals);
        mPresenter.setPaymentMethod(paymentMethod);
        mPresenter.setPaymentMethodList(paymentMethodList);
        mPresenter.setIdentification(identification);
        mPresenter.setIdentificationNumberRequired(identificationNumberRequired);
        mPresenter.setPaymentPreference(paymentPreference);
        mPresenter.setPaymentRecovery(paymentRecovery);
        mPresenter.setPayerEmail(payerEmail);
        mPresenter.setDiscount(discount);
        mPresenter.setTransactionAmount(transactionAmount);
        mPresenter.setDiscountEnabled(discountEnabled);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mPresenter.getPaymentMethod() != null) {
            outState.putString(CARD_SIDE_STATE_BUNDLE, mCardSideState);
            outState.putString(PAYMENT_METHOD_BUNDLE, JsonUtil.getInstance().toJson(mPresenter.getPaymentMethod()));
            outState.putBoolean(ID_REQUIRED_BUNDLE, mPresenter.isIdentificationNumberRequired());
            outState.putBoolean(SEC_CODE_REQUIRED_BUNDLE, mPresenter.isSecurityCodeRequired());
            outState.putInt(SEC_CODE_LENGTH_BUNDLE, mPresenter.getSecurityCodeLength());
            outState.putInt(CARD_NUMBER_LENGTH_BUNDLE, mPresenter.getCardNumberLength());
            outState.putString(SEC_CODE_LOCATION_BUNDLE, mPresenter.getSecurityCodeLocation());
            outState.putString(CARD_TOKEN_BUNDLE, JsonUtil.getInstance().toJson(mPresenter.getCardToken()));
            outState.putString(CARD_INFO_BIN_BUNDLE, mPresenter.getSavedBin());
            outState.putString(PAYMENT_METHOD_LIST_BUNDLE, JsonUtil.getInstance().toJson(mPresenter.getPaymentMethodList()));
            outState.putString(CARD_NUMBER_BUNDLE, mPresenter.getCardNumber());
            outState.putString(CARD_NAME_BUNDLE, mPresenter.getCardholderName());
            outState.putString(EXPIRY_MONTH_BUNDLE, mPresenter.getExpiryMonth());
            outState.putString(EXPIRY_YEAR_BUNDLE, mPresenter.getExpiryYear());
            outState.putString(IDENTIFICATION_BUNDLE, JsonUtil.getInstance().toJson(mPresenter.getIdentification()));
            outState.putString(IDENTIFICATION_NUMBER_BUNDLE, mPresenter.getIdentificationNumber());
            outState.putString(IDENTIFICATION_TYPE_BUNDLE, JsonUtil.getInstance().toJson(mPresenter.getIdentificationType()));
            outState.putString(PAYMENT_TYPES_LIST_BUNDLE, JsonUtil.getInstance().toJson(mPresenter.getPaymentTypes()));
            outState.putString(BANK_DEALS_LIST_BUNDLE, JsonUtil.getInstance().toJson(mPresenter.getBankDealsList()));
            outState.putString(IDENTIFICATION_TYPES_LIST_BUNDLE, JsonUtil.getInstance().toJson(mPresenter.getIdentificationTypes()));
            outState.putString(PAYMENT_RECOVERY_BUNDLE, JsonUtil.getInstance().toJson(mPresenter.getPaymentRecovery()));
            outState.putBoolean(LOW_RES_BUNDLE, mLowResActive);
            mSecurityCodeEditText.getText().clear();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.getString(PAYMENT_METHOD_BUNDLE) != null) {
                PaymentMethod pm = JsonUtil.getInstance().fromJson(savedInstanceState.getString(PAYMENT_METHOD_BUNDLE), PaymentMethod.class);
                if (pm != null) {
                    List<PaymentMethod> paymentMethodList;
                    try {
                        Type listType = new TypeToken<List<PaymentMethod>>() {
                        }.getType();
                        paymentMethodList = JsonUtil.getInstance().getGson().fromJson(
                                savedInstanceState.getString(PAYMENT_METHOD_LIST_BUNDLE), listType);
                    } catch (Exception ex) {
                        paymentMethodList = null;
                    }
                    List<PaymentType> paymentTypesList;
                    try {
                        Type listType = new TypeToken<List<PaymentType>>() {
                        }.getType();
                        paymentTypesList = JsonUtil.getInstance().getGson().fromJson(
                                savedInstanceState.getString(PAYMENT_TYPES_LIST_BUNDLE), listType);
                    } catch (Exception ex) {
                        paymentTypesList = null;
                    }
                    List<BankDeal> bankDealsList;
                    try {
                        Type listType = new TypeToken<List<BankDeal>>() {
                        }.getType();
                        bankDealsList = JsonUtil.getInstance().getGson().fromJson(
                                savedInstanceState.getString(BANK_DEALS_LIST_BUNDLE), listType);
                    } catch (Exception ex) {
                        bankDealsList = null;
                    }
                    List<IdentificationType> identificationTypesList;
                    try {
                        Type listType = new TypeToken<List<IdentificationType>>() {
                        }.getType();
                        identificationTypesList = JsonUtil.getInstance().getGson().fromJson(
                                savedInstanceState.getString(IDENTIFICATION_TYPES_LIST_BUNDLE), listType);
                    } catch (Exception ex) {
                        identificationTypesList = null;
                    }
                    mPresenter.setPaymentMethodList(paymentMethodList);
                    mPresenter.setPaymentTypesList(paymentTypesList);
                    mPresenter.setIdentificationTypesList(identificationTypesList);
                    mPresenter.setBankDealsList(bankDealsList);
                    mPresenter.initializeGuessingCardNumberController();
                    mPresenter.saveBin(savedInstanceState.getString(CARD_INFO_BIN_BUNDLE));
                    mPresenter.setIdentificationNumberRequired(savedInstanceState.getBoolean(ID_REQUIRED_BUNDLE));
                    mPresenter.setSecurityCodeRequired(savedInstanceState.getBoolean(SEC_CODE_REQUIRED_BUNDLE));
                    mPresenter.setCardNumber(savedInstanceState.getString(CARD_NUMBER_BUNDLE));
                    mPresenter.setCardholderName(savedInstanceState.getString(CARD_NAME_BUNDLE));
                    mPresenter.setExpiryMonth(savedInstanceState.getString(EXPIRY_MONTH_BUNDLE));
                    mPresenter.setExpiryYear(savedInstanceState.getString(EXPIRY_YEAR_BUNDLE));
                    String idNumber = savedInstanceState.getString(IDENTIFICATION_NUMBER_BUNDLE);
                    mPresenter.setIdentificationNumber(idNumber);
                    Identification identification = JsonUtil.getInstance().fromJson(savedInstanceState.getString(IDENTIFICATION_BUNDLE), Identification.class);
                    identification.setNumber(idNumber);
                    mPresenter.setIdentification(identification);
                    CardToken cardToken = JsonUtil.getInstance().fromJson(savedInstanceState.getString(CARD_TOKEN_BUNDLE), CardToken.class);
                    cardToken.getCardholder().setIdentification(identification);
                    IdentificationType identificationType = JsonUtil.getInstance().fromJson(savedInstanceState.getString(IDENTIFICATION_TYPE_BUNDLE), IdentificationType.class);
                    mPresenter.setCardToken(cardToken);
                    mPresenter.setPaymentRecovery(JsonUtil.getInstance().fromJson(savedInstanceState.getString(PAYMENT_RECOVERY_BUNDLE), PaymentRecovery.class));
                    mLowResActive = savedInstanceState.getBoolean(LOW_RES_BUNDLE);
                    if (mCardView == null) {
                        loadViews();
                    }
                    if (cardViewsActive()) {
                        mCardView.drawEditingCardNumber(mPresenter.getCardNumber());
                        mCardView.drawEditingCardHolderName(mPresenter.getCardholderName());
                        mCardView.drawEditingExpiryMonth(mPresenter.getExpiryMonth());
                        mCardView.drawEditingExpiryYear(mPresenter.getExpiryYear());
                        mIdentificationCardView.setIdentificationNumber(idNumber);
                        mIdentificationCardView.setIdentificationType(identificationType);
                        mIdentificationCardView.draw();
                    }
                    setPaymentMethod(pm);
                    mSecurityCodeEditText.getText().clear();
                    requestCardNumberFocus();
                    if (cardViewsActive()) {
                        mCardView.updateCardNumberMask(getCardNumberTextTrimmed());
                    }
                }
            }
        }
    }

    private void analizeLowRes() {
        this.mLowResActive = ScaleUtil.isLowRes(this);
    }

    private void setContentView() {
        if (mLowResActive) {
            setContentViewLowRes();
        } else {
            setContentViewNormal();
        }
    }

    private void setContentViewLowRes() {
        setContentView(R.layout.mpsdk_activity_form_card_lowres);
    }

    private void setContentViewNormal() {
        setContentView(R.layout.mpsdk_activity_form_card_normal);
    }

    @Override
    public void onInvalidStart(String message) {
        finish();
    }

    @Override
    public void onValidStart() {
        mPresenter.initializeMercadoPago();
        mPresenter.initializeCardToken();
        initializeViews();
        loadViews();
        decorate();

        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            CheckoutTimer.getInstance().addObserver(this);
            mTimerTextView.setVisibility(View.VISIBLE);
            mTimerTextView.setText(CheckoutTimer.getInstance().getCurrentTime());
        } else {
            mPresenter.resolveBankDeals();
        }

        mErrorState = NORMAL_STATE;
        mPresenter.initialize();
    }

    private void initializeViews() {
        mTimerTextView = (MPTextView) findViewById(R.id.mpsdkTimerTextView);

        if (mLowResActive) {
            mLowResToolbar = (Toolbar) findViewById(R.id.mpsdkLowResToolbar);
            mLowResTitleToolbar = (MPTextView) findViewById(R.id.mpsdkTitle);
            mLowResToolbar.setVisibility(View.VISIBLE);
        } else {
            mNormalToolbar = (Toolbar) findViewById(R.id.mpsdkTransparentToolbar);
            mCardBackground = (FrameLayout) findViewById(R.id.mpsdkCardBackground);
            mCardViewContainer = (FrameLayout) findViewById(R.id.mpsdkCardViewContainer);
            mIdentificationCardContainer = (FrameLayout) findViewById(R.id.mpsdkIdentificationCardContainer);
        }
        mIdentificationTypeContainer = (LinearLayout) findViewById(R.id.mpsdkCardIdentificationTypeContainer);
        mIdentificationTypeSpinner = (Spinner) findViewById(R.id.mpsdkCardIdentificationType);
        mBankDealsTextView = (MPTextView) findViewById(R.id.mpsdkBankDealsText);
        mCardNumberEditText = (MPEditText) findViewById(R.id.mpsdkCardNumber);
        mCardHolderNameEditText = (MPEditText) findViewById(R.id.mpsdkCardholderName);
        mCardExpiryDateEditText = (MPEditText) findViewById(R.id.mpsdkCardExpiryDate);
        mSecurityCodeEditText = (MPEditText) findViewById(R.id.mpsdkCardSecurityCode);
        mIdentificationNumberEditText = (MPEditText) findViewById(R.id.mpsdkCardIdentificationNumber);
        mInputContainer = (LinearLayout) findViewById(R.id.mpsdkInputContainer);
        mProgressBar = (ProgressBar) findViewById(R.id.mpsdkProgressBar);
        mNextButton = (FrameLayout) findViewById(R.id.mpsdkNextButton);
        mBackButton = (FrameLayout) findViewById(R.id.mpsdkBackButton);
        mBackInactiveButton = (FrameLayout) findViewById(R.id.mpsdkBackInactiveButton);
        mBackInactiveButtonText = (TextView) findViewById(R.id.mpsdkBackInactiveButtonText);
        mNextButtonText = (TextView) findViewById(R.id.mpsdkNextButtonText);
        mBackButtonText = (TextView) findViewById(R.id.mpsdkBackButtonText);
        mButtonContainer = (LinearLayout) findViewById(R.id.mpsdkButtonContainer);
        mCardNumberInput = (LinearLayout) findViewById(R.id.mpsdkCardNumberInput);
        mCardholderNameInput = (LinearLayout) findViewById(R.id.mpsdkCardholderNameInput);
        mCardExpiryDateInput = (LinearLayout) findViewById(R.id.mpsdkExpiryDateInput);
        mCardIdentificationInput = (LinearLayout) findViewById(R.id.mpsdkCardIdentificationInput);
        mCardSecurityCodeInput = (LinearLayout) findViewById(R.id.mpsdkCardSecurityCodeContainer);
        mErrorContainer = (FrameLayout) findViewById(R.id.mpsdkErrorContainer);
        mErrorTextView = (MPTextView) findViewById(R.id.mpsdkErrorTextView);
        mScrollView = (ScrollView) findViewById(R.id.mpsdkScrollViewContainer);
        mDiscountFrameLayout = (FrameLayout) findViewById(R.id.mpsdkDiscount);
        mInputContainer.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        fullScrollDown();
    }

    @Override
    public void showInputContainer() {
        mIdentificationTypeContainer.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mInputContainer.setVisibility(View.VISIBLE);
        requestCardNumberFocus();
    }

    private void loadViews() {
        if (mLowResActive) {
            loadLowResViews();
        } else {
            loadNormalViews();
        }
    }

    private boolean cardViewsActive() {
        return !mLowResActive;
    }

    private void loadLowResViews() {
        loadToolbarArrow(mLowResToolbar);
    }

    private void loadNormalViews() {
        loadToolbarArrow(mNormalToolbar);

        mCardView = new CardView(mActivity);
        mCardView.setSize(CardRepresentationModes.BIG_SIZE);
        mCardView.inflateInParent(mCardViewContainer, true);
        mCardView.initializeControls();
        mCardView.draw(CardView.CARD_SIDE_FRONT);
        mCardSideState = CardView.CARD_SIDE_FRONT;

        mIdentificationCardView = new IdentificationCardView(mActivity);
        mIdentificationCardView.inflateInParent(mIdentificationCardContainer, true);
        mIdentificationCardView.initializeControls();
        mIdentificationCardView.hide();
    }

    private void loadToolbarArrow(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("discount", JsonUtil.getInstance().toJson(mPresenter.getDiscount()));
                    setResult(RESULT_CANCELED, returnIntent);
                    finish();
                }
            });
        }
    }

    private void decorate() {
        if (isDecorationEnabled()) {
            if (mLowResActive) {
                decorateLowRes();
            } else {
                decorateNormal();
            }
        }
    }

    private boolean isDecorationEnabled() {
        return mDecorationPreference != null && mDecorationPreference.hasColors();
    }

    private void decorateLowRes() {
        ColorsUtil.decorateLowResToolbar(mLowResToolbar, mLowResTitleToolbar, mDecorationPreference,
                getSupportActionBar(), this);
        ColorsUtil.decorateTextView(mDecorationPreference, mBankDealsTextView, this);
        if (mTimerTextView != null) {
            ColorsUtil.decorateTextView(mDecorationPreference, mTimerTextView, this);
        }
        mNextButtonText.setTextColor(mDecorationPreference.getDarkFontColor(this));
        mBackButtonText.setTextColor(mDecorationPreference.getDarkFontColor(this));
        mBackInactiveButtonText.setTextColor(ContextCompat.getColor(this, R.color.mpsdk_warm_grey));
    }

    private void decorateNormal() {
        ColorsUtil.decorateTransparentToolbar(mNormalToolbar, mBankDealsTextView, mDecorationPreference,
                getSupportActionBar(), this);
        if (mTimerTextView != null) {
            ColorsUtil.decorateTextView(mDecorationPreference, mTimerTextView, this);
        }
        mCardView.decorateCardBorder(mDecorationPreference.getLighterColor());
        mIdentificationCardView.decorateCardBorder(mDecorationPreference.getLighterColor());
        mCardBackground.setBackgroundColor(mDecorationPreference.getLighterColor());
        mNextButtonText.setTextColor(mDecorationPreference.getDarkFontColor(this));
        mBackButtonText.setTextColor(mDecorationPreference.getDarkFontColor(this));
        mBackInactiveButtonText.setTextColor(ContextCompat.getColor(this, R.color.mpsdk_warm_grey));
    }

    private String getCardNumberTextTrimmed() {
        return mCardNumberEditText.getText().toString().replaceAll("\\s", "");
    }

    @Override
    public void initializeTitle() {
        if (mLowResActive) {
            String paymentTypeId = mPresenter.getPaymentTypeId();
            String paymentTypeText = getString(R.string.mpsdk_form_card_title);
            if (paymentTypeId != null) {
                if (paymentTypeId.equals(PaymentTypes.CREDIT_CARD)) {
                    paymentTypeText = getString(R.string.mpsdk_form_card_title_payment_type, getString(R.string.mpsdk_credit_payment_type));
                } else if (paymentTypeId.equals(PaymentTypes.DEBIT_CARD)) {
                    paymentTypeText = getString(R.string.mpsdk_form_card_title_payment_type, getString(R.string.mpsdk_debit_payment_type));
                } else if (paymentTypeId.equals(PaymentTypes.PREPAID_CARD)) {
                    paymentTypeText = getString(R.string.mpsdk_form_card_title_payment_type_prepaid);
                }
            }
            mLowResTitleToolbar.setText(paymentTypeText);
        }
    }

    @Override
    public void showBankDeals() {
        if (mPresenter.getBankDealsList() == null || mPresenter.getBankDealsList().size() == 0) {
            hideBankDeals();
        } else {
            if (mLowResActive) {
                mBankDealsTextView.setText(getString(R.string.mpsdk_bank_deals_lowres));
            } else {
                mBankDealsTextView.setText(getString(R.string.mpsdk_bank_deals_action));
            }

            mBankDealsTextView.setVisibility(View.VISIBLE);

            mBankDealsTextView.setFocusable(true);
            mBankDealsTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MercadoPagoComponents.Activities.BankDealsActivityBuilder()
                            .setActivity(mActivity)
                            .setMerchantPublicKey(mPresenter.getPublicKey())
                            .setPayerAccessToken(mPresenter.getPrivateKey())
                            .setDecorationPreference(mDecorationPreference)
                            .setBankDeals(mPresenter.getBankDealsList())
                            .startActivity();
                }
            });
        }
    }

    @Override
    public void hideBankDeals() {
        mBankDealsTextView.setVisibility(View.GONE);
    }

    @Override
    public void setCardNumberListeners(PaymentMethodGuessingController controller) {
        mCardNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
        mCardNumberEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEditText(mCardNumberEditText, event);
                return true;
            }
        });
        mCardNumberEditText.addTextChangedListener(new CardNumberTextWatcher(
                controller,
                new PaymentMethodSelectionCallback() {
                    @Override
                    public void onPaymentMethodListSet(List<PaymentMethod> paymentMethodList, String bin) {
                        mPresenter.saveBin(bin);
                        if (paymentMethodList.size() == 0) {
                            setInputMaxLength(mCardNumberEditText, MercadoPagoUtil.BIN_LENGTH);
                            setErrorView(getString(R.string.mpsdk_invalid_payment_method));
                        } else if (paymentMethodList.size() == 1) {
                            onPaymentMethodSet(paymentMethodList.get(0));
                        } else {
                            mPresenter.enablePaymentTypeSelection(paymentMethodList);
                            setPaymentMethod(paymentMethodList.get(0));
                        }
                    }

                    @Override
                    public void onPaymentMethodSet(PaymentMethod paymentMethod) {
                        setPaymentMethod(paymentMethod);
                    }

                    @Override
                    public void onPaymentMethodCleared() {
                        clearErrorView();
                        clearCardNumberInputLength();
                        if (mPresenter.getPaymentMethod() == null) {
                            return;
                        }
                        mPresenter.setPaymentMethod(null);
                        mSecurityCodeEditText.getText().clear();
                        mPresenter.initializeCardToken();
                        mPresenter.setIdentificationNumberRequired(true);
                        mPresenter.setSecurityCodeRequired(true);
                        mPresenter.disablePaymentTypeSelection();
                        if (cardViewsActive()) {
                            mCardView.clearPaymentMethod();
                        }
                    }
                },
                new CardNumberEditTextCallback() {
                    @Override
                    public void checkOpenKeyboard() {
                        openKeyboard(mCardNumberEditText);
                    }

                    @Override
                    public void saveCardNumber(CharSequence string) {
                        mPresenter.saveCardNumber(string.toString());
                        if (cardViewsActive()) {
                            mCardView.drawEditingCardNumber(string.toString());
                        }
                    }

                    @Override
                    public void appendSpace(CharSequence string) {
                        if (MPCardMaskUtil.needsMask(string, mPresenter.getCardNumberLength())) {
                            mCardNumberEditText.append(" ");
                        }
                    }

                    @Override
                    public void deleteChar(CharSequence s) {
                        if (MPCardMaskUtil.needsMask(s, mPresenter.getCardNumberLength())) {
                            mCardNumberEditText.getText().delete(s.length() - 1, s.length());
                        }
                    }

                    @Override
                    public void changeErrorView() {
                        checkChangeErrorView();
                    }

                    @Override
                    public void toggleLineColorOnError(boolean toggle) {
                        mCardNumberEditText.toggleLineColorOnError(toggle);
                    }
                }));
    }

    private void setPaymentMethod(PaymentMethod paymentMethod) {
        if (mPresenter.getPaymentMethod() == null) {
            mPresenter.setPaymentMethod(paymentMethod);
            mPresenter.configureWithSettings();
            mPresenter.loadIdentificationTypes();
            if (cardViewsActive()) {
                mCardView.setPaymentMethod(paymentMethod);
                mCardView.setCardNumberLength(mPresenter.getCardNumberLength());
                mCardView.setSecurityCodeLength(mPresenter.getSecurityCodeLength());
                mCardView.setSecurityCodeLocation(mPresenter.getSecurityCodeLocation());
                mCardView.updateCardNumberMask(getCardNumberTextTrimmed());
                mCardView.transitionPaymentMethodSet();
            }
        }
    }

    @Override
    public void setNextButtonListeners() {
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateCurrentEditText();
            }
        });
    }

    @Override
    public void setBackButtonListeners() {
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCurrentEditingEditText.equals(CARD_NUMBER_INPUT)) {
                    checkIsEmptyOrValid();
                }
            }
        });
    }

    @Override
    public void setCardholderName(String cardholderName) {
        mCardHolderNameEditText.setText(cardholderName);
        if (cardViewsActive()) {
            mCardView.fillCardholderName(cardholderName);
        }
    }

    @Override
    public void setIdentificationNumber(String identificationNumber) {
        mIdentificationNumberEditText.setText(identificationNumber);
        if (cardViewsActive()) {
            mIdentificationCardView.setIdentificationNumber(identificationNumber);
        }
    }

    @Override
    public void setCardholderNameListeners() {
        mCardHolderNameEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        mCardHolderNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
        mCardHolderNameEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEditText(mCardHolderNameEditText, event);
                return true;
            }
        });
        mCardHolderNameEditText.addTextChangedListener(new CardholderNameTextWatcher(new CardholderNameEditTextCallback() {
            @Override
            public void checkOpenKeyboard() {
                openKeyboard(mCardHolderNameEditText);
            }

            @Override
            public void saveCardholderName(CharSequence string) {
                mPresenter.saveCardholderName(string.toString());
                if (cardViewsActive()) {
                    mCardView.drawEditingCardHolderName(string.toString());
                }
            }

            @Override
            public void changeErrorView() {
                checkChangeErrorView();
            }

            @Override
            public void toggleLineColorOnError(boolean toggle) {
                mCardHolderNameEditText.toggleLineColorOnError(toggle);
            }
        }));
    }

    @Override
    public void setExpiryDateListeners() {
        mCardExpiryDateEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
        mCardExpiryDateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEditText(mCardExpiryDateEditText, event);
                return true;
            }
        });
        mCardExpiryDateEditText.addTextChangedListener(new CardExpiryDateTextWatcher(new CardExpiryDateEditTextCallback() {
            @Override
            public void checkOpenKeyboard() {
                openKeyboard(mCardExpiryDateEditText);
            }

            @Override
            public void saveExpiryMonth(CharSequence string) {
                mPresenter.saveExpiryMonth(string.toString());
                if (cardViewsActive()) {
                    mCardView.drawEditingExpiryMonth(string.toString());
                }
            }

            @Override
            public void saveExpiryYear(CharSequence string) {
                mPresenter.saveExpiryYear(string.toString());
                if (cardViewsActive()) {
                    mCardView.drawEditingExpiryYear(string.toString());
                }
            }

            @Override
            public void changeErrorView() {
                checkChangeErrorView();
            }

            @Override
            public void toggleLineColorOnError(boolean toggle) {
                mCardExpiryDateEditText.toggleLineColorOnError(toggle);
            }

            @Override
            public void appendDivider() {
                mCardExpiryDateEditText.append("/");
            }

            @Override
            public void deleteChar(CharSequence string) {
                mCardExpiryDateEditText.getText().delete(string.length() - 1, string.length());
            }
        }));
    }

    @Override
    public void setSecurityCodeListeners() {
        mSecurityCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
        mSecurityCodeEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEditText(mSecurityCodeEditText, event);
                return true;
            }
        });
        mSecurityCodeEditText.addTextChangedListener(new CardSecurityCodeTextWatcher(new CardSecurityCodeEditTextCallback() {
            @Override
            public void checkOpenKeyboard() {
                openKeyboard(mSecurityCodeEditText);
            }

            @Override
            public void saveSecurityCode(CharSequence string) {
                mPresenter.saveSecurityCode(string.toString());
                if (cardViewsActive()) {
                    mCardView.setSecurityCodeLocation(mPresenter.getSecurityCodeLocation());
                    mCardView.drawEditingSecurityCode(string.toString());
                }
            }

            @Override
            public void changeErrorView() {
                checkChangeErrorView();
            }

            @Override
            public void toggleLineColorOnError(boolean toggle) {
                mSecurityCodeEditText.toggleLineColorOnError(toggle);
            }
        }));
    }

    @Override
    public void setIdentificationTypeListeners() {
        mIdentificationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mPresenter.saveIdentificationType((IdentificationType) mIdentificationTypeSpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mIdentificationTypeSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mCurrentEditingEditText.equals(CARD_SECURITYCODE_INPUT)) {
                    return false;
                }
                checkTransitionCardToId();
                openKeyboard(mIdentificationNumberEditText);
                return false;
            }
        });
    }

    @Override
    public void setIdentificationNumberListeners() {
        mIdentificationNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
        mIdentificationNumberEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEditText(mIdentificationNumberEditText, event);
                return true;
            }
        });
        mIdentificationNumberEditText.addTextChangedListener(new CardIdentificationNumberTextWatcher(new CardIdentificationNumberEditTextCallback() {
            @Override
            public void checkOpenKeyboard() {
                openKeyboard(mIdentificationNumberEditText);
            }

            @Override
            public void saveIdentificationNumber(CharSequence string) {
                mPresenter.saveIdentificationNumber(string.toString());
                if (mPresenter.getIdentificationNumberMaxLength() == string.length()) {
                    mPresenter.setIdentificationNumber(string.toString());
                    mPresenter.validateIdentificationNumber();
                }
                if (cardViewsActive()) {
                    mIdentificationCardView.setIdentificationNumber(string.toString());
                    if (showingIdentification()) {
                        mIdentificationCardView.draw();
                    }
                }
            }

            @Override
            public void changeErrorView() {
                checkChangeErrorView();
            }

            @Override
            public void toggleLineColorOnError(boolean toggle) {
                mIdentificationNumberEditText.toggleLineColorOnError(toggle);
            }
        }));
    }

    @Override
    public void setIdentificationNumberRestrictions(String type) {
        setInputMaxLength(mIdentificationNumberEditText, mPresenter.getIdentificationNumberMaxLength());
        if ("number".equals(type)) {
            mIdentificationNumberEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            mIdentificationNumberEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        if (!mIdentificationNumberEditText.getText().toString().isEmpty()) {
            mPresenter.validateIdentificationNumber();
        }
    }

    @Override
    public void initializeIdentificationTypes(List<IdentificationType> identificationTypes) {
        mIdentificationTypeSpinner.setAdapter(new IdentificationTypesAdapter(this, identificationTypes));
        mIdentificationTypeContainer.setVisibility(View.VISIBLE);
        if (cardViewsActive()) {
            mIdentificationCardView.setIdentificationType(identificationTypes.get(0));
        }
    }

    @Override
    public void setSecurityCodeViewLocation(String location) {
        if (location.equals(CardView.CARD_SIDE_FRONT) && cardViewsActive()) {
            mCardView.hasToShowSecurityCodeInFront(true);
        }
    }

    private void onTouchEditText(MPEditText editText, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            openKeyboard(editText);
        }
    }

    private boolean onNextKey(int actionId, KeyEvent event) {
        if (isNextKey(actionId, event)) {
            validateCurrentEditText();
            return true;
        }
        return false;
    }

    private boolean isNextKey(int actionId, KeyEvent event) {
        return actionId == EditorInfo.IME_ACTION_NEXT ||
                (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
    }

    @Override
    public void setSecurityCodeInputMaxLength(int length) {
        setInputMaxLength(mSecurityCodeEditText, length);
    }

    @Override
    public void showApiExceptionError(ApiException exception) {
        ApiUtil.showApiExceptionError(mActivity, exception);
    }

    @Override
    public void startErrorView(String message, String errorDetail) {
        ErrorUtil.startErrorActivity(mActivity, message, errorDetail, false);
    }

    @Override
    public void setCardNumberInputMaxLength(int length) {
        setInputMaxLength(mCardNumberEditText, length);
    }

    private void setInputMaxLength(MPEditText text, int maxLength) {
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        text.setFilters(fArray);
    }

    private void clearCardNumberInputLength() {
        int maxLength = MPCardMaskUtil.CARD_NUMBER_MAX_LENGTH;
        setInputMaxLength(mCardNumberEditText, maxLength);
    }

    private void openKeyboard(MPEditText ediText) {
        ediText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ediText, InputMethodManager.SHOW_IMPLICIT);
        fullScrollDown();
    }

    private void fullScrollDown() {
        Runnable r = new Runnable() {
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        };
        mScrollView.post(r);
        r.run();
    }

    private void requestCardNumberFocus() {
        MPTracker.getInstance().trackScreen("CARD_NUMBER", "2", mPresenter.getPublicKey(),
                BuildConfig.VERSION_NAME, this);
        disableBackInputButton();
        mCurrentEditingEditText = CARD_NUMBER_INPUT;
        openKeyboard(mCardNumberEditText);
        if (cardViewsActive()) {
            mCardView.drawEditingCardNumber(mPresenter.getCardNumber());
        } else {
            initializeTitle();
        }
    }

    private void requestCardHolderNameFocus() {
        if (!mPresenter.validateCardNumber()) {
            return;
        }
        MPTracker.getInstance().trackScreen("CARD_HOLDER_NAME", "2", mPresenter.getPublicKey(),
                BuildConfig.VERSION_NAME, this);
        enableBackInputButton();
        mCurrentEditingEditText = CARDHOLDER_NAME_INPUT;
        openKeyboard(mCardHolderNameEditText);
        if (cardViewsActive()) {
            mCardView.drawEditingCardHolderName(mPresenter.getCardholderName());
        }
    }

    private void requestExpiryDateFocus() {
        if (!mPresenter.validateCardName()) {
            return;
        }
        MPTracker.getInstance().trackScreen("CARD_EXPIRY_DATE", "2", mPresenter.getPublicKey(),
                BuildConfig.VERSION_NAME, this);
        enableBackInputButton();
        mCurrentEditingEditText = CARD_EXPIRYDATE_INPUT;
        openKeyboard(mCardExpiryDateEditText);
        checkFlipCardToFront();
        if (cardViewsActive()) {
            mCardView.drawEditingExpiryMonth(mPresenter.getExpiryMonth());
            mCardView.drawEditingExpiryYear(mPresenter.getExpiryYear());
        } else {
            initializeTitle();
        }
    }

    private void requestSecurityCodeFocus() {
        if (!mPresenter.validateExpiryDate()) {
            return;
        }
        if (mCurrentEditingEditText.equals(CARD_EXPIRYDATE_INPUT) ||
                mCurrentEditingEditText.equals(CARD_IDENTIFICATION_INPUT) ||
                mCurrentEditingEditText.equals(CARD_SECURITYCODE_INPUT)) {
            MPTracker.getInstance().trackScreen("CARD_SECURITY_CODE", "2", mPresenter.getPublicKey(),
                    BuildConfig.VERSION_NAME, this);
            enableBackInputButton();
            mCurrentEditingEditText = CARD_SECURITYCODE_INPUT;
            openKeyboard(mSecurityCodeEditText);
            if (mPresenter.getSecurityCodeLocation().equals(CardView.CARD_SIDE_BACK)) {
                checkFlipCardToBack();
            } else {
                checkFlipCardToFront();
            }
            initializeTitle();
        }
    }

    private void requestIdentificationFocus() {
        if ((mPresenter.isSecurityCodeRequired() && !mPresenter.validateSecurityCode()) ||
                (!mPresenter.isSecurityCodeRequired() && !mPresenter.validateExpiryDate())) {
            return;
        }
        MPTracker.getInstance().trackScreen("IDENTIFICATION_NUMBER", "2", mPresenter.getPublicKey(),
                BuildConfig.VERSION_NAME, this);
        enableBackInputButton();
        mCurrentEditingEditText = CARD_IDENTIFICATION_INPUT;
        openKeyboard(mIdentificationNumberEditText);
        checkTransitionCardToId();
        if (mLowResActive) {
            mLowResTitleToolbar.setText(getResources().getString(R.string.mpsdk_form_identification_title));
        }
    }

    private void disableBackInputButton() {
        mBackButton.setVisibility(View.GONE);
        mBackInactiveButton.setVisibility(View.VISIBLE);
    }

    private void enableBackInputButton() {
        mBackButton.setVisibility(View.VISIBLE);
        mBackInactiveButton.setVisibility(View.GONE);
    }

    @Override
    public void hideIdentificationInput() {
        mCardIdentificationInput.setVisibility(View.GONE);
    }

    @Override
    public void hideSecurityCodeInput() {
        mCardSecurityCodeInput.setVisibility(View.GONE);
    }

    @Override
    public void showIdentificationInput() {
        mCardIdentificationInput.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSecurityCodeInput() {
        mCardSecurityCodeInput.setVisibility(View.VISIBLE);
    }

    @Override
    public void setErrorView(String message) {
        mButtonContainer.setVisibility(View.GONE);
        mErrorContainer.setVisibility(View.VISIBLE);
        mErrorTextView.setText(message);
        setErrorState(ERROR_STATE);
    }

    @Override
    public void clearErrorView() {
        mButtonContainer.setVisibility(View.VISIBLE);
        mErrorContainer.setVisibility(View.GONE);
        mErrorTextView.setText("");
        setErrorState(NORMAL_STATE);
    }

    @Override
    public void setErrorCardNumber() {
        mCardNumberEditText.toggleLineColorOnError(true);
        mCardNumberEditText.requestFocus();
    }

    @Override
    public void setErrorCardholderName() {
        mCardHolderNameEditText.toggleLineColorOnError(true);
        mCardHolderNameEditText.requestFocus();
    }

    @Override
    public void setErrorExpiryDate() {
        mCardExpiryDateEditText.toggleLineColorOnError(true);
        mCardExpiryDateEditText.requestFocus();
    }

    @Override
    public void setErrorSecurityCode() {
        mSecurityCodeEditText.toggleLineColorOnError(true);
        mSecurityCodeEditText.requestFocus();
    }

    @Override
    public void setErrorIdentificationNumber() {
        mIdentificationNumberEditText.toggleLineColorOnError(true);
        mIdentificationNumberEditText.requestFocus();
    }

    @Override
    public void clearErrorIdentificationNumber() {
        mIdentificationNumberEditText.toggleLineColorOnError(false);
    }

    private void setErrorState(String mErrorState) {
        this.mErrorState = mErrorState;
    }

    private void checkChangeErrorView() {
        if (mErrorState != null && mErrorState.equals(ERROR_STATE)) {
            clearErrorView();
        }
    }

    private boolean validateCurrentEditText() {
        switch (mCurrentEditingEditText) {
            case CARD_NUMBER_INPUT:
                if (mPresenter.validateCardNumber()) {
                    mCardNumberInput.setVisibility(View.GONE);
                    requestCardHolderNameFocus();
                    return true;
                }
                return false;
            case CARDHOLDER_NAME_INPUT:
                if (mPresenter.validateCardName()) {
                    mCardholderNameInput.setVisibility(View.GONE);
                    requestExpiryDateFocus();
                    return true;
                }
                return false;
            case CARD_EXPIRYDATE_INPUT:
                if (mPresenter.validateExpiryDate()) {
                    mCardExpiryDateInput.setVisibility(View.GONE);
                    if (mPresenter.isSecurityCodeRequired()) {
                        requestSecurityCodeFocus();
                    } else if (mPresenter.isIdentificationNumberRequired()) {
                        requestIdentificationFocus();
                    } else {
                        checkFinishWithCardToken();
                    }
                    return true;
                }
                return false;
            case CARD_SECURITYCODE_INPUT:
                if (mPresenter.validateSecurityCode()) {
                    mCardSecurityCodeInput.setVisibility(View.GONE);
                    if (mPresenter.isIdentificationNumberRequired()) {
                        requestIdentificationFocus();
                    } else {
                        checkFinishWithCardToken();
                    }
                    return true;
                }
                return false;
            case CARD_IDENTIFICATION_INPUT:
                if (mPresenter.validateIdentificationNumber()) {
                    checkFinishWithCardToken();
                    return true;
                }
                return false;
        }
        return false;
    }

    private boolean checkIsEmptyOrValid() {
        switch (mCurrentEditingEditText) {
            case CARDHOLDER_NAME_INPUT:
                if (mPresenter.checkIsEmptyOrValidCardholderName()) {
                    mCardNumberInput.setVisibility(View.VISIBLE);
                    requestCardNumberFocus();
                    return true;
                }
                return false;
            case CARD_EXPIRYDATE_INPUT:
                if (mPresenter.checkIsEmptyOrValidExpiryDate()) {
                    mCardholderNameInput.setVisibility(View.VISIBLE);
                    requestCardHolderNameFocus();
                    return true;
                }
                return false;
            case CARD_SECURITYCODE_INPUT:
                if (mPresenter.checkIsEmptyOrValidSecurityCode()) {
                    mCardExpiryDateInput.setVisibility(View.VISIBLE);
                    requestExpiryDateFocus();
                    return true;
                }
                return false;
            case CARD_IDENTIFICATION_INPUT:
                if (mPresenter.checkIsEmptyOrValidIdentificationNumber()) {
                    if (mPresenter.isSecurityCodeRequired()) {
                        mCardSecurityCodeInput.setVisibility(View.VISIBLE);
                        requestSecurityCodeFocus();
                    } else {
                        mCardExpiryDateInput.setVisibility(View.VISIBLE);
                        requestExpiryDateFocus();
                    }
                    return true;
                }
                return false;
        }
        return false;
    }

    private void checkTransitionCardToId() {
        if (!mPresenter.isIdentificationNumberRequired()) {
            return;
        }
        if (showingFront() || showingBack()) {
            transitionToIdentification();
        }
    }

    private void checkFlipCardToBack() {
        if (showingFront()) {
            flipCardToBack();
        } else if (showingIdentification()) {
            if (cardViewsActive()) {
                MPAnimationUtils.transitionCardDisappear(this, mCardView, mIdentificationCardView);
            }
            mCardSideState = CardView.CARD_SIDE_BACK;
            showBankDeals();
        }
    }

    private void checkFlipCardToFront() {
        if (showingBack() || showingIdentification()) {
            if (showingBack()) {
                flipCardToFrontFromBack();
            } else if (showingIdentification()) {
                if (cardViewsActive()) {
                    MPAnimationUtils.transitionCardDisappear(this, mCardView, mIdentificationCardView);
                }
                mCardSideState = CardView.CARD_SIDE_FRONT;
            }
            showBankDeals();
        }
    }

    private void transitionToIdentification() {
        hideBankDeals();
        mCardSideState = CARD_IDENTIFICATION;
        if (cardViewsActive()) {
            MPAnimationUtils.transitionCardAppear(this, mCardView, mIdentificationCardView);
            mIdentificationCardView.draw();
        }
    }

    private void flipCardToBack() {
        mCardSideState = CardView.CARD_SIDE_BACK;
        if (cardViewsActive()) {
            mCardView.flipCardToBack(mPresenter.getPaymentMethod(), mPresenter.getSecurityCodeLength(),
                    getWindow(), mCardBackground, mPresenter.getSecurityCode());
        }
    }

    private void flipCardToFrontFromBack() {
        mCardSideState = CardView.CARD_SIDE_FRONT;
        if (cardViewsActive()) {
            mCardView.flipCardToFrontFromBack(getWindow(), mCardBackground, mPresenter.getCardNumber(),
                    mPresenter.getCardholderName(), mPresenter.getExpiryMonth(), mPresenter.getExpiryYear(),
                    mPresenter.getSecurityCodeFront());
        }
    }

    private void initCardState() {
        if (mCardSideState == null) {
            mCardSideState = CardView.CARD_SIDE_FRONT;
        }
    }

    private boolean showingIdentification() {
        initCardState();
        return mCardSideState.equals(CARD_IDENTIFICATION);
    }

    private boolean showingBack() {
        initCardState();
        return mCardSideState.equals(CardView.CARD_SIDE_BACK);
    }

    private boolean showingFront() {
        initCardState();
        return mCardSideState.equals(CardView.CARD_SIDE_FRONT);
    }

    private void checkFinishWithCardToken() {
        if (mPresenter.hasToShowPaymentTypes() && mPresenter.getGuessedPaymentMethods() != null) {
            List<PaymentMethod> paymentMethods = mPresenter.getGuessedPaymentMethods();
            List<PaymentType> paymentTypes = mPresenter.getPaymentTypes();
            new MercadoPagoComponents.Activities.PaymentTypesActivityBuilder()
                    .setActivity(mActivity)
                    .setMerchantPublicKey(mPresenter.getPublicKey())
                    .setPaymentMethods(paymentMethods)
                    .setPaymentTypes(paymentTypes)
                    .setCardInfo(new CardInfo(mPresenter.getCardToken()))
                    .setDecorationPreference(mDecorationPreference)
                    .startActivity();
            overridePendingTransition(R.anim.mpsdk_slide_right_to_left_in, R.anim.mpsdk_slide_right_to_left_out);

        } else {
            finishWithCardToken();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MercadoPagoComponents.Activities.PAYMENT_TYPES_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                PaymentType paymentType = JsonUtil.getInstance().fromJson(bundle.getString("paymentType"), PaymentType.class);
                mPresenter.setSelectedPaymentType(paymentType);
                finishWithCardToken();
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        } else if (requestCode == MercadoPagoComponents.Activities.DISCOUNTS_REQUEST_CODE) {
            resolveDiscountRequest(resultCode, data);
        } else if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mPresenter.recoverFromFailure();
            } else {
                setResult(resultCode, data);
                finish();
            }
        }
    }

    private void resolveDiscountRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (mPresenter.getDiscount() == null) {
                Discount discount = JsonUtil.getInstance().fromJson(data.getStringExtra("discount"), Discount.class);
                mPresenter.onDiscountReceived(discount);
            }
        }
    }

    private void finishWithCardToken() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPresenter.getPaymentMethod()));
        returnIntent.putExtra("cardToken", JsonUtil.getInstance().toJson(mPresenter.getCardToken()));
        returnIntent.putExtra("discount", JsonUtil.getInstance().toJson(mPresenter.getDiscount()));
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.mpsdk_slide_right_to_left_in, R.anim.mpsdk_slide_right_to_left_out);
    }

    @Override
    public void onBackPressed() {
        checkFlipCardToFront();
        MPTracker.getInstance().trackEvent("GUESSING_CARD", "BACK_PRESSED", "2", mPresenter.getPublicKey(),
                BuildConfig.VERSION_NAME, this);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("discount", JsonUtil.getInstance().toJson(mPresenter.getDiscount()));
        returnIntent.putExtra("backButtonPressed", true);
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void onTimeChanged(String timeToShow) {
        mTimerTextView.setText(timeToShow);
    }

    @Override
    public void onFinish() {
        this.finish();
    }

    public void initializeDiscountActivity(View view) {
        mPresenter.initializeDiscountActivity();
    }

    @Override
    public void startDiscountActivity(BigDecimal transactionAmount) {
        setSoftInputMode();

        MercadoPagoComponents.Activities.DiscountsActivityBuilder discountsActivityBuilder =
                new MercadoPagoComponents.Activities.DiscountsActivityBuilder();

        discountsActivityBuilder.setActivity(this)
                .setMerchantPublicKey(mPresenter.getPublicKey())
                .setPayerEmail(mPresenter.getPayerEmail())
                .setAmount(transactionAmount)
                .setDiscount(mPresenter.getDiscount())
                .setDecorationPreference(mDecorationPreference);

        if (mPresenter.getDiscount() == null) {
            discountsActivityBuilder.setDirectDiscountEnabled(false);
        } else {
            discountsActivityBuilder.setDiscount(mPresenter.getDiscount());
        }

        discountsActivityBuilder.startActivity();
    }

    @Override
    public void showDiscountRow(BigDecimal transactionAmount) {
        MercadoPagoUI.Views.DiscountRowViewBuilder discountRowBuilder = new MercadoPagoUI.Views.DiscountRowViewBuilder();

        discountRowBuilder.setContext(this)
                .setDiscount(mPresenter.getDiscount())
                .setTransactionAmount(transactionAmount)
                .setShortRowEnabled(true)
                .setDiscountEnabled(mPresenter.getDiscountEnabled());

        if (mPresenter.getDiscount() != null) {
            discountRowBuilder.setCurrencyId(mPresenter.getDiscount().getCurrencyId());
        }

        DiscountRowView discountRowView = discountRowBuilder.build();

        discountRowView.inflateInParent(mDiscountFrameLayout, true);
        discountRowView.initializeControls();
        discountRowView.draw();
        discountRowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initializeDiscountActivity(view);
            }
        });
    }

    @Override
    public void setSoftInputMode() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    public GuessingCardPresenter getPresenter() {
        return mPresenter;
    }
}

