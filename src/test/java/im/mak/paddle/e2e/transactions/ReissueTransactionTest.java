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
    private static Account account;
    private static long accountWavesBalance;
    private static AssetId issuedAssetId;
    private static AssetId issuedSmartAssetId;
    long feeForSmartAssetReissue = MIN_FEE + EXTRA_FEE;

    @BeforeAll
    static void before() {
        account = new Account(DEFAULT_FAUCET);
        async(
                () -> account.createAlias(randomNumAndLetterString(15)),
                () -> issuedAssetId = account.issue(i -> i.name("Test_Asset").quantity(1000)).tx().assetId(),
                () -> issuedSmartAssetId =
                        account.issue(i -> i.name("T_Smart_Asset").quantity(1000).script("2 * 2 == 4")).tx().assetId()
        );
    }

    @Test
    @DisplayName("reissue minimum quantity asset")
    void reissueMinimumAssets() {
        reissueTransaction(ASSET_QUANTITY_MINIMUM, issuedAssetId, MIN_FEE);
    }

    @Test
    @DisplayName("reissue maximum quantity asset")
    void reissueAlmostMaximumAssets() {
        long reissueSum = ASSET_QUANTITY_MAXIMUM - account.getAssetBalance(issuedAssetId) - ASSET_QUANTITY_MINIMUM;
        reissueTransaction(reissueSum, issuedAssetId, MIN_FEE);
    }

    @Test
    @DisplayName("reissue minimum quantity smart asset")
    void reissueMinimumSmartAssets() {
        reissueTransaction(ASSET_QUANTITY_MINIMUM, issuedSmartAssetId, feeForSmartAssetReissue);
    }

    @Test
    @DisplayName("reissue maximum quantity smart asset")
    void reissueAlmostMaximumSmartAssets() {
        long reissueSum = ASSET_QUANTITY_MAXIMUM - account.getAssetBalance(issuedSmartAssetId) - ASSET_QUANTITY_MINIMUM;
        reissueTransaction(reissueSum, issuedSmartAssetId, feeForSmartAssetReissue);
    }

    private void reissueTransaction(long amount, AssetId assetId, long fee) {
        accountWavesBalance = account.getBalance(AssetId.WAVES);
        long balanceAfterReissue = account.getBalance(assetId) + amount;
        ReissueTransaction tx = account.reissue(amount, assetId, i -> i.reissuable(true)).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getAssetBalance(assetId)).isEqualTo(balanceAfterReissue),
                () -> assertThat(account.getWavesBalance()).isEqualTo(accountWavesBalance - fee),
                () -> assertThat(tx.fee().value()).isEqualTo(fee),
                () -> assertThat(tx.sender()).isEqualTo(account.publicKey()),
                () -> assertThat(tx.reissuable()).isEqualTo(true),
                () -> assertThat(tx.amount().value()).isEqualTo(amount),
                () -> assertThat(tx.amount().assetId()).isEqualTo(assetId),
                () -> assertThat(tx.type()).isEqualTo(5)
        );
    }
}
