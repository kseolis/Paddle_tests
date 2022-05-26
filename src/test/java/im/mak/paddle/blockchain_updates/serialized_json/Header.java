package im.mak.paddle.blockchain_updates.serialized_json;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Header {

    @SerializedName("chain_id")
    @Expose
    private Long chainId;
    @SerializedName("reference")
    @Expose
    private String reference;
    @SerializedName("base_target")
    @Expose
    private Long baseTarget;
    @SerializedName("generation_signature")
    @Expose
    private String generationSignature;
    @SerializedName("timestamp")
    @Expose
    private Long timestamp;
    @SerializedName("version")
    @Expose
    private Long version;
    @SerializedName("generator")
    @Expose
    private String generator;
    @SerializedName("reward_vote")
    @Expose
    private Long rewardVote;
    @SerializedName("transactions_root")
    @Expose
    private String transactionsRoot;

    public Long getChainId() {
        return chainId;
    }

    public void setChainId(Long chainId) {
        this.chainId = chainId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Long getBaseTarget() {
        return baseTarget;
    }

    public void setBaseTarget(Long baseTarget) {
        this.baseTarget = baseTarget;
    }

    public String getGenerationSignature() {
        return generationSignature;
    }

    public void setGenerationSignature(String generationSignature) {
        this.generationSignature = generationSignature;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public Long getRewardVote() {
        return rewardVote;
    }

    public void setRewardVote(Long rewardVote) {
        this.rewardVote = rewardVote;
    }

    public String getTransactionsRoot() {
        return transactionsRoot;
    }

    public void setTransactionsRoot(String transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.reference == null)? 0 :this.reference.hashCode()));
        result = ((result* 31)+((this.chainId == null)? 0 :this.chainId.hashCode()));
        result = ((result* 31)+((this.transactionsRoot == null)? 0 :this.transactionsRoot.hashCode()));
        result = ((result* 31)+((this.generationSignature == null)? 0 :this.generationSignature.hashCode()));
        result = ((result* 31)+((this.rewardVote == null)? 0 :this.rewardVote.hashCode()));
        result = ((result* 31)+((this.baseTarget == null)? 0 :this.baseTarget.hashCode()));
        result = ((result* 31)+((this.generator == null)? 0 :this.generator.hashCode()));
        result = ((result* 31)+((this.version == null)? 0 :this.version.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Header) == false) {
            return false;
        }
        Header rhs = ((Header) other);
        return ((((((((((this.reference == rhs.reference)||((this.reference!= null)&&this.reference.equals(rhs.reference)))&&((this.chainId == rhs.chainId)||((this.chainId!= null)&&this.chainId.equals(rhs.chainId))))&&((this.transactionsRoot == rhs.transactionsRoot)||((this.transactionsRoot!= null)&&this.transactionsRoot.equals(rhs.transactionsRoot))))&&((this.generationSignature == rhs.generationSignature)||((this.generationSignature!= null)&&this.generationSignature.equals(rhs.generationSignature))))&&((this.rewardVote == rhs.rewardVote)||((this.rewardVote!= null)&&this.rewardVote.equals(rhs.rewardVote))))&&((this.baseTarget == rhs.baseTarget)||((this.baseTarget!= null)&&this.baseTarget.equals(rhs.baseTarget))))&&((this.generator == rhs.generator)||((this.generator!= null)&&this.generator.equals(rhs.generator))))&&((this.version == rhs.version)||((this.version!= null)&&this.version.equals(rhs.version))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}