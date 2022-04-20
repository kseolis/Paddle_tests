package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.LeaseCancelTransaction;
import com.wavesplatform.transactions.LeaseTransaction;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class LeaseCancelTransactionTest {

    private static Account alice;
    private static Account bob;

    @BeforeAll
    static void before() {
        async(
                () -> {
                    alice = new Account(DEFAULT_FAUCET);
                },
                () -> {
                    bob = new Account(DEFAULT_FAUCET);
                }
        );
        alice.lease(bob, MIN_TRANSFER_SUM).tx();
    }

    @Test
    @DisplayName("Minimum lease sum transaction")
    void leaseMinimumWavesAssets() {
        leaseTransaction(bob, alice);
    }

    @Test
    @DisplayName("lease asset transaction 1 WAVES")
    void leaseOneWavesAssets() {
        leaseTransaction(bob, alice);
    }

    @Test
    @DisplayName("Maximum lease sum transaction")
    void leaseMaximumAssets() {
        leaseTransaction(alice, bob);
    }

    private void leaseTransaction(Account from, Account to) {
        long wavesBalanceAfterSendLeaseTransaction = from.getWavesBalanceDetails().effective() - MIN_FEE + MIN_TRANSFER_SUM;
        long wavesBalanceAfterReceivingLease = to.getWavesBalanceDetails().effective() - MIN_TRANSFER_SUM;

        LeaseCancelTransaction tx = from.cancelLease(node().getActiveLeases(alice.address()).get(0).id()).tx();
        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(from.getWavesBalanceDetails().effective()).isEqualTo(wavesBalanceAfterSendLeaseTransaction),
                () -> assertThat(to.getWavesBalanceDetails().effective()).isEqualTo(wavesBalanceAfterReceivingLease),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.sender()).isEqualTo(from.publicKey()),
                () -> assertThat(tx.type()).isEqualTo(9),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(MIN_FEE)
        );
    }
}
