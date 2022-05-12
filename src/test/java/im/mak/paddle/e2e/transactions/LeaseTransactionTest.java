package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.LeaseTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.LeaseTransaction.LATEST_VERSION;
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
                () -> alice = new Account(DEFAULT_FAUCET),
                () -> bob = new Account(DEFAULT_FAUCET)
        );
    }

    @Test
    @DisplayName("Minimum lease sum transaction")
    void leaseMinimumWavesAssets() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            leaseTransaction(MIN_TRANSFER_SUM, bob, alice, v);
        }
    }

    @Test
    @DisplayName("lease asset transaction 1 WAVES")
    void leaseOneWavesAssets() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            leaseTransaction(ONE_WAVES, bob, alice, v);
        }
    }

    @Test
    @DisplayName("Maximum lease sum transaction")
    void leaseMaximumAssets() {
        long aliceBalance = alice.getWavesBalance() - MIN_FEE;
        for (int v = 1; v <= LATEST_VERSION; v++) {
            leaseTransaction(aliceBalance, alice, bob, v);
            node().faucet().transfer(alice, DEFAULT_FAUCET, AssetId.WAVES);
        }
    }

    private void leaseTransaction(long amount, Account from, Account to, int version) {
        long wavesBalanceAfterSendLeaseTransaction = from.getWavesBalanceDetails().effective() - MIN_FEE - amount;
        long wavesBalanceAfterReceivingLease = to.getWavesBalanceDetails().effective() + amount;

        LeaseTransaction tx = LeaseTransaction
                .builder(to.address(), amount)
                .version(version)
                .getSignedWith(from.privateKey());

        node().waitForTransaction(node().broadcast(tx).id());

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.sender()).isEqualTo(from.publicKey()),
                () -> assertThat(tx.amount()).isEqualTo(amount),
                () -> assertThat(tx.recipient()).isEqualTo(to.address()),
                () -> assertThat(tx.fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(tx.fee().value()).isEqualTo(MIN_FEE),
                () -> assertThat(from.getWavesBalanceDetails().effective()).isEqualTo(wavesBalanceAfterSendLeaseTransaction),
                () -> assertThat(to.getWavesBalanceDetails().effective()).isEqualTo(wavesBalanceAfterReceivingLease),
                () -> assertThat(tx.type()).isEqualTo(8)
        );
    }
}
