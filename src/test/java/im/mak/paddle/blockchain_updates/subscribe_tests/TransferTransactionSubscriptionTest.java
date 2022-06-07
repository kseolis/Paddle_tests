package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.transactions.TransferTransaction;
import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.getTransactionId;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.TransactionMetadata.getTransferRecipientAddressFromTransactionMetadata;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Balances.getAmountAfter;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.TransferTransaction.*;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class TransferTransactionSubscriptionTest extends BaseTest {
    private String address;
    private String publicKey;
    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account(DEFAULT_FAUCET);
        address = account.address().toString();
        publicKey = account.publicKey().toString();
    }

    @Test
    @DisplayName("Check subscription on transfer transaction")
    void subscribeTestForTransferTransaction() {
        Account recipient = new Account();
        String recipientAddress = recipient.address().toString();

        Amount amount = Amount.of(getRandomInt(1, 10000));
        long amountVal = amount.value();
        long amountAfter = DEFAULT_FAUCET - MIN_FEE - amountVal;

        String recipientPublicKeyHash = Base58.encode(recipient.address().publicKeyHash());

        TransferTransaction tx = account.transfer(recipient, amount).tx();
        height = node().getHeight();
        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getTransferAssetAmount(0)).isEqualTo(amountVal),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(TransferTransaction.LATEST_VERSION),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE),
                () -> assertThat(getTransferAssetId(0)).isEqualTo(""),
                () -> assertThat(getTransferTransactionPublicKeyHash(0)).isEqualTo(recipientPublicKeyHash),
                () -> assertThat(getTransactionId()).isEqualTo(tx.id().toString()),
                // check sender balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(amountAfter),
                // check recipient balance
                () -> assertThat(getAddress(0, 1)).isEqualTo(recipientAddress),
                () -> assertThat(getAmountBefore(0, 1)).isEqualTo(0),
                () -> assertThat(getAmountAfter(0, 1)).isEqualTo(amountVal),
                // check recipient address
                () -> assertThat(getTransferRecipientAddressFromTransactionMetadata(0)).isEqualTo(recipientAddress)
        );
    }
}
