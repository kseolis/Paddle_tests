package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.SetScriptTransaction;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SetScriptTransactionTest {

    private static Account alice;

    @BeforeAll
    static void before() {
        alice = new Account(DEFAULT_FAUCET);
    }

    @Test
    @DisplayName("set script transaction Account")
    void setScriptTransactionForAccount() {
        String setScript = "{-# STDLIB_VERSION 3 #-}\n" +
                "{-# SCRIPT_TYPE ACCOUNT #-}\n" +
                "{-# CONTENT_TYPE LIBRARY #-}";
        setScriptTransaction(setScript);
    }

    @Test
    @DisplayName("set script transaction dApp")
    void setScriptTransactionForDapp() {
        String setScript = "{-# STDLIB_VERSION 3 #-}\n" +
                "{-# SCRIPT_TYPE ACCOUNT #-}\n" +
                "{-# CONTENT_TYPE DAPP #-}";
        setScriptTransaction(setScript);
    }

    private void setScriptTransaction(String script) {
        long balanceAfterTransaction = alice.getWavesBalance() - MIN_FEE_FOR_SET_SCRIPT;
        SetScriptTransaction tx = alice.setScript(script).tx();
        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(alice.getWavesBalance()).isEqualTo(balanceAfterTransaction),
                () -> assertThat(tx.sender()).isEqualTo(alice.publicKey()),
                () -> assertThat(tx.type()).isEqualTo(13),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(MIN_FEE_FOR_SET_SCRIPT)
        );
    }
}
