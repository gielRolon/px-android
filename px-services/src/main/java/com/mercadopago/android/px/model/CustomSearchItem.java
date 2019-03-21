package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomSearchItem implements Serializable, Parcelable {

    private static final Comparator<CustomSearchItem> AVAILABILITY_ORDER = (o1, o2) -> {
        if (o1.isDisabled() && !o2.isDisabled()) {
            return 1;
        } else if (!o1.isDisabled() && o2.isDisabled()) {
            return -1;
        }
        return 0;
    };

    //TODO make final when deprecate custom search item constructor.
    private String description;
    private String id;
    private String comment;

    //TODO change name / serialize name - signature V5
    @SerializedName("payment_type_id")
    private String type;
    private String paymentMethodId;

    @Nullable private String discountInfo;

    private String defaultAmountConfiguration;
    private Map<String, AmountConfiguration> amountConfigurations;

    private boolean disabled;

    public static void orderListByAvailability(@NonNull final List<CustomSearchItem> items){
        Collections.sort(items, AVAILABILITY_ORDER);
    }

    @Deprecated
    public CustomSearchItem() {
        amountConfigurations = new HashMap<>();
    }

    protected CustomSearchItem(final Parcel in) {
        description = in.readString();
        id = in.readString();
        type = in.readString();
        comment = in.readString();
        paymentMethodId = in.readString();
        discountInfo = in.readString();
        defaultAmountConfiguration = in.readString();
        amountConfigurations = new HashMap<>();
        in.readMap(amountConfigurations, CustomSearchItem.class.getClassLoader());
        disabled = in.readByte() == 1;
    }

    public static final Creator<CustomSearchItem> CREATOR = new Creator<CustomSearchItem>() {
        @Override
        public CustomSearchItem createFromParcel(final Parcel in) {
            return new CustomSearchItem(in);
        }

        @Override
        public CustomSearchItem[] newArray(final int size) {
            return new CustomSearchItem[size];
        }
    };

    public String getDescription() {
        return description;
    }

    @Nullable
    public String getDiscountInfo() {
        return discountInfo;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getComment() {
        return comment;
    }

    public String getDefaultAmountConfiguration() {
        return defaultAmountConfiguration;
    }

    public AmountConfiguration getAmountConfiguration(final String key) {
        return amountConfigurations.get(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(description);
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(paymentMethodId);
        dest.writeString(comment);
        dest.writeString(discountInfo);
        dest.writeString(defaultAmountConfiguration);
        dest.writeMap(amountConfigurations);
        dest.writeByte((byte) (disabled ? 1 : 0));
    }

    @Deprecated
    public void setDescription(final String description) {
        this.description = description;
    }

    @Deprecated
    public void setId(final String id) {
        this.id = id;
    }

    @Deprecated
    public void setType(final String type) {
        this.type = type;
    }

    @Deprecated
    public void setPaymentMethodId(final String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public boolean hasDiscountInfo() {
        return !TextUtils.isEmpty(discountInfo);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }
}