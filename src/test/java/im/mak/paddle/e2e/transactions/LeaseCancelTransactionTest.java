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

    private static Account stan;
    private static Account eric;

    private static Account kenny;
    private static Account kyle;

    @BeforeAll
    static void before() {
        async(
                () -> stan = new Account(DEFAULT_FAUCET),
                () -> eric = new Account(DEFAULT_FAUCET)
        );
        async(
                () -> kenny = new Account(DEFAULT_FAUCET),
                () -> kyle = new Account(DEFAULT_FAUCET)
        );
    }

    @Test
    @DisplayName("cancel lease of the minimum available amount")
    void leaseMinAssets() {
        Id minLeaseTx = stan.lease(eric, MIN_TRANSFER_SUM).tx().id();
        cancelLeaseTransaction(stan, eric, minLeaseTx, MIN_TRANSFER_SUM);
    }

    @Test
    @DisplayName("cancel lease of the maximum available amount")
    void leaseMaxAssets() {
        long leaseSum = kenny.getWavesBalance() - MIN_FEE;
        Id maxLeaseTx = kenny.lease(kyle, leaseSum).tx().id();
        cancelLeaseTransaction(kenny, kyle, maxLeaseTx, leaseSum);
    }

    private void cancelLeaseTransaction(Account from, Account to, Id index, long leaseSum) {
        long balanceAfterCancelLeaseAtSender = from.getWavesBalanceDetails().effective() - MIN_FEE + leaseSum;
        long balanceAfterCancelLeaseAtRecipient = to.getWavesBalanceDetails().effective() - leaseSum;

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
