package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.BurnTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class BurnTransactionTest {
    private Account marrie;
    private long marrieWavesBalance;
    private AssetId issuedAsset;

    @BeforeEach
    void before() {
        marrie = new Account(DEFAULT_FAUCET);
        marrie.createAlias(randomNumAndLetterString(15));
        issuedAsset = marrie.issue(i -> i.name("Test_Asset").quantity(1000)).tx().assetId();
        marrieWavesBalance = marrie.getBalance(AssetId.WAVES);
    }

    @Test
    @DisplayName("test burn minimum quantity asset")
    void reissueMinimumAssets() {
        burnTransaction(ASSET_QUANTITY_MINIMUM);
    }

    @Test
    @DisplayName("test burn maximum quantity asset")
    void reissueMaximumAssets() {
        long burnSum = marrie.getAssetBalance(issuedAsset);
        burnTransaction(burnSum);
    }

    private void burnTransaction(long amount) {
        long balanceAfterBurn = marrie.getBalance(issuedAsset) - amount;
        BurnTransaction tx = marrie.burn(amount, issuedAsset).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(marrie.getAssetBalance(issuedAsset)).isEqualTo(balanceAfterBurn),
                () -> assertThat(marrie.getWavesBalance()).isEqualTo(marrieWavesBalance - MIN_FEE),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(MIN_FEE)
        );
    }
}
