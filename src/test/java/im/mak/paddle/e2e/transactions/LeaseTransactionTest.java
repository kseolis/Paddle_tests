package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.dapps.DefaultDApp420Complexity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.LeaseTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.LeaseTransactionSender.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class LeaseTransactionTest {
    private static Account alice;
    private static Account bob;
    private static DefaultDApp420Complexity smartAcc;

    @BeforeAll
    static void before() {
        async(
                () -> alice = new Account(DEFAULT_FAUCET),
                () -> bob = new Account(DEFAULT_FAUCET),
                () -> smartAcc = new DefaultDApp420Complexity(DEFAULT_FAUCET)
        );
    }

    @Test
    @DisplayName("Minimum lease sum transaction")
    void leaseMinimumWavesAssets() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            leaseTransactionSender(MIN_TRANSACTION_SUM, bob, alice, MIN_FEE, v);
            leaseTransactionCheck(MIN_TRANSACTION_SUM, bob, alice, MIN_FEE);
        }
    }

    @Test
    @DisplayName("lease asset transaction random WAVES")
    void leaseOneWavesAssets() {
        long amount = getRandomInt(100_000, 1_000_000_00);
        for (int v = 1; v <= LATEST_VERSION; v++) {
            leaseTransactionSender(amount, bob, alice, MIN_FEE, v);
            leaseTransactionCheck(amount, bob, alice, MIN_FEE);
        }
    }

    @Test
    @DisplayName("Maximum lease sum transaction")
    void leaseMaximumAssets() {
        long amount = alice.getWavesBalance() - MIN_FEE;
        for (int v = 1; v <= LATEST_VERSION; v++) {
            leaseTransactionSender(amount, alice, bob, MIN_FEE, v);
            leaseTransactionCheck(amount, alice, bob, MIN_FEE);
            node().faucet().transfer(alice, DEFAULT_FAUCET, AssetId.WAVES);
        }
    }

    @Test
    @DisplayName("Maximum lease sum transaction from DApp account")
    void leaseFromDAppAccount() {
        long amount = smartAcc.getWavesBalance() - SUM_FEE;
        leaseTransactionSender(amount, smartAcc, bob, SUM_FEE, LATEST_VERSION);
        leaseTransactionCheck(amount, smartAcc, bob, SUM_FEE);
        node().faucet().transfer(smartAcc, DEFAULT_FAUCET, AssetId.WAVES);
    }

    private void leaseTransactionCheck(long amount, Account from, Account to, long fee) {

        assertThat(from.getWavesBalanceDetails().effective()).isEqualTo(getEffectiveBalanceAfterSendTransaction());
        assertThat(to.getWavesBalanceDetails().effective()).isEqualTo(getBalanceAfterReceiving());
        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(getLeaseTx().sender()).isEqualTo(from.publicKey()),
                () -> assertThat(getLeaseTx().amount()).isEqualTo(amount),
                () -> assertThat(getLeaseTx().recipient()).isEqualTo(to.address()),
                () -> assertThat(getLeaseTx().fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(getLeaseTx().fee().value()).isEqualTo(fee),
                () -> assertThat(getLeaseTx().type()).isEqualTo(8)
        );
    }
}
