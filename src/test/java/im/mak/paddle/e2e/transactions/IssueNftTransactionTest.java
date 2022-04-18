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

class IssueNftTransactionTest {

    private Account alice;

    @BeforeEach
    void before() {
        alice = new Account(DEFAULT_FAUCET);
    }

    @Test
    void canIssueNftToken() {
        long initBalance = alice.getWavesBalance();

        IssueTransaction tx = alice.issueNft(i ->
                i.name("T_NFT_asset")
                        .description("Test NFT Asset")
                        .script("true")).tx();

        TransactionInfo transactionInfo = node().getTransactionInfo(tx.id());

        assertAll("check 'issue transaction' result",
                () -> assertThat(alice.getWavesBalance()).isEqualTo(initBalance - MIN_FEE),
                () -> assertThat(transactionInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat((Object) transactionInfo.tx().fee().value()).isEqualTo(MIN_FEE)
        );
    }
}
