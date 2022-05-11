package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.SponsorFeeTransaction;
import com.wavesplatform.transactions.TransferTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import im.mak.paddle.exceptions.ApiError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.DEFAULT_FAUCET;
import static im.mak.paddle.util.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SponsorFeeTransactionTest {
    private static Account alice;
    private static Account bob;
    private static Account acc;

    private static AssetId aliceAssetId;
    private static AssetId bobAssetId;

    @BeforeAll
    static void before() {
        async(
                () -> {
                    alice = new Account(DEFAULT_FAUCET);
                    aliceAssetId = alice.issue(i -> i.name("Test_Asset").quantity(1000L).decimals(8)).tx().assetId();
                },
                () -> {
                    bob = new Account(DEFAULT_FAUCET);
                    bobAssetId = bob.issue(i -> i.name("Bob_Asset").quantity(1000L).decimals(8)).tx().assetId();
                },
                () -> acc = new Account()
        );
        alice.transfer(bob, alice.getBalance(aliceAssetId) / 2, aliceAssetId);
        bob.transfer(acc, bob.getBalance(bobAssetId) / 2, bobAssetId);
    }

    @Test
    @DisplayName("Sponsor transaction with minimal sponsored fee")
    void sponsorMinAssets() {
        sponsorTransaction(alice, bob, acc, 1, 1, aliceAssetId);
    }

    @Test
    @DisplayName("Sponsor transaction with maximum sponsored fee")
    void sponsorMaxAssets() {
        long fee = getRandomInt(10, 50);
        long transferSum = acc.getBalance(bobAssetId) - fee;
        sponsorTransaction(bob, acc, alice, fee, transferSum, bobAssetId);
    }

    @Test
    @DisplayName("Cancel sponsored fee")
    void cancelAliceSponsorFee() {
        transactionCancelSponsorFee(alice, bob, acc, aliceAssetId);
    }

    private void sponsorTransaction
            (Account assetOwner, Account sender, Account recipient, long fee, long transferSum, AssetId assetId) {
        long assetOwnerAssetBalanceAfterTransaction = assetOwner.getBalance(assetId) + fee;
        long fromAssetBalanceAfterTransaction = sender.getBalance(assetId) - fee - transferSum;
        long toAssetBalanceAfterTransaction = recipient.getAssetBalance(assetId) + transferSum;

        long assetOwnerWavesBalance = assetOwner.getWavesBalance() - MIN_FEE - MIN_FEE;
        long fromWavesBalance = sender.getWavesBalance();
        long toWavesBalance = recipient.getWavesBalance();

        SponsorFeeTransaction sponsorTx = SponsorFeeTransaction.builder(assetId, fee)
                .getSignedWith(assetOwner.privateKey());
        node().waitForTransaction(node().broadcast(sponsorTx).id());
        TransactionInfo sponsorTxInfo = node().getTransactionInfo(sponsorTx.id());

        TransferTransaction transferTx = sender.transfer(
                recipient.address(), transferSum, assetId, i -> i.feeAssetId(assetId)
        ).tx();
        TransactionInfo transferTxInfo = node().getTransactionInfo(transferTx.id());

        assertAll(
                () -> assertThat(sponsorTxInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(sponsorTx.sender()).isEqualTo(assetOwner.publicKey()),
                () -> assertThat(sponsorTx.assetId()).isEqualTo(assetId),
                () -> assertThat(sponsorTx.minSponsoredFee()).isEqualTo(fee),
                () -> assertThat(sponsorTx.type()).isEqualTo(14),

                () -> assertThat(transferTxInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(transferTx.sender()).isEqualTo(sender.publicKey()),
                () -> assertThat(transferTx.fee().assetId()).isEqualTo(assetId),
                () -> assertThat(transferTx.fee().value()).isEqualTo(fee),

                () -> assertThat(assetOwner.getBalance(assetId)).isEqualTo(assetOwnerAssetBalanceAfterTransaction),
                () -> assertThat(sender.getBalance(assetId)).isEqualTo(fromAssetBalanceAfterTransaction),
                () -> assertThat(recipient.getBalance(assetId)).isEqualTo(toAssetBalanceAfterTransaction),

                () -> assertThat(assetOwner.getWavesBalance()).isEqualTo(assetOwnerWavesBalance),
                () -> assertThat(sender.getWavesBalance()).isEqualTo(fromWavesBalance),
                () -> assertThat(recipient.getWavesBalance()).isEqualTo(toWavesBalance)
        );
    }

    private void transactionCancelSponsorFee(Account assetOwner, Account sender, Account recipient, AssetId assetId) {
        SponsorFeeTransaction sponsorTx = assetOwner.sponsorFee(assetId, 0).tx();
        TransactionInfo sponsorTxInfo = node().getTransactionInfo(sponsorTx.id());

        try {
            sender.transfer(recipient.address(), 1, assetId, i -> i.feeAssetId(assetId));
        } catch (ApiError e) {
            assertThat(e.getMessage()).isEqualTo("insufficient fee");
        }

        assertAll(
                () -> assertThat(sponsorTxInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(sponsorTx.sender()).isEqualTo(assetOwner.publicKey()),
                () -> assertThat(sponsorTx.assetId()).isEqualTo(assetId),
                () -> assertThat(sponsorTx.minSponsoredFee()).isEqualTo(0),
                () -> assertThat(sponsorTx.type()).isEqualTo(14)
        );
    }
}
