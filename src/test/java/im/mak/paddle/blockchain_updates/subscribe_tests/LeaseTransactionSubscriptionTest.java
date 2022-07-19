package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.crypto.base.Base58;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import im.mak.paddle.dapps.DefaultDApp420Complexity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.LeaseTransaction.LATEST_VERSION;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.getTransactionId;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.TransactionMetadataHandler.getLeaseTransactionMetadata;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Leasing.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.LeaseTransactionHandler.getLeaseAssetAmount;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.LeaseTransactionHandler.getLeaseTransactionPublicKeyHash;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.helpers.transaction_senders.LeaseTransactionSender.getLeaseTx;
import static im.mak.paddle.helpers.transaction_senders.LeaseTransactionSender.leaseTransactionSender;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.Constants.DEFAULT_FAUCET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class LeaseTransactionSubscriptionTest extends BaseTest {
    private Account sender;
    private String senderPublicKey;
    private String senderAddress;

    private Account recipient;
    private String recipientAddress;
    private String recipientPublicKeyHash;

    private int amountLease;
    private long amountAfter = DEFAULT_FAUCET - MIN_FEE;
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
                    recipientPublicKeyHash = Base58.encode(recipient.address().publicKeyHash());
                }
        );
    }

    @Test
    @DisplayName("Check subscription on lease min sum waves transaction")
    void subscribeTestForWavesLeaseTransaction() {
        leaseTransactionSender(MIN_TRANSACTION_SUM, sender, recipient, MIN_FEE, LATEST_VERSION);
        String leaseId = getLeaseTx().id().toString();
        height = node().getHeight();
        subscribeResponseHandler(channel, sender, height, height);
        checkLeaseSubscribe(leaseId, MIN_TRANSACTION_SUM, DEFAULT_FAUCET, MIN_FEE);
    }

    @Test
    @DisplayName("Check subscription on lease transaction in smartAcc")
    void subscribeTestForWavesLeaseTransactionDAppAcc() {
        long balance = accWithDApp.getWavesBalance();
        amountAfter = balance - SUM_FEE;
        senderAddress = accWithDApp.address().toString();
        senderPublicKey = accWithDApp.publicKey().toString();

        leaseTransactionSender(amountLease, accWithDApp, recipient, SUM_FEE, LATEST_VERSION);
        String leaseId = getLeaseTx().id().toString();
        height = node().getHeight();
        subscribeResponseHandler(channel, accWithDApp, height, height);
        checkLeaseSubscribe(leaseId, amountLease, balance, SUM_FEE);
    }

    private void checkLeaseSubscribe(String leaseId, long leaseSum, long balanceBefore, long fee) {
        assertAll(
                // transaction
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(senderPublicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(fee),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(LATEST_VERSION),
                // lease
                () -> assertThat(getLeaseTransactionPublicKeyHash(0)).isEqualTo(recipientPublicKeyHash),
                () -> assertThat(getLeaseAssetAmount(0)).isEqualTo(leaseSum),
                // transaction_ids
                () -> assertThat(getTransactionId()).isEqualTo(leaseId),
                // transactions_metadata
                () -> assertThat(getLeaseTransactionMetadata(0)).isEqualTo(recipientAddress),
                // balances sender
                () -> assertThat(getAddress(0, 0)).isEqualTo(senderAddress),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(balanceBefore),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(amountAfter),
                // leasing_for_address sender
                () -> assertThat(getAddressFromLeasingForAddress(0, 0)).isEqualTo(senderAddress),
                () -> assertThat(getOutAfterFromLeasingForAddress(0, 0)).isEqualTo(leaseSum),
                // leasing_for_address recipient
                () -> assertThat(getAddressFromLeasingForAddress(0, 1)).isEqualTo(recipientAddress),
                () -> assertThat(getInAfterFromLeasingForAddress(0, 1)).isEqualTo(leaseSum),
                // individual_leases
                () -> assertThat(getLeaseIdFromIndividualLeases(0, 0)).isEqualTo(leaseId),
                () -> assertThat(getStatusAfterFromIndividualLeases(0, 0)).isEqualTo(ACTIVE_STATUS_LEASE),
                () -> assertThat(getAmountFromIndividualLeases(0, 0)).isEqualTo(leaseSum),
                () -> assertThat(getSenderFromIndividualLeases(0, 0)).isEqualTo(senderPublicKey),
                () -> assertThat(getRecipientFromIndividualLeases(0, 0)).isEqualTo(recipientAddress),
                () -> assertThat(getOriginalTransactionIdFromIndividualLeases(0, 0)).isEqualTo(leaseId)
        );
    }
}
