package com.mercadopago.android.px.internal.util;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.CardToken;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.exceptions.InvalidFieldException;
import java.util.InputMismatchException;
import java.util.regex.Pattern;

public final class IdentificationUtils {

    private static final String CPF = "CPF";
    private static final String CNPJ = "CNPJ";

    private static final int CPF_ALGORITHM_EXPECTED_LENGTH = 11;
    private static final int CPF_ALGORITHM_LAST_INDEX = CPF_ALGORITHM_EXPECTED_LENGTH - 1;
    private static final int CPF_ALGORITHM_CHECK_DIGITS_INDEX = CPF_ALGORITHM_EXPECTED_LENGTH - 2;

    private static final int CNPJ_ALGORITHM_EXPECTED_LENGTH = 14;

    private static final Pattern CPF_VALID_NUMBERS_PATTERN = Pattern.compile(
        "(?=^((?!((([0]{11})|([1]{11})|([2]{11})|([3]{11})|([4]{11})|([5]{11})|([6]{11})|([7]{11})|([8]{11})|([9]{11})))).)*$)([0-9]{11})");

    private static final Pattern CNPJ_VALID_NUMBERS_PATTERN = Pattern.compile(
        "(?=^((?!((([0]{14})|([1]{14})|([2]{14})|([3]{14})|([4]{14})|([5]{14})|([6]{14})|([7]{14})|([8]{14})|([9]{14})))).)*$)([0-9]{14})");

    private IdentificationUtils() {
    }

    public static void validateTicketIdentification(final Identification identification,
        final IdentificationType identificationType) throws InvalidFieldException {
        if (identificationType != null && identification != null) {
            validateIdentificationNumberLength(identification, identificationType);
        }
        validateNumber(identification);
    }

    public static void validateCardIdentification(final CardToken cardToken, final Identification identification,
        final IdentificationType identificationType) throws InvalidFieldException {
        if (identificationType != null && identification != null) {
            validateCardTokenIdentificationNumber(cardToken, identification, identificationType);
        }
        validateNumber(identification);
    }

    private static void validateCardTokenIdentificationNumber(final CardToken cardToken,
        final Identification identification, final IdentificationType identificationType) throws InvalidFieldException {
        cardToken.getCardholder().setIdentification(identification);
        if (!cardToken.validateIdentificationNumber(identificationType)) {
            throw InvalidFieldException.createInvalidLengthException();
        }
    }

    private static void validateNumber(final Identification identification) throws InvalidFieldException {
        if (identification == null || TextUtil.isEmpty(identification.getNumber()) ||
            TextUtil.isEmpty(identification.getType())) {
            throw InvalidFieldException.createInvalidLengthException();
        } else if (identification.getType().equals(CPF)) {
            validateCpf(identification.getNumber());
        } else if (identification.getType().equals(CNPJ)) {
            validateCnpj(identification.getNumber());
        }
    }

    private static void validateIdentificationNumberLength(@NonNull final Identification identification,
        @NonNull final IdentificationType identificationType) throws InvalidFieldException {
        if (TextUtil.isNotEmpty(identification.getNumber())) {
            final int len = identification.getNumber().length();
            final Integer min = identificationType.getMinLength();
            final Integer max = identificationType.getMaxLength();
            if ((min != null) && (max != null) && !((len <= max) && (len >= min))) {
                throw InvalidFieldException.createInvalidLengthException();
            }
        } else {
            throw InvalidFieldException.createInvalidLengthException();
        }
    }

    private static void validateCpf(@NonNull final CharSequence cpf) throws InvalidFieldException {
        if (cpf.length() != CPF_ALGORITHM_EXPECTED_LENGTH) {
            return;
        }

        if (CPF_VALID_NUMBERS_PATTERN.matcher(cpf).matches()) {
            int[] numbers = new int[CPF_ALGORITHM_EXPECTED_LENGTH];
            for (int i = 0; i < CPF_ALGORITHM_EXPECTED_LENGTH; i++) {
                numbers[i] = Character.getNumericValue(cpf.charAt(i));
            }
            int i;
            int sum = 0;
            int factor = 100;
            for (i = 0; i < CPF_ALGORITHM_CHECK_DIGITS_INDEX; i++) {
                sum += numbers[i] * factor;
                factor -= CPF_ALGORITHM_LAST_INDEX;
            }
            int leftover = sum % CPF_ALGORITHM_EXPECTED_LENGTH;
            leftover = leftover == CPF_ALGORITHM_LAST_INDEX ? 0 : leftover;
            if (leftover == numbers[CPF_ALGORITHM_CHECK_DIGITS_INDEX]) {
                sum = 0;
                factor = 110;
                for (i = 0; i < CPF_ALGORITHM_LAST_INDEX; i++) {
                    sum += numbers[i] * factor;
                    factor -= CPF_ALGORITHM_LAST_INDEX;
                }
                leftover = sum % CPF_ALGORITHM_EXPECTED_LENGTH;
                leftover = leftover == CPF_ALGORITHM_LAST_INDEX ? 0 : leftover;
                if (leftover != numbers[CPF_ALGORITHM_LAST_INDEX]) {
                    throw InvalidFieldException.createInvalidCpfException();
                }
            }
        } else {
            throw InvalidFieldException.createInvalidCpfException();
        }
    }

    private static void validateCnpj(@NonNull final CharSequence cnpj) throws InvalidFieldException {
        if (cnpj.length() != CNPJ_ALGORITHM_EXPECTED_LENGTH) {
            return;
        }

        if (CNPJ_VALID_NUMBERS_PATTERN.matcher(cnpj).matches()) {
            char cnpj_first_check_digit, cnpj_second_check_digit;
            int sum, i, r, num, weight;

            // Protect code from int conversion errors.
            try {
                // 1. Digit verification.
                sum = 0;
                weight = 2;
                for (i = 11; i >= 0; i--) {
                    // Convert cnpj's i char into number.
                    num = (int) (cnpj.charAt(i) - 48);
                    sum = sum + (num * weight);
                    weight = weight + 1;
                    if (weight == 10) {
                        weight = 2;
                    }
                }

                r = sum % 11;
                if ((r == 0) || (r == 1)) {
                    cnpj_first_check_digit = '0';
                } else {
                    cnpj_first_check_digit = (char) ((11 - r) + 48);
                }

                // 2. Digit verification.
                sum = 0;
                weight = 2;
                for (i = 12; i >= 0; i--) {
                    num = (int) (cnpj.charAt(i) - 48);
                    sum = sum + (num * weight);
                    weight = weight + 1;
                    if (weight == 10) {
                        weight = 2;
                    }
                }

                r = sum % 11;
                if ((r == 0) || (r == 1)) {
                    cnpj_second_check_digit = '0';
                } else {
                    cnpj_second_check_digit = (char) ((11 - r) + 48);
                }

                // Checks whether the calculated digits match the digits entered.
                if (!((cnpj_first_check_digit == cnpj.charAt(12)) && (cnpj_second_check_digit == cnpj.charAt(13)))) {
                    throw new InvalidFieldException(InvalidFieldException.INVALID_CNPJ);
                }
            } catch (InputMismatchException e) {
                throw new InvalidFieldException(InvalidFieldException.INVALID_CNPJ);
            }
        } else {
            // Equal number sequences makes an invalid cnpj.
            throw new InvalidFieldException(InvalidFieldException.INVALID_CNPJ);
        }
    }
}