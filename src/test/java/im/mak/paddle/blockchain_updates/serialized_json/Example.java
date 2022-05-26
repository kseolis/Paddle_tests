package im.mak.paddle.blockchain_updates.serialized_json;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Example {

    @SerializedName("update")
    @Expose
    private Update update;

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.update == null)? 0 :this.update.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Example) == false) {
            return false;
        }
        Example rhs = ((Example) other);
        return ((this.update == rhs.update)||((this.update!= null)&&this.update.equals(rhs.update)));
    }

}