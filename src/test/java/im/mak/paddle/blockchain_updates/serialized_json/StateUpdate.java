
package im.mak.paddle.blockchain_updates.serialized_json;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class StateUpdate {

    @SerializedName("balances")
    @Expose
    private List<Balance> balances = null;

    public List<Balance> getBalances() {
        return balances;
    }

    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.balances == null)? 0 :this.balances.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StateUpdate) == false) {
            return false;
        }
        StateUpdate rhs = ((StateUpdate) other);
        return ((this.balances == rhs.balances)||((this.balances!= null)&&this.balances.equals(rhs.balances)));
    }

}