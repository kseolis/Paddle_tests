package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.transactions.MassTransferTransaction;
import com.wavesplatform.transactions.account.PublicKey;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;
import com.wavesplatform.transactions.mass.Transfer;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Calculations.calculateSenderBalanceAfterMassTransfer;
import static im.mak.paddle.helpers.Calculations.getTransactionCommission;
import static im.mak.paddle.helpers.Randomizer.accountListGenerator;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.TransactionMetadataHandler.getMassTransferFromTransactionMetadata;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.MassTransferTransactionHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionFeeAmount;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MassTransferTransactionSubscriptionTest extends BaseTest {
    private Account senderAccount;
    private String senderAddress;
    private PublicKey senderPublicKey;

    private Amount amount;
    private long amountValue;
    private long senderAmountAfter;

    private static Base58String base58StringAttachment;
    private static List<Account> accountList;

    @BeforeEach
    void setUp() {
        async(
                () -> base58StringAttachment = new Base58String("attachment"),
                () -> {
                    senderAccount = new Account(DEFAULT_FAUCET);
                    senderAddress = senderAccount.address().toString();
                    senderPublicKey = senderAccount.publicKey();
                },
                () -> {
                    amount = Amount.of(getRandomInt(10_000, 1_000_000));
                    amountValue = amount.value();
                }
        );
    }

    @Test
    @DisplayName("Check subscription on transfer transaction")
    void subscribeTestForMassTransferTransaction() {
        accountList = accountListGenerator(MAX_NUM_ACCOUNT_FOR_MASS_TRANSFER);
        List<Transfer> transfers = new ArrayList<>();
        senderAmountAfter = calculateSenderBalanceAfterMassTransfer
                (senderAccount, AssetId.WAVES, amountValue, MAX_NUM_ACCOUNT_FOR_MASS_TRANSFER);
        accountList.forEach(a -> transfers.add(Transfer.to(a.address(), amountValue)));

        senderAccount.massTransfer(i -> i.transfers(transfers).attachment(base58StringAttachment));

        height = node().getHeight();

        subscribeResponseHandler(channel, senderAccount, height, height);

        checkMassTransferOnSubscribe();
    }

    private void checkMassTransferOnSubscribe() {
        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(senderPublicKey.toString()),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(getTransactionCommission()),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(MassTransferTransaction.LATEST_VERSION),
                () -> assertThat(getAttachmentFromMassTransfer(0)).isEqualTo(base58StringAttachment.toString()),
                () -> assertThat(getAssetIdFromMassTransfer(0)).isEqualTo("")
        );

        for (int i = 0; i < MAX_NUM_ACCOUNT_FOR_MASS_TRANSFER; i++) {
            for (int j = 0; j < MAX_NUM_ACCOUNT_FOR_MASS_TRANSFER; j++) {
                if (getAccountAddressFromList(j).equals(getAddress(0, i))) {
                    assertThat(getAmountBefore(0, i)).isEqualTo(0);
                    assertThat(getAmountAfter(0, i)).isEqualTo(amountValue);
                } else if (senderAddress.equals(getAddress(0, i))) {
                    assertThat(getAmountBefore(0, i)).isEqualTo(DEFAULT_FAUCET);
                    assertThat(getAmountAfter(0, i)).isEqualTo(senderAmountAfter);
                }
            }
        }

        for (int i = 0; i < MAX_NUM_ACCOUNT_FOR_MASS_TRANSFER; i++) {
            assertThat(getRecipientAmountFromMassTransfer(0, i)).isEqualTo(amountValue);
            assertThat(getRecipientPublicKeyHashFromMassTransfer(0, i)).isEqualTo(getPublicKeyHashFromList(i));
            assertThat(getMassTransferFromTransactionMetadata(0, i)).isEqualTo(getAccountAddressFromList(i));
        }
    }

    private String getPublicKeyHashFromList(int index) {
        return Base58.encode(accountList.get(index).address().publicKeyHash());
    }

    private String getAccountAddressFromList(int index) {
        return accountList.get(index).address().toString();
    }
}
