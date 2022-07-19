package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.SponsorFeeTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import im.mak.paddle.exceptions.ApiError;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;

public class SponsorFeeTransactionSender extends BaseTransactionSender {
    private static long assetOwnerWavesBalance;
    private static SponsorFeeTransaction sponsorTx;
    private static TransactionInfo sponsorTxInfo;

    public static void sponsorFeeTransactionSender(Account account, long sponsorFee, AssetId assetId, long fee, int version) {
        assetOwnerWavesBalance = account.getWavesBalance() - MIN_FEE - fee;

        sponsorTx = SponsorFeeTransaction.builder(assetId, sponsorFee)
                .version(version)
                .fee(fee)
                .getSignedWith(account.privateKey());
        node().waitForTransaction(node().broadcast(sponsorTx).id());
        sponsorTxInfo = node().getTransactionInfo(sponsorTx.id());
    }

    public static void cancelSponsorFeeSender
            (Account assetOwner, Account sender, Account recipient, AssetId assetId, int version) {


        sponsorTx = SponsorFeeTransaction.builder(assetId, 0).version(version)
                .getSignedWith(assetOwner.privateKey());
        node().waitForTransaction(node().broadcast(sponsorTx).id());

        sponsorTxInfo = node().getTransactionInfo(sponsorTx.id());

        try {
            sender.transfer(recipient.address(), 1, assetId, i -> i.feeAssetId(assetId));
        } catch (ApiError e) {
            assertThat(e.getMessage()).isEqualTo("insufficient fee");
        }
    }

    public static long getAssetOwnerWavesBalance() {
        return assetOwnerWavesBalance;
    }

    public static SponsorFeeTransaction getSponsorTx() {
        return sponsorTx;
    }

    public static TransactionInfo getSponsorTxInfo() {
        return sponsorTxInfo;
    }
}
