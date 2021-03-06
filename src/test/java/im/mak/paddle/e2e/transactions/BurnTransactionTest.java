package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.BurnTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.BurnTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class BurnTransactionTest {
    private static Account account;
    private static long accountWavesBalance;
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
            burnTransaction(amount, issuedAsset, MIN_FEE, v);
        }
    }

    @Test
    @DisplayName("burn almost maximum quantity asset")
    void burnMaximumAssets() {
        long burnSum = account.getAssetBalance(issuedAsset);
        Amount amount = Amount.of(burnSum, issuedAsset);

        for (int v = 1; v < LATEST_VERSION; v++) {
            burnTransaction(amount, issuedAsset, MIN_FEE, v);
            account.reissue(1000, issuedAsset);
        }
    }

    @Test
    @DisplayName("burn minimum quantity smart asset")
    void burnMinimumSmartAssets() {
        long fee = MIN_FEE + EXTRA_FEE;
        Amount amount = Amount.of(ASSET_QUANTITY_MIN, issuedSmartAssetId);

        for (int v = 1; v < LATEST_VERSION; v++) {
            burnTransaction(amount, issuedSmartAssetId, fee, v);
        }
    }

    @Test
    @DisplayName("burn almost maximum quantity smart asset")
    void burnMaximumSmartAssets() {
        long fee = MIN_FEE + EXTRA_FEE;
        long burnSum = account.getAssetBalance(issuedSmartAssetId);
        Amount amount = Amount.of(burnSum, issuedSmartAssetId);

        for (int v = 1; v < LATEST_VERSION; v++) {
            burnTransaction(amount, issuedSmartAssetId, fee, v);
            account.reissue(1000, issuedSmartAssetId);
        }
    }

    private void burnTransaction(Amount amount, AssetId assetId, long fee, int version) {
        accountWavesBalance = account.getBalance(AssetId.WAVES);
        long balanceAfterBurn = account.getBalance(assetId) - amount.value();

/*
        BurnTransaction tx = account.burn(amount, assetId).tx();
*/

        BurnTransaction tx = BurnTransaction.builder(amount)
                .version(version)
                .sender(account.publicKey())
                .fee(fee)
                .getSignedWith(account.privateKey());
        node().waitForTransaction(node().broadcast(tx).id());

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getAssetBalance(assetId)).isEqualTo(balanceAfterBurn),
                () -> assertThat(account.getWavesBalance()).isEqualTo(accountWavesBalance - fee),
                () -> assertThat(tx.fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(tx.fee().value()).isEqualTo(fee),
                () -> assertThat(tx.amount().value()).isEqualTo(amount.value()),
                () -> assertThat(tx.amount().assetId()).isEqualTo(assetId),
                () -> assertThat(tx.sender()).isEqualTo(account.publicKey()),
                () -> assertThat(tx.type()).isEqualTo(6)
        );
    }
}
