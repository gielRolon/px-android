package com.mercadopago.android.px.mocks;

import com.google.gson.reflect.TypeToken;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.utils.ResourcesUtil;
import java.lang.reflect.Type;
import java.util.List;

public class IdentificationTypes {

    private static final String TYPE = "number";
    private static final String CPF_ID = "CPF";
    private static final String CNPJ_ID = "CNPJ";
    private static final String CPF_NAME = "CPF";
    private static final String CNPJ_NAME = "CNPJ";
    private static final int CPF_INVALID_LENGTH = 10;
    private static final int CPF_VALID_LENGTH = 11;
    private static final int CNPJ_VALID_LENGTH = 14;
    private static final String doNotFindIdentificationTypesException =
        "{\"message\":\"doesn't find identification types\",\"error\":\"identification types not found error\",\"cause\":[]}";

    public static IdentificationType getById(String id) {
        switch (id) {
        case "RUT":
            return new IdentificationType("RUT", "RUT", "string", 7, 20);
        case "CPF":
            return new IdentificationType("CPF", "CPF", "number", 11, 11);
        default:
            return new IdentificationType("DNI", "DNI", "number", 7, 8);
        }
    }

    public static ApiException getDoNotFindIdentificationTypesException() {
        return JsonUtil.getInstance().fromJson(doNotFindIdentificationTypesException, ApiException.class);
    }

    public static IdentificationType getIdentificationType() {
        List<IdentificationType> identificationTypesList = getIdentificationTypes();
        return identificationTypesList == null ? null : identificationTypesList.get(0);
    }

    public static List<IdentificationType> getIdentificationTypes() {
        List<IdentificationType> identificationTypesList;
        String json = ResourcesUtil.getStringResource("identification_types.json");

        try {
            Type listType = new TypeToken<List<IdentificationType>>() {
            }.getType();
            identificationTypesList = JsonUtil.getInstance().getGson().fromJson(json, listType);
        } catch (Exception ex) {
            identificationTypesList = null;
        }
        return identificationTypesList;
    }

    public static List<IdentificationType> getEnabledMLBIdentificationTypes() {
        List<IdentificationType> identificationTypesList;
        String json = ResourcesUtil.getStringResource("identification_types_MLB.json");

        try {
            Type listType = new TypeToken<List<IdentificationType>>() {
            }.getType();
            identificationTypesList = JsonUtil.getInstance().getGson().fromJson(json, listType);
        } catch (Exception ex) {
            identificationTypesList = null;
        }
        return identificationTypesList;
    }

    public static IdentificationType getIdentificationTypeCPF() {
        IdentificationType identificationType = new IdentificationType();
        identificationType.setType(TYPE);
        identificationType.setId(CPF_ID);
        identificationType.setMaxLength(CPF_VALID_LENGTH);
        identificationType.setMinLength(CPF_VALID_LENGTH);
        identificationType.setName(CPF_NAME);

        return identificationType;
    }

    public static IdentificationType getIdentificationTypeWithInvalidLengthCPF() {
        IdentificationType identificationType = new IdentificationType();
        identificationType.setType(TYPE);
        identificationType.setId(CPF_ID);
        identificationType.setMaxLength(CPF_INVALID_LENGTH);
        identificationType.setMinLength(CPF_INVALID_LENGTH);
        identificationType.setName(CPF_NAME);

        return identificationType;
    }

    public static IdentificationType getIdentificationTypeCNPJ() {
        IdentificationType identificationType = new IdentificationType();
        identificationType.setType(TYPE);
        identificationType.setId(CNPJ_ID);
        identificationType.setMaxLength(CNPJ_VALID_LENGTH);
        identificationType.setMinLength(CNPJ_VALID_LENGTH);
        identificationType.setName(CNPJ_NAME);

        return identificationType;
    }
}
