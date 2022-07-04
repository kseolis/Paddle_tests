package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.LeaseTransaction;
import com.wavesplatform.transactions.common.Id;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import im.mak.paddle.dapps.DefaultDApp420Complexity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.LeaseCancelTransaction.LATEST_VERSION;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.getTransactionId;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Leasing.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.LeaseCancelTransactionHandler.getLeaseCancelLeaseId;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionVersion;
import static im.mak.paddle.helpers.transaction_senders.LeaseTransactionSender.getLeaseCancelTx;
import static im.mak.paddle.helpers.transaction_senders.LeaseTransactionSender.leaseCancelTransactionSender;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class LeaseCancelTransactionSubscriptionTest extends BaseTest {
    private Account sender;
    private String senderPublicKey;
    private String senderAddress;

    private Account recipient;
    private String recipientAddress;

    private int amountLease;
    private long amountBefore = DEFAULT_FAUCET - MIN_FEE;
    private long amountAfter = amountBefore - MIN_FEE;
    private static final DefaultDApp420Complexity accWithDApp = new DefaultDApp420Complexity(DEFAULT_FAUCET);

    @BeforeEach
    void setUp() {
        async(
                () -> amountLease = getRandomInt(100_000, 1_00_000_000),
                () -> {
                    sender = new Account(DEFAULT_FAUCET);
                    senderAddress = sender.address().toString();
                    senderPublicKey = sender.publicKey().toString();
                },
                () -> {
                    recipient = new Account(DEFAULT_FAUCET);
                    recipientAddress = recipient.address().toString();
                }
        );
    }

    @Test
    @DisplayName("Check subscription on lease waves transaction")
    void subscribeTestForWavesLeaseTransaction() {
        final LeaseTransaction leaseTx = sender.lease(recipient, amountLease).tx();
        final Id leaseId = leaseTx.id();
        final String leaseIdString = leaseId.toString();

        leaseCancelTransactionSender(sender, recipient, leaseId, amountLease, MIN_FEE, LATEST_VERSION);
        final String leaseCancelId = getLeaseCancelTx().id().toString();
        height = node().getHeight();
        subscribeResponseHandler(channel, sender, height, height);
        checkLeaseCancelSubscribe(leaseIdString, leaseCancelId, MIN_FEE);
    }

    @Test
    @DisplayName("Check subscription on lease waves transaction in smartAcc")
    void subscribeTestForWavesLeaseTransactionDAppAcc() {
        senderAddress = accWithDApp.address().toString();
        senderPublicKey = accWithDApp.publicKey().toString();
        final LeaseTransaction leaseTx = accWithDApp.lease(recipient, amountLease).tx();
        final Id leaseId = leaseTx.id();
        final String leaseIdString = leaseId.toString();
        amountBefore = accWithDApp.getWavesBalance();
        amountAfter = amountBefore - SUM_FEE;

        leaseCancelTransactionSender(accWithDApp, recipient, leaseId, amountLease, SUM_FEE, LATEST_VERSION);
        final String leaseCancelId = getLeaseCancelTx().id().toString();
        height = node().getHeight();
        subscribeResponseHandler(channel, accWithDApp, height, height);
        checkLeaseCancelSubscribe(leaseIdString, leaseCancelId, SUM_FEE);
    }

    private void checkLeaseCancelSubscribe(String leaseId, String leaseCancelId, long fee) {
        assertAll(
                // transaction
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(senderPublicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(fee),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(LATEST_VERSION),
                // lease cancel
                () -> assertThat(getLeaseCancelLeaseId(0)).isEqualTo(leaseId),
                // transaction_ids
                () -> assertThat(getTransactionId()).isEqualTo(leaseCancelId),
                // balances sender
                () -> assertThat(getAddress(0, 0)).isEqualTo(senderAddress),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(amountBefore),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(amountAfter),
                // leasing_for_address sender
                () -> assertThat(getAddressFromLeasingForAddress(0, 0)).isEqualTo(senderAddress),
                () -> assertThat(getOutBeforeFromLeasingForAddress(0, 0)).isEqualTo(amountLease),
                () -> assertThat(getOutAfterFromLeasingForAddress(0, 0)).isEqualTo(0),
                // leasing_for_address recipient
                () -> assertThat(getAddressFromLeasingForAddress(0, 1)).isEqualTo(recipientAddress),
                () -> assertThat(getInBeforeFromLeasingForAddress(0, 1)).isEqualTo(amountLease),
                () -> assertThat(getInAfterFromLeasingForAddress(0, 1)).isEqualTo(0),
                // individual_leases
                () -> assertThat(getLeaseIdFromIndividualLeases(0, 0)).isEqualTo(leaseId),
                () -> assertThat(getStatusAfterFromIndividualLeases(0, 0)).isEqualTo(INACTIVE_STATUS_LEASE),
                () -> assertThat(getAmountFromIndividualLeases(0, 0)).isEqualTo(amountLease),
                () -> assertThat(getSenderFromIndividualLeases(0, 0)).isEqualTo(senderPublicKey),
                () -> assertThat(getRecipientFromIndividualLeases(0, 0)).isEqualTo(recipientAddress),
                () -> assertThat(getOriginalTransactionIdFromIndividualLeases(0, 0)).isEqualTo(leaseId)
        );
    }
}
