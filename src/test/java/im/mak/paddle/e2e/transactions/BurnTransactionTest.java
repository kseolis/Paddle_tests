package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.BurnTransaction;
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
import static im.mak.paddle.util.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class BurnTransactionTest {
    private static Account alice;
    private static long aliceWavesBalance;
    private static AssetId issuedAsset;

    @BeforeAll
    static void before() {
        alice = new Account(DEFAULT_FAUCET);
        async(
            () -> alice.createAlias(randomNumAndLetterString(15)),
            () -> issuedAsset = alice.issue(i -> i.name("Test_Asset").quantity(1000)).tx().assetId()
        );
    }

    @Test
    @DisplayName("test burn minimum quantity asset")
    void burnMinimumAssets() {
        burnTransaction(ASSET_QUANTITY_MINIMUM);
    }

    @Test
    @DisplayName("test burn maximum quantity asset")
    void burnMaximumAssets() {
        long burnSum = alice.getAssetBalance(issuedAsset);
        burnTransaction(burnSum);
    }

    private void burnTransaction(long amount) {
        aliceWavesBalance = alice.getBalance(AssetId.WAVES);
        long balanceAfterBurn = alice.getBalance(issuedAsset) - amount;
        BurnTransaction tx = alice.burn(amount, issuedAsset).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(alice.getAssetBalance(issuedAsset)).isEqualTo(balanceAfterBurn),
                () -> assertThat(alice.getWavesBalance()).isEqualTo(aliceWavesBalance - MIN_FEE),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.sender()).isEqualTo(alice.publicKey()),
                () -> assertThat(tx.type()).isEqualTo(6),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(MIN_FEE)
        );
    }
}
