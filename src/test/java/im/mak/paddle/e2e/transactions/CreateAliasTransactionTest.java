package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.CreateAliasTransaction;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class CreateAliasTransactionTest {
    private static Account alice;
    private static String aliceAlias;

    @BeforeAll
    static void before() {
        alice = new Account(DEFAULT_FAUCET);
    }

    @Test
    @DisplayName("test create minimally short alias")
    void createMinShortAlias() {
        aliceAlias = randomNumAndLetterString(4);
        createAliasTransaction(aliceAlias);
    }

    @Test
    @DisplayName("test create maximum long alias")
    void createMaxLongAlias() {
        aliceAlias = randomNumAndLetterString(30);
        createAliasTransaction(aliceAlias);
    }

    private void createAliasTransaction(String alias) {
        long aliceWavesBalance = alice.getWavesBalance();
        long balanceAfterCreateAlias = aliceWavesBalance - MIN_FEE;
        CreateAliasTransaction tx = alice.createAlias(alias).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(alice.getWavesBalance()).isEqualTo(balanceAfterCreateAlias),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.alias().name()).isEqualTo(alias),
                () -> assertThat(tx.sender()).isEqualTo(alice.publicKey()),
                () -> assertThat(tx.type()).isEqualTo(10),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(MIN_FEE)
        );
    }
}
