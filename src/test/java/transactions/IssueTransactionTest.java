package transactions;

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
    private final long assetQuantity = 9_223_372_036_854_775_807L;

    @BeforeEach
    void before() {
        alice = new Account(10_00000000L);
        System.out.println(node().uri());
        System.out.println(node().faucet());
        System.out.println("\n_________________________\n");
    }

    @Test
    void canIssueToken() {
        long initBalance = alice.getWavesBalance();

        IssueTransaction tx = alice.issue(i ->
                i.name("T_asset")
                    .description("Test Asset")
                    .quantity(assetQuantity)
                    .script("true")
                    .decimals(1)
                    .reissuable(true)).tx();

        TransactionInfo transactionInfo = node().getTransactionInfo(tx.id());

        System.out.println(transactionInfo.tx().toJson());

        assertAll("check 'issue transaction' result",
            () -> assertThat(alice.getWavesBalance()).isEqualTo(initBalance - ONE_WAVES),
            () -> assertThat(alice.getAssetBalance(tx.assetId())).isEqualTo(assetQuantity),
            () -> assertThat(transactionInfo.applicationStatus()).isEqualTo(SUCCEEDED),
            () -> assertThat((Object) transactionInfo.tx().fee().value()).isEqualTo(ONE_WAVES)
        );
    }
}
