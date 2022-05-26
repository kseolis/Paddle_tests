package im.mak.paddle.blockchain_updates.serialized_json;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Update {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("height")
    @Expose
    private Long height;
    @SerializedName("append")
    @Expose
    private Append append;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Append getAppend() {
        return append;
    }

    public void setAppend(Append append) {
        this.append = append;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.append == null)? 0 :this.append.hashCode()));
        result = ((result* 31)+((this.height == null)? 0 :this.height.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Update) == false) {
            return false;
        }
        Update rhs = ((Update) other);
        return ((((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id)))&&((this.append == rhs.append)||((this.append!= null)&&this.append.equals(rhs.append))))&&((this.height == rhs.height)||((this.height!= null)&&this.height.equals(rhs.height))));
    }

}