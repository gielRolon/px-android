package com.mercadopago.android.px.model.exceptions;

import android.support.annotation.NonNull;

public class InvalidFieldException extends Exception {
    public static final int INVALID_IDENTIFICATION_LENGHT = 0;
    public static final int INVALID_CPF = 1;
    public static final int INVALID_CNPJ = 2;

    private final int errorCode;

    private InvalidFieldException(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @NonNull
    public static InvalidFieldException createInvalidCpfException() {
        return new InvalidFieldException(INVALID_CPF);
    }

    @NonNull
    public static InvalidFieldException createInvalidCnpjException() {
        return new InvalidFieldException(INVALID_CNPJ);
    }

    @NonNull
    public static InvalidFieldException createInvalidLengthException() {
        return new InvalidFieldException(INVALID_IDENTIFICATION_LENGHT);
    }
}
