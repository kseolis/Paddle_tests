
package im.mak.paddle.blockchain_updates.serialized_json;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Append {

    @SerializedName("block")
    @Expose
    private Block block;
    @SerializedName("state_update")
    @Expose
    private StateUpdate stateUpdate;

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public StateUpdate getStateUpdate() {
        return stateUpdate;
    }

    public void setStateUpdate(StateUpdate stateUpdate) {
        this.stateUpdate = stateUpdate;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.block == null)? 0 :this.block.hashCode()));
        result = ((result* 31)+((this.stateUpdate == null)? 0 :this.stateUpdate.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Append) == false) {
            return false;
        }
        Append rhs = ((Append) other);
        return (((this.block == rhs.block)||((this.block!= null)&&this.block.equals(rhs.block)))&&((this.stateUpdate == rhs.stateUpdate)||((this.stateUpdate!= null)&&this.stateUpdate.equals(rhs.stateUpdate))));
    }

}