package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.SponsorFeeTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.DEFAULT_FAUCET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SponsorFeeTransactionTest {

    private static Account alice;
    private static Account bob;

    private static AssetId testAssetId;

    @BeforeAll
    static void before() {
        async(
                () -> {
                    alice = new Account(DEFAULT_FAUCET);
                    testAssetId = alice.issue(i -> i.name("Test_Asset").quantity(1000L).decimals(8)).tx().assetId();
                },
                () -> bob = new Account(DEFAULT_FAUCET)
        );
    }

    @Test
    @DisplayName("Sponsor transaction")
    void exchangeMaxAssets() {
        sponsorTransaction();
    }

    private void sponsorTransaction() {
        SponsorFeeTransaction tx = alice.sponsorFee(testAssetId, 50).tx();
        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.sender()).isEqualTo(alice.publicKey()),
                () -> assertThat(tx.type()).isEqualTo(14)
        );
    }
}
