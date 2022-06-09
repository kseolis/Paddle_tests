package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.dapps.IntDApp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.InvokeScriptTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getBalanceAfterTransaction;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.InvokeScriptTransactionSender.*;
import static im.mak.paddle.token.Waves.WAVES;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class InvokeScriptTransactionTest {
    private static IntDApp accWithDApp;
    private static Account account;
    private static final long fee = MIN_FEE + EXTRA_FEE;

    @BeforeAll
    static void before() {
        accWithDApp = new IntDApp(DEFAULT_FAUCET);
        account = new Account(DEFAULT_FAUCET);
    }

    @Test
    @DisplayName("invoke script with small complexity")
    void invokeScriptWithSmallComplexityTest() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            Amount amount = WAVES.of(0.1);
            invokeIntDAppSender(account, accWithDApp, amount, v, fee);
            checkAssertsForSetScriptTransaction();
        }
    }

    private void checkAssertsForSetScriptTransaction() {
        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getWavesBalance()).isEqualTo(getBalanceAfterTransaction()),
                () -> assertThat(getInvokeScriptTx().dApp()).isEqualTo(getDAppCall().getDApp()),
                () -> assertThat(getInvokeScriptTx().function()).isEqualTo(getDAppCall().getFunction()),
                () -> assertThat(getInvokeScriptTx().sender()).isEqualTo(account.publicKey()),
                () -> assertThat(getInvokeScriptTx().fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(getInvokeScriptTx().fee().value()).isEqualTo(fee),
                () -> assertThat(getInvokeScriptTx().type()).isEqualTo(16)
        );
    }
}
