package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.LeaseCancelTransaction;
import com.wavesplatform.transactions.LeaseTransaction;
import com.wavesplatform.transactions.common.Id;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.getTransactionId;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Leasing.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.LeaseCancelTransaction.getLeaseCancelLeaseId;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.getTransactionVersion;
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
    private final long amountBefore = DEFAULT_FAUCET - MIN_FEE;
    private final long amountAfter = amountBefore - MIN_FEE;

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
        final LeaseCancelTransaction leaseCancelTx = sender.cancelLease(leaseId).tx();
        final String leaseCancelId = leaseCancelTx.id().toString();

        height = node().getHeight();
        subscribeResponseHandler(channel, sender, height, height);

        assertAll(
                // transaction
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(senderPublicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(LeaseCancelTransaction.LATEST_VERSION),
                // lease cancel
                () -> assertThat(getLeaseCancelLeaseId(0)).isEqualTo(leaseIdString),
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
                () -> assertThat(getLeaseIdFromIndividualLeases(0, 0)).isEqualTo(leaseIdString),
                () -> assertThat(getStatusAfterFromIndividualLeases(0, 0)).isEqualTo(INACTIVE_STATUS_LEASE),
                () -> assertThat(getAmountFromIndividualLeases(0, 0)).isEqualTo(amountLease),
                () -> assertThat(getSenderFromIndividualLeases(0, 0)).isEqualTo(senderPublicKey),
                () -> assertThat(getRecipientFromIndividualLeases(0, 0)).isEqualTo(recipientAddress),
                () -> assertThat(getOriginalTransactionIdFromIndividualLeases(0, 0)).isEqualTo(leaseIdString)
        );
    }
}
