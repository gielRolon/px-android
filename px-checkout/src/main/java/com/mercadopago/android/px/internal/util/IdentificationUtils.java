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

    private static final Pattern CPF_VALID_NUMBERS_PATTERN = Pattern.compile(
        "(?=^((?!((([0]{11})|([1]{11})|([2]{11})|([3]{11})|([4]{11})|([5]{11})|([6]{11})|([7]{11})|([8]{11})|([9]{11})))).)*$)([0-9]{11})");

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
            isValidCnpj(identification.getNumber());
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

    private static void isValidCnpj(@NonNull String cnpj) throws InvalidFieldException {

        cnpj = removeSpecialCharacters(cnpj);

        // Equal number sequences makes an invalid cnpj.
        if (cnpj.equals("00000000000000") || cnpj.equals("11111111111111") || cnpj.equals("22222222222222") ||
            cnpj.equals("33333333333333") || cnpj.equals("44444444444444") || cnpj.equals("55555555555555") ||
            cnpj.equals("66666666666666") || cnpj.equals("77777777777777") || cnpj.equals("88888888888888") ||
            cnpj.equals("99999999999999") || (cnpj.length() != 14)) {
            throw new InvalidFieldException(InvalidFieldException.INVALID_CNPJ);
        }

        char dig13, dig14;
        int sm, i, r, num, peso;

        // Protect code from int conversion errors.
        try {
            // 1. Digit verification.
            sm = 0;
            peso = 2;
            for (i = 11; i >= 0; i--) {
                // Convert cnpj's i char into number.
                num = (int) (cnpj.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) {
                    peso = 2;
                }
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) {
                dig13 = '0';
            } else {
                dig13 = (char) ((11 - r) + 48);
            }

            // 2. Digit verification.
            sm = 0;
            peso = 2;
            for (i = 12; i >= 0; i--) {
                num = (int) (cnpj.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) {
                    peso = 2;
                }
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) {
                dig14 = '0';
            } else {
                dig14 = (char) ((11 - r) + 48);
            }

            // Checks whether the calculated digits match the digits entered.
            if (!((dig13 == cnpj.charAt(12)) && (dig14 == cnpj.charAt(13)))) {
                throw new InvalidFieldException(InvalidFieldException.INVALID_CNPJ);
            }
        } catch (InputMismatchException e) {
            throw new InvalidFieldException(InvalidFieldException.INVALID_CNPJ);
        }
    }

    private static String removeSpecialCharacters(@NonNull String cnpj) {
        if (cnpj.contains(".")) {
            cnpj = cnpj.replace(".", "");
        }
        if (cnpj.contains("-")) {
            cnpj = cnpj.replace("-", "");
        }
        if (cnpj.contains("/")) {
            cnpj = cnpj.replace("/", "");
        }
        return cnpj;
    }
}