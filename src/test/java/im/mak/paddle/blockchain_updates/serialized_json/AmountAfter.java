
package im.mak.paddle.blockchain_updates.serialized_json;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class AmountAfter {

    @SerializedName("amount")
    @Expose
    private Long amount;

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.amount == null)? 0 :this.amount.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AmountAfter) == false) {
            return false;
        }
        AmountAfter rhs = ((AmountAfter) other);
        return ((this.amount == rhs.amount)||((this.amount!= null)&&this.amount.equals(rhs.amount)));
    }

}