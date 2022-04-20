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
    private static Account marrie;
    private static long marrieWavesBalance;
    private static AssetId issuedAssetId;
    private static AssetId issuedSmartAssetId;
    long feeForSmartAssetReissue = MIN_FEE + EXTRA_FEE;

    @BeforeAll
    static void before() {
        marrie = new Account(DEFAULT_FAUCET);
        async(
            () -> marrie.createAlias(randomNumAndLetterString(15)),
            () -> issuedAssetId = marrie.issue(i -> i.name("Test_Asset").quantity(1000)).tx().assetId(),
            () -> issuedSmartAssetId =
                    marrie.issue(i -> i.name("Test_Asset").quantity(1000).script("2 * 2 == 4")).tx().assetId()
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
        long reissueSum = ASSET_QUANTITY_MAXIMUM - marrie.getAssetBalance(issuedAssetId) - ASSET_QUANTITY_MINIMUM;
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
        long reissueSum = ASSET_QUANTITY_MAXIMUM - marrie.getAssetBalance(issuedSmartAssetId) - ASSET_QUANTITY_MINIMUM;
        reissueTransaction(reissueSum, issuedSmartAssetId, feeForSmartAssetReissue);
    }

    private void reissueTransaction(long amount, AssetId assetId, long fee) {
        marrieWavesBalance = marrie.getBalance(AssetId.WAVES);
        long balanceAfterReissue = marrie.getBalance(assetId) + amount;
        ReissueTransaction tx = marrie.reissue(amount, assetId, i -> i.reissuable(true)).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(marrie.getAssetBalance(assetId)).isEqualTo(balanceAfterReissue),
                () -> assertThat(marrie.getWavesBalance()).isEqualTo(marrieWavesBalance - fee),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(fee)
        );
    }
}
