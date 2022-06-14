package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.SponsorFeeTransaction;
import com.wavesplatform.transactions.TransferTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import im.mak.paddle.exceptions.ApiError;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;

public class SponsorFeeTransactionSender extends BaseTransactionSender {
    private static long assetOwnerAssetBalanceAfterTransaction;
    private static long fromAssetBalanceAfterTransaction;
    private static long toAssetBalanceAfterTransaction;
    private static long assetOwnerWavesBalance;
    private static long fromWavesBalance;
    private static long toWavesBalance;
    private static SponsorFeeTransaction sponsorTx;
    private static TransactionInfo sponsorTxInfo;
    private static TransferTransaction transferTx;
    private static TransactionInfo transferTxInfo;

    public static void sponsorFeeTransactionSender(Account assetOwner, Account sender, Account recipient,
                                                   long fee, long transferSum, AssetId assetId, int version) {
        assetOwnerAssetBalanceAfterTransaction = assetOwner.getBalance(assetId) + fee;
        fromAssetBalanceAfterTransaction = sender.getBalance(assetId) - fee - transferSum;
        toAssetBalanceAfterTransaction = recipient.getAssetBalance(assetId) + transferSum;

        assetOwnerWavesBalance = assetOwner.getWavesBalance() - MIN_FEE - MIN_FEE;
        fromWavesBalance = sender.getWavesBalance();
        toWavesBalance = recipient.getWavesBalance();

        sponsorTx = SponsorFeeTransaction.builder(assetId, fee).version(version)
                .getSignedWith(assetOwner.privateKey());
        node().waitForTransaction(node().broadcast(sponsorTx).id());
        sponsorTxInfo = node().getTransactionInfo(sponsorTx.id());

        transferTx = sender.transfer(
                recipient.address(), transferSum, assetId, i -> i.feeAssetId(assetId)
        ).tx();
        transferTxInfo = node().getTransactionInfo(transferTx.id());
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

    public static long getAssetOwnerAssetBalanceAfterTransaction() {
        return assetOwnerAssetBalanceAfterTransaction;
    }

    public static long getFromAssetBalanceAfterTransaction() {
        return fromAssetBalanceAfterTransaction;
    }

    public static long getToAssetBalanceAfterTransaction() {
        return toAssetBalanceAfterTransaction;
    }

    public static long getAssetOwnerWavesBalance() {
        return assetOwnerWavesBalance;
    }

    public static long getFromWavesBalance() {
        return fromWavesBalance;
    }

    public static long getToWavesBalance() {
        return toWavesBalance;
    }

    public static SponsorFeeTransaction getSponsorTx() {
        return sponsorTx;
    }

    public static TransactionInfo getSponsorTxInfo() {
        return sponsorTxInfo;
    }

    public static TransferTransaction getTransferTx() {
        return transferTx;
    }

    public static TransactionInfo getTransferTxInfo() {
        return transferTxInfo;
    }

}
