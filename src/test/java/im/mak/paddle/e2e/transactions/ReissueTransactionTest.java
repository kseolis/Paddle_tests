package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.ReissueTransaction;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ReissueTransactionTest {
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
    @DisplayName("test reissue minimum quantity asset")
    void reissueMinimumAssets() {
        reissueTransaction(ASSET_QUANTITY_MINIMUM);
    }

    @Test
    @DisplayName("test reissue maximum quantity asset")
    void reissueMaximumAssets() {
        long reissueSum = ASSET_QUANTITY_MAXIMUM - marrie.getAssetBalance(issuedAsset);
        reissueTransaction(reissueSum);
    }

    private void reissueTransaction(long amount) {
        long balanceAfterReissue = marrie.getBalance(issuedAsset) + amount;
        ReissueTransaction tx = marrie.reissue(amount, issuedAsset, i -> i.reissuable(true)).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(marrie.getAssetBalance(issuedAsset)).isEqualTo(balanceAfterReissue),
                () -> assertThat(marrie.getWavesBalance()).isEqualTo(marrieWavesBalance - MIN_FEE),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(MIN_FEE)
        );
        System.out.println("after balance " + balanceAfterReissue);
    }
}
