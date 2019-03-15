package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;

import com.mercadopago.android.px.core.PaymentMethodPlugin;
import com.mercadopago.android.px.internal.datasource.PaymentVaultTitleSolver;
import com.mercadopago.android.px.internal.datasource.PaymentVaultTitleSolverImpl;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESC;
import com.mercadopago.android.px.internal.features.uicontrollers.AmountRowController;
import com.mercadopago.android.px.internal.navigation.DefaultPayerInformationDriver;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.AmountView;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CustomSearchItem;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentMethodSearchItem;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.tracking.internal.views.SelectMethodChildView;
import com.mercadopago.android.px.tracking.internal.views.SelectMethodView;
import java.util.Collection;
import java.util.List;

public class PaymentVaultPresenter extends BasePresenter<PaymentVaultView> implements AmountView.OnClick,
    PaymentVault.Actions, AmountRowController.AmountRowVisibilityBehaviour {

    @NonNull
    private final PaymentSettingRepository paymentSettingRepository;
    @NonNull
    private final UserSelectionRepository userSelectionRepository;
    @NonNull
    private final PluginRepository pluginRepository;

    private final DiscountRepository discountRepository;
    @NonNull
    private final GroupsRepository groupsRepository;

    @NonNull private final MercadoPagoESC mercadoPagoESC;

    @NonNull private final PaymentVaultTitleSolver titleSolver;

    private PaymentMethodSearchItem selectedSearchItem;

    /* default */ PaymentMethodSearch paymentMethodSearch;
    private FailureRecovery failureRecovery;
    private AmountRowController amountRowController;

    public PaymentVaultPresenter(@NonNull final PaymentSettingRepository paymentSettingRepository,
                                 @NonNull final UserSelectionRepository userSelectionRepository,
                                 @NonNull final PluginRepository pluginService,
                                 @NonNull final DiscountRepository discountRepository,
                                 @NonNull final GroupsRepository groupsRepository,
                                 @NonNull final MercadoPagoESC mercadoPagoESC,
                                 @NonNull final PaymentVaultTitleSolver titleSolver) {
        this.paymentSettingRepository = paymentSettingRepository;
        this.userSelectionRepository = userSelectionRepository;
        pluginRepository = pluginService;
        this.discountRepository = discountRepository;
        this.groupsRepository = groupsRepository;
        this.mercadoPagoESC = mercadoPagoESC;
        this.titleSolver = titleSolver;
    }

    public void initialize() {
        try {
            validateParameters();
            initPaymentVaultFlow();
            getView().setTitle(titleSolver.solveTitle());
        } catch (final IllegalStateException exception) {
            getView().showError(MercadoPagoError.createNotRecoverable(exception.getMessage()), TextUtil.EMPTY);
        }
    }

    public void initPaymentVaultFlow() {
        initializeAmountRow();

        groupsRepository.getGroups().enqueue(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                if (isViewAttached()) {
                    PaymentVaultPresenter.this.paymentMethodSearch = paymentMethodSearch;
                    initPaymentMethodSearch();
                }
            }

            @Override
            public void failure(final ApiException apiException) {
                getView()
                    .showError(MercadoPagoError.createNotRecoverable(apiException, ApiException.ErrorCodes.PAYMENT_METHOD_NOT_FOUND),
                        ApiException.ErrorCodes.PAYMENT_METHOD_NOT_FOUND);
                setFailureRecovery(new FailureRecovery() {
                    @Override
                    public void recover() {
                        initPaymentVaultFlow();
                    }
                });
            }
        });
    }

    /* default */ void setFailureRecovery(final FailureRecovery failureRecovery) {
        this.failureRecovery = failureRecovery;
    }

    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        }
    }

    public void initializeAmountRow() {
        if (isViewAttached()) {
            amountRowController = new AmountRowController(this, paymentSettingRepository.getAdvancedConfiguration());
            amountRowController.configure();
        }
    }

    @Override
    public void showAmountRow() {
        getView().showAmount(discountRepository.getCurrentConfiguration(),
                paymentSettingRepository.getCheckoutPreference().getTotalAmount(),
                paymentSettingRepository.getCheckoutPreference().getSite());
    }

    @Override
    public void hideAmountRow() {
        getView().hideAmountRow();
    }

    public void onPayerInformationReceived() {
        getView().finishPaymentMethodSelection(userSelectionRepository.getPaymentMethod());
    }

    private void validateParameters() throws IllegalStateException {
        final PaymentPreference paymentPreference =
            paymentSettingRepository.getCheckoutPreference().getPaymentPreference();
        if (!paymentPreference.validMaxInstallments()) {
            throw new IllegalStateException("Invalid max installments number");
        }
        if (!paymentPreference.validDefaultInstallments()) {
            throw new IllegalStateException("Invalid installments number by default");
        }
        if (!paymentPreference.excludedPaymentTypesValid()) {
            throw new IllegalStateException("All payments types excluded");
        }
    }

    public boolean isItemSelected() {
        return selectedSearchItem != null;
    }

    /* default */ void initPaymentMethodSearch() {
        showPaymentMethodGroup();
    }

    private void showPaymentMethodGroup() {
        if (isItemSelected()) {
            showSelectedItemChildren();
        } else {
            resolveAvailablePaymentMethods();
        }
    }

    private void showSelectedItemChildren() {
        trackScreen();
        getView().setTitle(selectedSearchItem.getChildrenHeader());
        getView().showSearchItems(selectedSearchItem.getChildren(), getPaymentMethodSearchItemSelectionCallback());
        getView().hideProgress();
    }

    private void resolveAvailablePaymentMethods() {
        if (noPaymentMethodsAvailable()) {
            getView().showEmptyPaymentMethodsError();
        } else if (isOnlyOneItemAvailable() && !isDiscountAvailable()) {
            if (pluginRepository.hasEnabledPaymentMethodPlugin()) {
                selectPluginPaymentMethod(pluginRepository.getFirstEnabledPlugin());
            } else if (!paymentMethodSearch.getGroups().isEmpty()) {
                selectItem(paymentMethodSearch.getGroups().get(0), true);
            } else if (!paymentMethodSearch.getCustomSearchItems().isEmpty()) {
                if (PaymentTypes.CREDIT_CARD.equals(paymentMethodSearch.getCustomSearchItems().get(0).getType())) {
                    selectCustomOption(paymentMethodSearch.getCustomSearchItems().get(0));
                }
            }
        } else {
            showAvailableOptions();
            getView().hideProgress();
        }
    }

    private void selectItem(final PaymentMethodSearchItem item) {
        selectItem(item, false);
    }

    private void selectItem(final PaymentMethodSearchItem item, final Boolean automaticSelection) {
        userSelectionRepository.select((Card) null, null);

        if (item.hasChildren()) {
            getView().showSelectedItem(item);
        } else if (item.isPaymentType()) {
            startNextStepForPaymentType(item, automaticSelection);
        } else if (item.isPaymentMethod()) {
            resolvePaymentMethodSelection(item);
        }
    }

    private void showAvailableOptions() {
        if (paymentMethodSearch.hasCustomSearchItems()) {
            final List<CustomSearchItem> shownCustomItems;
            shownCustomItems = paymentMethodSearch.getCustomSearchItems();
            getView().showCustomOptions(shownCustomItems, getCustomOptionCallback());
        }

        if (searchItemsAvailable()) {
            getView().showSearchItems(paymentMethodSearch.getGroups(), getPaymentMethodSearchItemSelectionCallback());
        }

        trackScreen();
    }

    private OnSelectedCallback<PaymentMethodSearchItem> getPaymentMethodSearchItemSelectionCallback() {
        return new OnSelectedCallback<PaymentMethodSearchItem>() {
            @Override
            public void onSelected(PaymentMethodSearchItem item) {
                selectItem(item);
            }
        };
    }

    private OnSelectedCallback<CustomSearchItem> getCustomOptionCallback() {
        return new OnSelectedCallback<CustomSearchItem>() {
            @Override
            public void onSelected(final CustomSearchItem searchItem) {
                selectCustomOption(searchItem);
            }
        };
    }

    private void selectCustomOption(final CustomSearchItem item) {
        if (PaymentTypes.isCardPaymentType(item.getType())) {
            final Card card = getCardWithPaymentMethod(item);
            userSelectionRepository.select(card, null);
            getView().startSavedCardFlow(card);
        } else if (PaymentTypes.isAccountMoney(item.getType())) {
            final PaymentMethod paymentMethod = paymentMethodSearch.getPaymentMethodById(item.getPaymentMethodId());
            userSelectionRepository.select(paymentMethod, null);
            getView().finishPaymentMethodSelection(paymentMethod);
        }
    }

    private Card getCardWithPaymentMethod(final CustomSearchItem searchItem) {
        final PaymentMethod paymentMethod = paymentMethodSearch.getPaymentMethodById(searchItem.getPaymentMethodId());
        final Card selectedCard = getCardById(paymentMethodSearch.getCards(), searchItem.getId());
        if (paymentMethod != null) {
            selectedCard.setPaymentMethod(paymentMethod);
            if (selectedCard.getSecurityCode() == null && paymentMethod.getSettings() != null &&
                paymentMethod.getSettings().get(0) != null) {
                selectedCard.setSecurityCode(paymentMethod.getSettings().get(0).getSecurityCode());
            }
        }
        return selectedCard;
    }

    private Card getCardById(final Iterable<Card> savedCards, final String cardId) {
        Card foundCard = null;
        for (final Card card : savedCards) {
            if (card.getId().equals(cardId)) {
                foundCard = card;
                break;
            }
        }
        return foundCard;
    }

    private void startNextStepForPaymentType(final PaymentMethodSearchItem item, final boolean automaticSelection) {

        final String itemId = item.getId();

        if (PaymentTypes.isCardPaymentType(itemId)) {
                userSelectionRepository.select(itemId);
                getView().startCardFlow(automaticSelection);
        } else {
            getView().startPaymentMethodsSelection(
                paymentSettingRepository.getCheckoutPreference().getPaymentPreference());
        }
    }

    private void resolvePaymentMethodSelection(final PaymentMethodSearchItem item) {

        final PaymentMethod selectedPaymentMethod = paymentMethodSearch.getPaymentMethodBySearchItem(item);
        userSelectionRepository.select(selectedPaymentMethod, null);

        if (selectedPaymentMethod == null) {
            getView().showMismatchingPaymentMethodError();
        } else {
            handleCollectPayerInformation(selectedPaymentMethod);
        }
    }

    private void handleCollectPayerInformation(final PaymentMethod selectedPaymentMethod) {
        new DefaultPayerInformationDriver(paymentSettingRepository.getCheckoutPreference().getPayer(),
            selectedPaymentMethod).drive(
            new DefaultPayerInformationDriver.PayerInformationDriverCallback() {
                @Override
                public void driveToNewPayerData() {
                    getView().collectPayerInformation();
                }

                @Override
                public void driveToReviewConfirm() {
                    getView().finishPaymentMethodSelection(selectedPaymentMethod);
                }
            });
    }

    public boolean isOnlyOneItemAvailable() {
        int groupCount = 0;
        int customCount = 0;

        if (paymentMethodSearch != null && paymentMethodSearch.hasSearchItems()) {
            groupCount = paymentMethodSearch.getGroups().size();
            customCount = paymentMethodSearch.getCustomSearchItems().size();
        }

        int pluginCount = pluginRepository.getPaymentMethodPluginCount();

        return groupCount + customCount + pluginCount == 1;
    }

    private boolean searchItemsAvailable() {
        return paymentMethodSearch != null && paymentMethodSearch.getGroups() != null
            && (!paymentMethodSearch.getGroups().isEmpty() || pluginRepository.hasEnabledPaymentMethodPlugin());
    }

    private boolean noPaymentMethodsAvailable() {
        return (paymentMethodSearch.getGroups() == null || paymentMethodSearch.getGroups().isEmpty())
            &&
            (paymentMethodSearch.getCustomSearchItems() == null || paymentMethodSearch.getCustomSearchItems().isEmpty())
            && !pluginRepository.hasEnabledPaymentMethodPlugin();
    }

    public PaymentMethodSearchItem getSelectedSearchItem() {
        return selectedSearchItem;
    }

    public void setSelectedSearchItem(PaymentMethodSearchItem mSelectedSearchItem) {
        selectedSearchItem = mSelectedSearchItem;
    }

    public void trackScreen() {
        // Do not remove check paymentMethodSearch, sometimes in recovery status is null.
        if(paymentMethodSearch != null) {
            if (selectedSearchItem == null) {
                trackInitialScreen();
            } else {
                trackChildScreen();
            }
        }
    }

    private void trackInitialScreen() {
        final SelectMethodView selectMethodView =
            new SelectMethodView(paymentMethodSearch, mercadoPagoESC.getESCCardIds(),
                paymentSettingRepository.getCheckoutPreference());
        setCurrentViewTracker(selectMethodView);
    }

    private void trackChildScreen() {
        final SelectMethodChildView selectMethodChildView =
            new SelectMethodChildView(paymentMethodSearch, selectedSearchItem,
                paymentSettingRepository.getCheckoutPreference());
        setCurrentViewTracker(selectMethodChildView);
    }

    @Override
    public void trackOnBackPressed() {
        if (selectedSearchItem == null) {
            tracker.trackAbort();
        } else {
            tracker.trackBack();
        }
    }

    @Override
    public void onDetailClicked(@NonNull final DiscountConfigurationModel discountModel) {
        getView().showDetailDialog(discountModel);
    }

    public void selectPluginPaymentMethod(final PaymentMethodPlugin plugin) {
        userSelectionRepository
            .select(pluginRepository.getPluginAsPaymentMethod(plugin.getId(), PaymentTypes.PLUGIN), null);
        onPluginAfterHookOne();
    }

    public void onPluginAfterHookOne() {
        getView().finishPaymentMethodSelection(userSelectionRepository.getPaymentMethod());
    }

    public void onPaymentMethodReturned() {
        getView().finishPaymentMethodSelection(userSelectionRepository.getPaymentMethod());
    }

    private boolean isDiscountAvailable() {
        return discountRepository.getCurrentConfiguration().getDiscount() != null;
    }
}