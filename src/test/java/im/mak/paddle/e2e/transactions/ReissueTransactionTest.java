package im.mak.paddle.e2e.transactions;

import static com.wavesplatform.transactions.ReissueTransaction.LATEST_VERSION;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getBalanceAfterTransaction;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.ReissueTransactionSender.getReissueTx;
import static im.mak.paddle.helpers.transaction_senders.ReissueTransactionSender.reissueTransactionSender;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ReissueTransactionTest {
    private static Account account;
    private static long accountWavesBalance;
    private static AssetId issuedAssetId;
    private static AssetId issuedSmartAssetId;

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
        Amount amount = Amount.of(ASSET_QUANTITY_MIN, issuedAssetId);
        for (int v = 1; v <= LATEST_VERSION; v++) {
            accountWavesBalance = account.getBalance(AssetId.WAVES);
            reissueTransactionSender(account, amount, issuedAssetId, MIN_FEE, v);
            checkReissueTransaction(amount, issuedAssetId, MIN_FEE, v);
        }
    }

    @Test
    @DisplayName("reissue maximum quantity asset")
    void reissueAlmostMaximumAssets() {
        long reissueSum = ASSET_QUANTITY_MAX - account.getAssetBalance(issuedAssetId) - ASSET_QUANTITY_MIN;
        Amount amount = Amount.of(reissueSum, issuedAssetId);
        for (int v = 1; v <= LATEST_VERSION; v++) {
            accountWavesBalance = account.getBalance(AssetId.WAVES);
            reissueTransactionSender(account, amount, issuedAssetId, MIN_FEE, v);
            checkReissueTransaction(amount, issuedAssetId, MIN_FEE, v);
            account.burn(reissueSum, issuedAssetId);
        }
    }

    @Test
    @DisplayName("reissue minimum quantity smart asset")
    void reissueMinimumSmartAssets() {
        Amount amount = Amount.of(ASSET_QUANTITY_MIN, issuedSmartAssetId);
        for (int v = 1; v <= LATEST_VERSION; v++) {
            accountWavesBalance = account.getBalance(AssetId.WAVES);
            reissueTransactionSender(account, amount, issuedSmartAssetId, SUM_FEE, v);
            checkReissueTransaction(amount, issuedSmartAssetId, SUM_FEE, v);
        }
    }

    @Test
    @DisplayName("reissue maximum quantity smart asset")
    void reissueAlmostMaximumSmartAssets() {
        long reissueSum = ASSET_QUANTITY_MAX - account.getAssetBalance(issuedSmartAssetId);
        Amount amount = Amount.of(reissueSum, issuedSmartAssetId);
        for (int v = 1; v <= LATEST_VERSION; v++) {
            accountWavesBalance = account.getBalance(AssetId.WAVES);
            reissueTransactionSender(account, amount, issuedSmartAssetId, SUM_FEE, v);
            checkReissueTransaction(amount, issuedSmartAssetId, SUM_FEE, v);
            account.burn(reissueSum, issuedSmartAssetId);
        }
    }

    private void checkReissueTransaction(Amount amount, AssetId assetId, long fee, int version) {
        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getAssetBalance(assetId)).isEqualTo(getBalanceAfterTransaction()),
                () -> assertThat(account.getWavesBalance()).isEqualTo(accountWavesBalance - fee),
                () -> assertThat(getReissueTx().fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(getReissueTx().fee().value()).isEqualTo(fee),
                () -> assertThat(getReissueTx().sender()).isEqualTo(account.publicKey()),
                () -> assertThat(getReissueTx().amount().value()).isEqualTo(amount.value()),
                () -> assertThat(getReissueTx().amount().assetId()).isEqualTo(assetId),
                () -> assertThat(getReissueTx().version()).isEqualTo(version),
                () -> assertThat(getReissueTx().type()).isEqualTo(5)
        );
    }
}
