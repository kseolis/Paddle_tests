package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.dapps.DefaultDApp420Complexity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.SponsorFeeTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.transaction_senders.SponsorFeeTransactionSender.*;
import static im.mak.paddle.helpers.transaction_senders.TransferTransactionSender.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SponsorFeeTransactionTest {
    private static Account alice;
    private static DefaultDApp420Complexity dAppAcc;
    private static Account acc;

    private static AssetId aliceAssetId;
    private static AssetId dAppAssetId;

    @BeforeAll
    static void before() {
        async(
                () -> {
                    alice = new Account(DEFAULT_FAUCET);
                    aliceAssetId = alice.issue(i -> i.name("Alice_Asset").quantity(1000L).decimals(8)).tx().assetId();
                },
                () -> {
                    dAppAcc = new DefaultDApp420Complexity(DEFAULT_FAUCET);
                    dAppAssetId = dAppAcc.issue(i -> i.name("Bob_Asset").quantity(1000L).decimals(8)).tx().assetId();
                },
                () -> acc = new Account(DEFAULT_FAUCET)
        );
        alice.transfer(dAppAcc, alice.getBalance(aliceAssetId) / 2, aliceAssetId);
        dAppAcc.transfer(acc, dAppAcc.getBalance(dAppAssetId) / 2, dAppAssetId);
    }

    @Test
    @DisplayName("Sponsor transaction with minimal sponsored fee")
    void sponsorMinAssets() {
        long sponsorFee = 1;
        for (int v = 1; v <= LATEST_VERSION; v++) {
            long amountValue = dAppAcc.getBalance(aliceAssetId) / 2;

            Amount amount = Amount.of(amountValue, aliceAssetId);

            sponsorFeeTransactionSender(alice, sponsorFee, aliceAssetId, MIN_FEE, v);
            transferTransactionSender(amount, dAppAcc, acc, ADDRESS, SUM_FEE, 2);

            checkSponsorTransaction(alice, dAppAcc, acc, sponsorFee, MIN_FEE, aliceAssetId);
        }
    }

    @Test
    @DisplayName("Sponsor transaction with dApp account fee")
    void sponsorDAppAccAssets() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            long sponsorFee = getRandomInt(10, 50);
            long amountValue = acc.getBalance(dAppAssetId) - sponsorFee;
            Amount amount = Amount.of(amountValue, dAppAssetId);

            sponsorFeeTransactionSender(dAppAcc, sponsorFee, dAppAssetId, SUM_FEE, v);
            transferTransactionSender(amount, acc, alice, ADDRESS, SUM_FEE, v);
            checkSponsorTransaction(dAppAcc, acc, alice, sponsorFee, SUM_FEE, dAppAssetId);

            dAppAcc.reissue(500, dAppAssetId);
            dAppAcc.transfer(acc, dAppAcc.getBalance(dAppAssetId) / 2, dAppAssetId);
        }
    }

    @Test
    @DisplayName("Cancel sponsored fee")
    void cancelAliceSponsorFee() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            cancelSponsorFeeSender(alice, dAppAcc, acc, aliceAssetId, v);
            checkCancelSponsorFee(alice, aliceAssetId);
        }
    }

    private void checkSponsorTransaction
            (Account assetCreator, Account sender, Account recipient, long sponsorFee, long fee, AssetId assetId) {
        assertAll(
                () -> assertThat(getSponsorTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(getSponsorTx().sender()).isEqualTo(assetCreator.publicKey()),
                () -> assertThat(getSponsorTx().assetId()).isEqualTo(assetId),
                () -> assertThat(getSponsorTx().fee().value()).isEqualTo(fee),
                () -> assertThat(getSponsorTx().minSponsoredFee()).isEqualTo(sponsorFee),
                () -> assertThat(getSponsorTx().type()).isEqualTo(14),

                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(getTransferTx().sender()).isEqualTo(sender.publicKey()),
                () -> assertThat(getTransferTx().fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(getTransferTx().fee().value()).isEqualTo(SUM_FEE),

                () -> assertThat(sender.getBalance(assetId)).isEqualTo(getSenderBalanceAfterTransaction()),
                () -> assertThat(recipient.getBalance(assetId)).isEqualTo(getRecipientBalanceAfterTransaction()),

                () -> assertThat(sender.getWavesBalance()).isEqualTo(getSenderWavesBalanceAfterTransaction()),
                () -> assertThat(recipient.getWavesBalance()).isEqualTo(getRecipientWavesBalanceAfterTransaction())
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
