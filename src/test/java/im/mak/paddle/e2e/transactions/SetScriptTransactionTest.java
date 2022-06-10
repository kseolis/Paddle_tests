package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.SetScriptTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getBalanceAfterTransaction;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.SetScriptTransactionSender.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SetScriptTransactionTest {

    private static Account stan;
    private static Account eric;
    private static Account kenny;
    private static Account kyle;

    @BeforeAll
    static void before() {
        async(
                () -> stan = new Account(DEFAULT_FAUCET),
                () -> eric = new Account(DEFAULT_FAUCET),
                () -> kenny = new Account(DEFAULT_FAUCET),
                () -> kyle = new Account(DEFAULT_FAUCET)
        );
    }

    @Test
    @DisplayName("set script transaction Account STDLIB V3")
    void setLibScriptTransaction() {
        Base64String setScript = node().compileScript("{-# STDLIB_VERSION 3 #-}\n" +
                "{-# SCRIPT_TYPE ACCOUNT #-}\n" +
                "{-# CONTENT_TYPE LIBRARY #-}").script();
        setScriptTransactionSender(stan, setScript, 0, LATEST_VERSION);
        checkSetScriptTransaction(stan, setScript);
    }

    @Test
    @DisplayName("set script transaction dApp STDLIB V4")
    void setDAppScriptTransaction() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            Base64String setScript = node().compileScript("{-# STDLIB_VERSION 4 #-}\n" +
                    "{-# SCRIPT_TYPE ACCOUNT #-}\n" +
                    "{-# CONTENT_TYPE DAPP #-}").script();
            setScriptTransactionSender(eric, setScript, 0, v);
            checkSetScriptTransaction(eric, setScript);
        }
    }

    @Test
    @DisplayName("set script transaction SNDLIB V5")
    void setScriptTransactionLibV5() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            Base64String setScript = node().compileScript(SCRIPT_PERMITTING_OPERATIONS).script();
            setScriptTransactionSender(kenny, setScript, 0, v);
            checkSetScriptTransaction(kenny, setScript);
        }
    }

    @Test
    @DisplayName("set 32kb script")
    void set32KbScript() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            long minimalValSetScriptFee = 2200000;
            Base64String setScript = node().compileScript(fromFile("ride_scripts/scriptSize32kb.ride")).script();
            setScriptTransactionSender(kyle, setScript, minimalValSetScriptFee, v);
            checkSetScriptTransaction(kyle, setScript);
        }
    }

    private void checkSetScriptTransaction(Account account, Base64String script) {
        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getWavesBalance()).isEqualTo(getBalanceAfterTransaction()),
                () -> assertThat(getSetScriptTx().sender()).isEqualTo(account.publicKey()),
                () -> assertThat(getSetScriptTx().script()).isEqualTo(script),
                () -> assertThat(getSetScriptTx().fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(getSetScriptTx().fee().value()).isEqualTo(getFee()),
                () -> assertThat(getSetScriptTx().type()).isEqualTo(13)
        );
    }
}
