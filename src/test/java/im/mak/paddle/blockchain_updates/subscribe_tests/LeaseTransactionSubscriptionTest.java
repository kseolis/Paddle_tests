package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.transactions.LeaseTransaction;
import com.wavesplatform.transactions.account.PrivateKey;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.getAppend;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.getTransactionId;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.TransactionMetadata.getLeaseTransactionMetadata;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Leasing.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.LeaseTransaction.getLeaseAssetAmount;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.LeaseTransaction.getLeaseTransactionPublicKeyHash;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.Constants.DEFAULT_FAUCET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class LeaseTransactionSubscriptionTest extends BaseTest {

    private Account sender;
    private PrivateKey senderPrivateKey;
    private String senderPublicKey;
    private String senderAddress;

    private Account recipient;
    private PrivateKey recipientPrivateKey;
    private String recipientPublicKey;
    private String recipientAddress;
    private String recipientPublicKeyHash;

    private int amount;
    private final long amountAfter = DEFAULT_FAUCET - MIN_FEE;

    @BeforeEach
    void setUp() {
        async(
                () -> amount = getRandomInt(100_000, 1_00_000_000),
                () -> {
                    sender = new Account(DEFAULT_FAUCET);
                    senderPrivateKey = sender.privateKey();
                    senderAddress = sender.address().toString();
                    senderPublicKey = sender.publicKey().toString();
                },
                () -> {
                    recipient = new Account(DEFAULT_FAUCET);
                    recipientPrivateKey = recipient.privateKey();
                    recipientPublicKey = recipient.publicKey().toString();
                    recipientAddress = recipient.address().toString();
                    recipientPublicKeyHash = Base58.encode(recipient.address().publicKeyHash());
                }
        );
    }

    @Test
    @DisplayName("Check subscription on lease waves transaction")
    void subscribeTestForWavesLeaseTransaction() {
        final LeaseTransaction leaseTx = sender.lease(recipient, amount).tx();
        final String leaseId = leaseTx.id().toString();
        height = node().getHeight();
        subscribeResponseHandler(channel, sender, height, height);

        System.out.println(getAppend());
        assertAll(
                // transaction
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(senderPublicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(LeaseTransaction.LATEST_VERSION),
                // lease
                () -> assertThat(getLeaseTransactionPublicKeyHash(0)).isEqualTo(recipientPublicKeyHash),
                () -> assertThat(getLeaseAssetAmount(0)).isEqualTo(amount),
                // transaction_ids
                () -> assertThat(getTransactionId()).isEqualTo(leaseTx.id().toString()),
                // transactions_metadata
                () -> assertThat(getLeaseTransactionMetadata(0)).isEqualTo(recipientAddress),
                // balances sender
                () -> assertThat(getAddress(0, 0)).isEqualTo(senderAddress),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(amountAfter),
                // leasing_for_address sender
                () -> assertThat(getAddressFromLeasingForAddress(0, 0)).isEqualTo(senderAddress),
                () -> assertThat(getOutAfterFromLeasingForAddress(0, 0)).isEqualTo(amount),
                // leasing_for_address recipient
                () -> assertThat(getAddressFromLeasingForAddress(0, 1)).isEqualTo(recipientAddress),
                () -> assertThat(getInAfterFromLeasingForAddress(0, 1)).isEqualTo(amount),
                // individual_leases
                () -> assertThat(getLeaseIdFromIndividualLeases(0, 0)).isEqualTo(leaseId),
                () -> assertThat(getStatusAfterFromIndividualLeases(0, 0)).isEqualTo("ACTIVE"),
                () -> assertThat(getAmountFromIndividualLeases(0, 0)).isEqualTo(amount),
                () -> assertThat(getSenderFromIndividualLeases(0, 0)).isEqualTo(senderPublicKey),
                () -> assertThat(getRecipientFromIndividualLeases(0, 0)).isEqualTo(recipientAddress),
                () -> assertThat(getOriginalTransactionIdFromIndividualLeases(0, 0)).isEqualTo(leaseId)
        );
    }
}
