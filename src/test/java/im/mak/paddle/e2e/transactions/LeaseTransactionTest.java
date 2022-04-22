package im.mak.paddle.e2e.transactions;

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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class LeaseTransactionTest {
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
    }

    @Test
    @DisplayName("Minimum lease sum transaction")
    void leaseMinimumWavesAssets() {
        leaseTransaction(MIN_TRANSFER_SUM, bob, alice);
    }

    @Test
    @DisplayName("lease asset transaction 1 WAVES")
    void leaseOneWavesAssets() {
        leaseTransaction(ONE_WAVES, bob, alice);
    }

    @Test
    @DisplayName("Maximum lease sum transaction")
    void leaseMaximumAssets() {
        long aliceBalance = alice.getWavesBalance() - MIN_FEE;
        leaseTransaction(aliceBalance, alice, bob);
    }

    private void leaseTransaction(long amount, Account from, Account to) {
        long wavesBalanceAfterSendLeaseTransaction = from.getWavesBalanceDetails().effective() - MIN_FEE - amount;
        long wavesBalanceAfterReceivingLease = to.getWavesBalanceDetails().effective() + amount;

        LeaseTransaction tx = from.lease(to, amount).tx();
        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(from.getWavesBalanceDetails().effective()).isEqualTo(wavesBalanceAfterSendLeaseTransaction),
                () -> assertThat(to.getWavesBalanceDetails().effective()).isEqualTo(wavesBalanceAfterReceivingLease),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.sender()).isEqualTo(from.publicKey()),
                () -> assertThat(tx.amount()).isEqualTo(amount),
                () -> assertThat(tx.recipient()).isEqualTo(to.address()),
                () -> assertThat(tx.type()).isEqualTo(8),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(MIN_FEE)
        );
    }
}
