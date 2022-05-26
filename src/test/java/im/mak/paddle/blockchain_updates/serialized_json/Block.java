package im.mak.paddle.blockchain_updates.serialized_json;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Block {

    @SerializedName("block")
    @Expose
    private Block__1 block;
    @SerializedName("updated_waves_amount")
    @Expose
    private Long updatedWavesAmount;

    public Block__1 getBlock() {
        return block;
    }

    public void setBlock(Block__1 block) {
        this.block = block;
    }

    public Long getUpdatedWavesAmount() {
        return updatedWavesAmount;
    }

    public void setUpdatedWavesAmount(Long updatedWavesAmount) {
        this.updatedWavesAmount = updatedWavesAmount;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.updatedWavesAmount == null)? 0 :this.updatedWavesAmount.hashCode()));
        result = ((result* 31)+((this.block == null)? 0 :this.block.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Block) == false) {
            return false;
        }
        Block rhs = ((Block) other);
        return (((this.updatedWavesAmount == rhs.updatedWavesAmount)||((this.updatedWavesAmount!= null)&&this.updatedWavesAmount.equals(rhs.updatedWavesAmount)))&&((this.block == rhs.block)||((this.block!= null)&&this.block.equals(rhs.block))));
    }

}