package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.TransferTransaction.LATEST_VERSION;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.TransactionMetadataHandler.getTransferRecipientAddressFromTransactionMetadata;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransferTransactionHandler.*;
import static im.mak.paddle.helpers.transaction_senders.TransferTransactionSender.getTransferTx;
import static im.mak.paddle.helpers.transaction_senders.TransferTransactionSender.transferTransactionSender;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class TransferTransactionSubscriptionTest extends BaseTest {
    private Account senderAccount;
    private String sender;
    private String senderPublicKey;

    private Account recipient;
    private String recipientAddress;
    private String recipientPublicKeyHash;

    private Amount amountTransfer;
    private long amountValue;
    private long amountBefore;
    private long amountAfter;

    @BeforeEach
    void setUp() {
        async(
                () -> {
                    senderAccount = new Account(DEFAULT_FAUCET);
                    sender = senderAccount.address().toString();
                    senderPublicKey = senderAccount.publicKey().toString();
                },
                () -> {
                    recipient = new Account();
                    recipientAddress = recipient.address().toString();
                    recipientPublicKeyHash = Base58.encode(recipient.address().publicKeyHash());
                },
                () -> {
                    amountTransfer = Amount.of(getRandomInt(1, 10000));
                    amountValue = amountTransfer.value();
                    amountAfter = DEFAULT_FAUCET - MIN_FEE - amountValue;
                }
        );
    }

    @Test
    @DisplayName("Check subscription on transfer transaction")
    void subscribeTestForTransferTransaction() {
        amountBefore = DEFAULT_FAUCET;
        transferTransactionSender(amountTransfer, senderAccount, recipient, ADDRESS, MIN_FEE, LATEST_VERSION);
        height = node().getHeight();
        subscribeResponseHandler(channel, senderAccount, height, height);
        System.out.println(getAppend());
        checkTransferSubscribe("", DEFAULT_FAUCET, 0, MIN_FEE);
    }

    @Test
    @DisplayName("Check subscription on transfer transaction issue asset")
    void subscribeTestForTransferTransactionIssueAsset() {
        IssueTransaction issuedAsset = senderAccount.issue(i -> i.name("Test_Asset").quantity(1000)).tx();
        AssetId assetId = issuedAsset.assetId();
        amountBefore = senderAccount.getWavesBalance();
        amountAfter = amountBefore - MIN_FEE;
        amountTransfer = Amount.of(getRandomInt(1, 1000), issuedAsset.assetId());
        amountValue = amountTransfer.value();

        long assetAmount = issuedAsset.quantity() - amountTransfer.value();

        transferTransactionSender(amountTransfer, senderAccount, recipient, ADDRESS, MIN_FEE, LATEST_VERSION);
        height = node().getHeight();
        subscribeResponseHandler(channel, senderAccount, height, height);

        checkTransferSubscribe(assetId.toString(), issuedAsset.quantity(), assetAmount, MIN_FEE);
    }
    @Test
    @DisplayName("Check subscription on transfer transaction issue smart asset")
    void subscribeTestForTransferTransactionIssueSmartAsset() {
        IssueTransaction issuedAsset = senderAccount.issue(i -> i
                .name("Test_Asset")
                .quantity(1000)
                .script(SCRIPT_PERMITTING_OPERATIONS)).tx();
        AssetId assetId = issuedAsset.assetId();
        amountBefore = senderAccount.getWavesBalance();
        amountAfter = amountBefore - SUM_FEE;
        amountTransfer = Amount.of(getRandomInt(1, 1000), issuedAsset.assetId());
        amountValue = amountTransfer.value();

        long assetAmount = issuedAsset.quantity() - amountTransfer.value();

        transferTransactionSender(amountTransfer, senderAccount, recipient, ADDRESS, SUM_FEE, LATEST_VERSION);
        height = node().getHeight();
        subscribeResponseHandler(channel, senderAccount, height, height);

        checkTransferSubscribe(assetId.toString(), issuedAsset.quantity(), assetAmount, SUM_FEE);
    }

    private void checkTransferSubscribe(String assetId, long quantity, long amountSecond, long fee) {
        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getTransferAssetAmount(0)).isEqualTo(amountValue),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(senderPublicKey),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(LATEST_VERSION),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(fee),
                () -> assertThat(getTransferAssetId(0)).isEqualTo(assetId),
                () -> assertThat(getTransferTransactionPublicKeyHash(0)).isEqualTo(recipientPublicKeyHash),
                () -> assertThat(getTransactionId()).isEqualTo(getTransferTx().id().toString()),
                // check sender WAVES balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(sender),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(amountBefore),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(amountAfter),
                // check recipient address
                () -> assertThat(getTransferRecipientAddressFromTransactionMetadata(0)).isEqualTo(recipientAddress)
        );
        if (assetId.equals("")) {
            // check recipient balance
            assertAll(
                    () -> assertThat(getAddress(0, 1)).isEqualTo(recipientAddress),
                    () -> assertThat(getAmountBefore(0, 1)).isEqualTo(0),
                    () -> assertThat(getAmountAfter(0, 1)).isEqualTo(amountValue)
            );
        } else {
            assertAll(
                    // check sender asset balance
                    () -> assertThat(getAddress(0, 1)).isEqualTo(sender),
                    () -> assertThat(getAmountBefore(0, 1)).isEqualTo(quantity),
                    () -> assertThat(getAmountAfter(0, 1)).isEqualTo(amountSecond),
                    // check recipient balance
                    () -> assertThat(getAddress(0, 2)).isEqualTo(recipientAddress),
                    () -> assertThat(getAmountBefore(0, 2)).isEqualTo(0),
                    () -> assertThat(getAmountAfter(0, 2)).isEqualTo(amountValue)
            );
        }
    }
}
