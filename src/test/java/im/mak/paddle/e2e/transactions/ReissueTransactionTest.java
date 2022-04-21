package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.ReissueTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ReissueTransactionTest {
    private static Account alice;
    private static long aliceWavesBalance;
    private static AssetId issuedAssetId;
    private static AssetId issuedSmartAssetId;
    long feeForSmartAssetReissue = MIN_FEE + EXTRA_FEE;

    @BeforeAll
    static void before() {
        alice = new Account(DEFAULT_FAUCET);
        async(
            () -> alice.createAlias(randomNumAndLetterString(15)),
            () -> issuedAssetId = alice.issue(i -> i.name("Test_Asset").quantity(1000)).tx().assetId(),
            () -> issuedSmartAssetId =
                    alice.issue(i -> i.name("Test_Asset").quantity(1000).script("2 * 2 == 4")).tx().assetId()
        );
    }

    @Test
    @DisplayName("test reissue minimum quantity asset")
    void reissueMinimumAssets() {
        reissueTransaction(ASSET_QUANTITY_MINIMUM, issuedAssetId, MIN_FEE);
    }

    @Test
    @DisplayName("test reissue maximum quantity asset")
    void reissueAlmostMaximumAssets() {
        long reissueSum = ASSET_QUANTITY_MAXIMUM - alice.getAssetBalance(issuedAssetId) - ASSET_QUANTITY_MINIMUM;
        reissueTransaction(reissueSum, issuedAssetId, MIN_FEE);
    }

    @Test
    @DisplayName("test reissue minimum quantity smart asset")
    void reissueMinimumSmartAssets() {
        reissueTransaction(ASSET_QUANTITY_MINIMUM, issuedSmartAssetId, feeForSmartAssetReissue);
    }

    @Test
    @DisplayName("test reissue maximum quantity smart asset")
    void reissueAlmostMaximumSmartAssets() {
        long reissueSum = ASSET_QUANTITY_MAXIMUM - alice.getAssetBalance(issuedSmartAssetId) - ASSET_QUANTITY_MINIMUM;
        reissueTransaction(reissueSum, issuedSmartAssetId, feeForSmartAssetReissue);
    }

    private void reissueTransaction(long amount, AssetId assetId, long fee) {
        aliceWavesBalance = alice.getBalance(AssetId.WAVES);
        long balanceAfterReissue = alice.getBalance(assetId) + amount;
        ReissueTransaction tx = alice.reissue(amount, assetId, i -> i.reissuable(true)).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(alice.getAssetBalance(assetId)).isEqualTo(balanceAfterReissue),
                () -> assertThat(alice.getWavesBalance()).isEqualTo(aliceWavesBalance - fee),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.sender()).isEqualTo(alice.publicKey()),
                () -> assertThat(tx.reissuable()).isEqualTo(true),
                () -> assertThat(tx.type()).isEqualTo(5),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(fee)
        );
    }
}
