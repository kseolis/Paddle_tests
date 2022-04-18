package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.*;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class IssueTransactionTest {

    private Account alice;
    private final byte assetQuantityMin = 1;
    private final long assetQuantityMax = 9_223_372_036_854_775_807L;

    @BeforeEach
    void before() {
        alice = new Account(DEFAULT_FAUCET);
        System.out.println(node().uri());
        System.out.println(node().faucet());
        System.out.println("\n_________________________\n");
    }

    @Test
    void canIssueToken() {
        long initBalance = alice.getWavesBalance();

        IssueTransaction txMin = alice.issue(i ->
                i.name("T_asset_min")
                    .description("Test Asset")
                    .quantity(assetQuantityMin)
                    .script("true")
                    .decimals(ASSET_DECIMALS_MIN)
                    .reissuable(true)).tx();

        IssueTransaction txMax = alice.issue(i ->
                i.name("T_asset")
                        .description("Test Asset")
                        .quantity(assetQuantityMax)
                        .script("true")
                        .decimals(ASSET_DECIMALS_MAX)
                        .reissuable(true)).tx();

        TransactionInfo txMinInfo = node().getTransactionInfo(txMin.id());
        TransactionInfo txMaxInfo = node().getTransactionInfo(txMax.id());

        assertAll("check 'issues transaction for min / max values' result",
            () -> assertThat(alice.getWavesBalance()).isEqualTo(initBalance - ONE_WAVES * 2),

            () -> assertThat(alice.getAssetBalance(txMin.assetId())).isEqualTo(assetQuantityMin),
            () -> assertThat(txMinInfo.applicationStatus()).isEqualTo(SUCCEEDED),
            () -> assertThat((Object) txMinInfo.tx().fee().value()).isEqualTo(ONE_WAVES),

            () -> assertThat(alice.getAssetBalance(txMax.assetId())).isEqualTo(assetQuantityMax),
            () -> assertThat(txMaxInfo.applicationStatus()).isEqualTo(SUCCEEDED),
            () -> assertThat((Object) txMaxInfo.tx().fee().value()).isEqualTo(ONE_WAVES)
        );
    }
}
