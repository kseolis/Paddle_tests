package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.CreateAliasTransaction;
import com.wavesplatform.transactions.common.AssetId;
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
    private static Account account;
    private static String accountAlias;

    @BeforeAll
    static void before() {
        account = new Account(DEFAULT_FAUCET);
    }

    @Test
    @DisplayName("test create minimally short alias")
    void createMinShortAlias() {
        accountAlias = randomNumAndLetterString(4);
        createAliasTransaction(accountAlias);
    }

    @Test
    @DisplayName("test create maximum long alias")
    void createMaxLongAlias() {
        accountAlias = randomNumAndLetterString(30);
        createAliasTransaction(accountAlias);
    }

    private void createAliasTransaction(String alias) {
        long accountWavesBalance = account.getWavesBalance();
        long balanceAfterCreateAlias = accountWavesBalance - MIN_FEE;
        CreateAliasTransaction tx = account.createAlias(alias).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getWavesBalance()).isEqualTo(balanceAfterCreateAlias),
                () -> assertThat(tx.sender()).isEqualTo(account.publicKey()),
                () -> assertThat(tx.alias().name()).isEqualTo(alias),
                () -> assertThat(tx.fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(tx.fee().value()).isEqualTo(MIN_FEE),
                () -> assertThat(tx.type()).isEqualTo(10)
        );
    }
}
