package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.BurnTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.helpers.transaction_senders.BurnTransactionSender.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class BurnTransactionTest {
    private static Account account;
    private static AssetId issuedAsset;
    private static AssetId issuedSmartAssetId;

    @BeforeAll
    static void before() {
        account = new Account(DEFAULT_FAUCET);
        async(
                () -> account.createAlias(randomNumAndLetterString(15)),
                () -> issuedAsset = account.issue(i -> i.name("Test_Asset").quantity(1000)).tx().assetId(),
                () -> issuedSmartAssetId =
                        account.issue(i -> i.name("T_Smart_Asset")
                                .quantity(1000)
                                .script("2 * 2 == 4")).tx().assetId()
        );
    }

    @Test
    @DisplayName("burn minimum quantity asset")
    void burnMinimumAssets() {
        Amount amount = Amount.of(ASSET_QUANTITY_MIN, issuedAsset);

        for (int v = 1; v < LATEST_VERSION; v++) {
            burnTransactionSender(account, amount, issuedAsset, MIN_FEE, v);
            checkAssertsForBurnTransaction(issuedAsset, MIN_FEE, amount.value());
        }
    }

    @Test
    @DisplayName("burn almost maximum quantity asset")
    void burnMaximumAssets() {
        long burnSum = account.getAssetBalance(issuedAsset);
        Amount amount = Amount.of(burnSum, issuedAsset);

        for (int v = 1; v < LATEST_VERSION; v++) {
            burnTransactionSender(account, amount, issuedAsset, MIN_FEE, v);
            checkAssertsForBurnTransaction(issuedAsset, MIN_FEE, amount.value());
            account.reissue(1000, issuedAsset);
        }
    }

    @Test
    @DisplayName("burn minimum quantity smart asset")
    void burnMinimumSmartAssets() {
        long fee = MIN_FEE + EXTRA_FEE;
        Amount amount = Amount.of(ASSET_QUANTITY_MIN, issuedSmartAssetId);

        for (int v = 1; v < LATEST_VERSION; v++) {
            burnTransactionSender(account, amount, issuedSmartAssetId, fee, v);
            checkAssertsForBurnTransaction(issuedSmartAssetId, fee, amount.value());
        }
    }

    @Test
    @DisplayName("burn almost maximum quantity smart asset")
    void burnMaximumSmartAssets() {
        long fee = MIN_FEE + EXTRA_FEE;
        long burnSum = account.getAssetBalance(issuedSmartAssetId);
        Amount amount = Amount.of(burnSum, issuedSmartAssetId);

        for (int v = 1; v < LATEST_VERSION; v++) {
            burnTransactionSender(account, amount, issuedSmartAssetId, fee, v);
            checkAssertsForBurnTransaction(issuedSmartAssetId, fee, amount.value());
            account.reissue(1000, issuedSmartAssetId);
        }
    }

    private void checkAssertsForBurnTransaction(AssetId assetId, long fee, long amount) {
        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getAssetBalance(assetId)).isEqualTo(getBalanceAfterTransaction()),
                () -> assertThat(account.getWavesBalance()).isEqualTo(getAccountWavesBalance() - fee),
                () -> assertThat(getBurnTx().fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(getBurnTx().fee().value()).isEqualTo(fee),
                () -> assertThat(getBurnTx().amount().value()).isEqualTo(amount),
                () -> assertThat(getBurnTx().amount().assetId()).isEqualTo(assetId),
                () -> assertThat(getBurnTx().sender()).isEqualTo(account.publicKey()),
                () -> assertThat(getBurnTx().type()).isEqualTo(6)
        );
    }
}
