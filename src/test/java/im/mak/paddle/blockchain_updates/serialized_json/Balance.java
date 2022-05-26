package im.mak.paddle.blockchain_updates.serialized_json;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Balance {

    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("amount_after")
    @Expose
    private AmountAfter amountAfter;
    @SerializedName("amount_before")
    @Expose
    private Long amountBefore;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public AmountAfter getAmountAfter() {
        return amountAfter;
    }

    public void setAmountAfter(AmountAfter amountAfter) {
        this.amountAfter = amountAfter;
    }

    public Long getAmountBefore() {
        return amountBefore;
    }

    public void setAmountBefore(Long amountBefore) {
        this.amountBefore = amountBefore;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.amountAfter == null)? 0 :this.amountAfter.hashCode()));
        result = ((result* 31)+((this.address == null)? 0 :this.address.hashCode()));
        result = ((result* 31)+((this.amountBefore == null)? 0 :this.amountBefore.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Balance) == false) {
            return false;
        }
        Balance rhs = ((Balance) other);
        return ((((this.amountAfter == rhs.amountAfter)||((this.amountAfter!= null)&&this.amountAfter.equals(rhs.amountAfter)))&&((this.address == rhs.address)||((this.address!= null)&&this.address.equals(rhs.address))))&&((this.amountBefore == rhs.amountBefore)||((this.amountBefore!= null)&&this.amountBefore.equals(rhs.amountBefore))));
    }

}