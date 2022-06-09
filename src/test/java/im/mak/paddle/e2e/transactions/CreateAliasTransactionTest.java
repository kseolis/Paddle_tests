package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.CreateAliasTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getBalanceAfterTransaction;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.CreateAliasTransactionSender.createAliasTransactionSender;
import static im.mak.paddle.helpers.transaction_senders.CreateAliasTransactionSender.getCreateAliasTx;
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
        for (int v = 1; v <= LATEST_VERSION; v++) {
            accountAlias = randomNumAndLetterString(4);
            createAliasTransactionSender(account, accountAlias, v);
            checkAssertsForCreateAliasTransaction(accountAlias);
        }
    }

    @Test
    @DisplayName("test create maximum long alias")
    void createMaxLongAlias() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            accountAlias = randomNumAndLetterString(30);
            createAliasTransactionSender(account, accountAlias, v);
            checkAssertsForCreateAliasTransaction(accountAlias);
        }
    }

    private void checkAssertsForCreateAliasTransaction(String alias) {
        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getWavesBalance()).isEqualTo(getBalanceAfterTransaction()),
                () -> assertThat(getCreateAliasTx().sender()).isEqualTo(account.publicKey()),
                () -> assertThat(getCreateAliasTx().alias().name()).isEqualTo(alias),
                () -> assertThat(getCreateAliasTx().fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(getCreateAliasTx().fee().value()).isEqualTo(MIN_FEE),
                () -> assertThat(getCreateAliasTx().type()).isEqualTo(10)
        );
    }
}
