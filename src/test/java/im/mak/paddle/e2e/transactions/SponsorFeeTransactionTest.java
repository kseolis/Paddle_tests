package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.SponsorFeeTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.transaction_senders.SponsorFeeTransactionSender.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.DEFAULT_FAUCET;
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
        for (int v = 1; v <= LATEST_VERSION; v++) {
            sponsorFeeTransactionSender(alice, bob, acc, 1, 1, aliceAssetId, v);
            checkSponsorTransaction(alice, bob, acc, 1, aliceAssetId);
        }
    }

    @Test
    @DisplayName("Sponsor transaction with maximum sponsored fee")
    void sponsorMaxAssets() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            long fee = getRandomInt(10, 50);
            long transferSum = acc.getBalance(bobAssetId) - fee;
            sponsorFeeTransactionSender(bob, acc, alice, fee, transferSum, bobAssetId, v);
            checkSponsorTransaction(bob, acc, alice, fee, bobAssetId);
            bob.reissue(500, bobAssetId);
            bob.transfer(acc, bob.getBalance(bobAssetId) / 2, bobAssetId);
        }
    }

    @Test
    @DisplayName("Cancel sponsored fee")
    void cancelAliceSponsorFee() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            cancelSponsorFeeSender(alice, bob, acc, aliceAssetId, v);
            checkCancelSponsorFee(alice, aliceAssetId);
        }
    }

    private void checkSponsorTransaction(Account assetOwner, Account sender, Account recipient, long fee, AssetId assetId) {
        assertAll(
                () -> assertThat(getSponsorTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(getSponsorTx().sender()).isEqualTo(assetOwner.publicKey()),
                () -> assertThat(getSponsorTx().assetId()).isEqualTo(assetId),
                () -> assertThat(getSponsorTx().minSponsoredFee()).isEqualTo(fee),
                () -> assertThat(getSponsorTx().type()).isEqualTo(14),

                () -> assertThat(getTransferTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(getTransferTx().sender()).isEqualTo(sender.publicKey()),
                () -> assertThat(getTransferTx().fee().assetId()).isEqualTo(assetId),
                () -> assertThat(getTransferTx().fee().value()).isEqualTo(fee),

                () -> assertThat(assetOwner.getBalance(assetId)).isEqualTo(getAssetOwnerAssetBalanceAfterTransaction()),
                () -> assertThat(sender.getBalance(assetId)).isEqualTo(getFromAssetBalanceAfterTransaction()),
                () -> assertThat(recipient.getBalance(assetId)).isEqualTo(getToAssetBalanceAfterTransaction()),

                () -> assertThat(assetOwner.getWavesBalance()).isEqualTo(getAssetOwnerWavesBalance()),
                () -> assertThat(sender.getWavesBalance()).isEqualTo(getFromWavesBalance()),
                () -> assertThat(recipient.getWavesBalance()).isEqualTo(getToWavesBalance())
        );
    }

    private void checkCancelSponsorFee(Account assetOwner, AssetId assetId) {
        assertAll(
                () -> assertThat(getSponsorTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(getSponsorTx().sender()).isEqualTo(assetOwner.publicKey()),
                () -> assertThat(getSponsorTx().assetId()).isEqualTo(assetId),
                () -> assertThat(getSponsorTx().minSponsoredFee()).isEqualTo(0),
                () -> assertThat(getSponsorTx().type()).isEqualTo(14)
        );
    }
}
