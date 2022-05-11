package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.SetScriptTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
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
        setScriptTransaction(stan, setScript, 0);
    }

    @Test
    @DisplayName("set script transaction dApp STDLIB V4")
    void setDAppScriptTransaction() {
        Base64String setScript = node().compileScript("{-# STDLIB_VERSION 4 #-}\n" +
                "{-# SCRIPT_TYPE ACCOUNT #-}\n" +
                "{-# CONTENT_TYPE DAPP #-}").script();
        setScriptTransaction(eric, setScript, 0);
    }

    @Test
    @DisplayName("set script transaction SNDLIB V5")
    void setScriptTransaction() {
        Base64String setScript = node().compileScript("{-# STDLIB_VERSION 5 #-}\n" +
                "{-# SCRIPT_TYPE ASSET #-} true").script();
        setScriptTransaction(kenny, setScript, 0);
    }

    @Test
    @DisplayName("set 32kb script")
    void set32KbScript() {
        long minimalValSetScriptFee = 2200000;
        Base64String setScript = node().compileScript(fromFile("/scriptSize32kb.ride")).script();
        setScriptTransaction(kyle, setScript, minimalValSetScriptFee);
    }

    private void setScriptTransaction(Account account, Base64String script, long additionalFee) {
        long fee = MIN_FEE_FOR_SET_SCRIPT + additionalFee;
        long balanceAfterTransaction = account.getWavesBalance() - MIN_FEE_FOR_SET_SCRIPT - additionalFee;

        SetScriptTransaction tx = account.setScript(script, i -> i.additionalFee(additionalFee)).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getWavesBalance()).isEqualTo(balanceAfterTransaction),
                () -> assertThat(tx.sender()).isEqualTo(account.publicKey()),
                () -> assertThat(tx.script()).isEqualTo(script),
                () -> assertThat(tx.fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(tx.fee().value()).isEqualTo(fee),
                () -> assertThat(tx.type()).isEqualTo(13)
        );
    }
}
