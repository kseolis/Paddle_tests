package im.mak.paddle.blockchain_updates.serialized_json;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Block__1 {

    @SerializedName("header")
    @Expose
    private Header header;
    @SerializedName("signature")
    @Expose
    private String signature;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.header == null)? 0 :this.header.hashCode()));
        result = ((result* 31)+((this.signature == null)? 0 :this.signature.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Block__1) == false) {
            return false;
        }
        Block__1 rhs = ((Block__1) other);
        return (((this.header == rhs.header)||((this.header!= null)&&this.header.equals(rhs.header)))&&((this.signature == rhs.signature)||((this.signature!= null)&&this.signature.equals(rhs.signature))));
    }

}