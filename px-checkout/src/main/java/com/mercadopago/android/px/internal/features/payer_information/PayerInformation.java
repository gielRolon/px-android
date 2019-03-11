package com.mercadopago.android.px.internal.features.payer_information;

import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.viewmodel.PayerInformationStateModel;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

public interface PayerInformation {

    interface View extends MvpView {

        void setIdentificationNumberRestrictions(String type);

        void clearErrorIdentificationNumber();

        void clearErrorName();

        void clearErrorLastName();

        void showErrorIdentificationNumber();

        void showErrorName();

        void showErrorLastName();

        void setErrorView(String message);

        void clearErrorView();

        void showCardFlowEnd();

        void showIdentificationLastNameFocus();

        void showIdentificationNameFocus();

        void requestIdentificationNumberFocus();

        void showError(MercadoPagoError error, String requestOrigin);

        void initializeIdentificationTypes(List<IdentificationType> identificationTypes,
            IdentificationType current);

        void showProgressBar();

        void hideProgressBar();

        void showMissingIdentificationTypesError();

        void showInvalidLengthIdentificationNumberErrorView();

        void showInvalidIdentificationNameErrorView();

        void showInvalidIdentificationLastNameErrorView();

        void setName(final String identificationName);

        void setLastName(final String identificationLastName);

        void setNumber(final String identificationNumber);

        void identificationDraw();

        void showInvalidIdentificationNumberErrorView();
    }

    interface Actions {

        void validateName();

        void validateLastName();

        void validateIdentification();

        void trackIdentificationNumberView();

        void trackIdentificationNameView();

        void trackIdentificationLastNameView();

        void trackAbort();

        void trackBack();

        PayerInformationStateModel getState();

        void focus(final String currentFocusType);
    }
}
