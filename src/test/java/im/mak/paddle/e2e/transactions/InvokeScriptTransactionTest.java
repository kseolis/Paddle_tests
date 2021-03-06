package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.InvokeScriptTransaction;
import com.wavesplatform.transactions.LeaseCancelTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import im.mak.paddle.dapp.DAppCall;
import im.mak.paddle.dapps.IntDApp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.InvokeScriptTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.token.Waves.WAVES;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class InvokeScriptTransactionTest {
    private static IntDApp accWithDApp;
    private static Account account;

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
            invokeTransaction(account, accWithDApp, amount, v);
        }
    }

    private void invokeTransaction(Account callingAcc, IntDApp dAppAccount, Amount amount, int version) {
        long minFee = MIN_FEE + EXTRA_FEE;
        long balanceAfterTransaction = callingAcc.getWavesBalance() - minFee - amount.value();
        DAppCall dAppCall = dAppAccount.setInt(getRandomInt(1, 50));

        InvokeScriptTransaction tx = InvokeScriptTransaction
                .builder(dAppAccount.address(), dAppCall.getFunction())
                .payments(amount)
                .version(version)
                .getSignedWith(callingAcc.privateKey());

        node().waitForTransaction(node().broadcast(tx).id());

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(callingAcc.getWavesBalance()).isEqualTo(balanceAfterTransaction),
                () -> assertThat(tx.dApp()).isEqualTo(dAppCall.getDApp()),
                () -> assertThat(tx.function()).isEqualTo(dAppCall.getFunction()),
                () -> assertThat(tx.sender()).isEqualTo(callingAcc.publicKey()),
                () -> assertThat(tx.fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(tx.fee().value()).isEqualTo(minFee),
                () -> assertThat(tx.type()).isEqualTo(16)
        );
    }
}
