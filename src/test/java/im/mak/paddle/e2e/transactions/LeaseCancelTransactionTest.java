package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.LeaseCancelTransaction;
import com.wavesplatform.transactions.LeaseTransaction;
import com.wavesplatform.transactions.common.Id;
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
    private static LeaseTransaction minLeaseTx;
    private static Account bob;
    private static LeaseTransaction maxLeaseTx;

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
        minLeaseTx = alice.lease(bob, MIN_TRANSFER_SUM).tx();
        maxLeaseTx = bob.lease(alice, MIN_TRANSFER_SUM).tx();
    }

    @Test
    @DisplayName("cancel lease of the minimum available amount")
    void leaseMinAssets() {
        cancelLeaseTransaction(alice, bob, minLeaseTx.id());
    }

    @Test
    @DisplayName("cancel lease of the minimum available amount")
    void leaseMaxAssets() {
        cancelLeaseTransaction(bob, alice, maxLeaseTx.id());
    }

    private void cancelLeaseTransaction(Account from, Account to, Id index) {
        long balanceAfterCancelLeaseAtSender = from.getWavesBalanceDetails().effective() - MIN_FEE + MIN_TRANSFER_SUM;
        long balanceAfterCancelLeaseAtRecipient = to.getWavesBalanceDetails().effective() - MIN_TRANSFER_SUM;

        LeaseCancelTransaction cancelTransaction = from.cancelLease(index).tx();
        TransactionInfo txInfo = node().getTransactionInfo(cancelTransaction.id());

        assertAll(
                () -> assertThat(from.getWavesBalanceDetails().effective()).isEqualTo(balanceAfterCancelLeaseAtSender),
                () -> assertThat(to.getWavesBalanceDetails().effective()).isEqualTo(balanceAfterCancelLeaseAtRecipient),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(cancelTransaction.sender()).isEqualTo(from.publicKey()),
                () -> assertThat(cancelTransaction.type()).isEqualTo(9),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(MIN_FEE)
        );
    }
}
