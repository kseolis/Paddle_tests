package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.transactions.DataTransaction;
import com.wavesplatform.transactions.common.Base64String;
import com.wavesplatform.transactions.data.BinaryEntry;
import com.wavesplatform.transactions.data.BooleanEntry;
import com.wavesplatform.transactions.data.IntegerEntry;
import com.wavesplatform.transactions.data.StringEntry;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.TransactionMetadataHandler.getElementTransactionMetadata;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.DataEntries.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.DataTransactionHandler.getKeyFromDataTx;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.DataTransactionHandler.getIntValueFromDataTx;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionVersion;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.DataTransactionsSender.dataEntryTransactionSender;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class DataTransactionSubscriptionTest extends BaseTest {
    private String senderAddress;
    private String senderPublicKey;
    private Account senderAccount;

    private final long wavesAmountBeforeBurn = DEFAULT_FAUCET - ONE_WAVES;
    private final byte[] compileScript = node().compileScript(SCRIPT_PERMITTING_OPERATIONS).script().bytes();

    private final Base64String base64String = new Base64String(randomNumAndLetterString(6));

    private final BinaryEntry binaryEntry = BinaryEntry.as("BinEntry", base64String);
    private final BooleanEntry booleanEntry = BooleanEntry.as("Boolean", true);
    private final IntegerEntry integerEntry = IntegerEntry.as("Integer", getRandomInt(100, 100000));
    private final StringEntry stringEntry = StringEntry.as("String", "string");

    @BeforeEach
    void setUp() {
        async(
                () -> {
                    senderAccount = new Account(DEFAULT_FAUCET);
                    senderAddress = senderAccount.address().toString();
                    senderPublicKey = senderAccount.publicKey().toString();
                }
        );
    }

    @Test
    @DisplayName("Check subscription on data smart asset transaction")
    void subscribeTestForDataTransaction() {
        dataEntryTransactionSender(senderAccount, DataTransaction.LATEST_VERSION, integerEntry);
        height = node().getHeight();
        subscribeResponseHandler(channel, senderAccount, height, height);

        System.out.println(getAppend());

        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(senderPublicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(DataTransaction.LATEST_VERSION),
                () -> assertThat(getTransactionId()).isEqualTo(getTxInfo().tx().id().toString()),

                () -> assertThat(getKeyFromDataTx(0, 0)).isEqualTo(integerEntry.key()),
                () -> assertThat(getIntValueFromDataTx(0, 0)).isEqualTo(integerEntry.value()),
                // check waves balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(senderAddress),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(DEFAULT_FAUCET - MIN_FEE),

                // before data transaction
                () -> assertThat(getBeforeDataEntries(0, 0).getKey()).isEqualTo(integerEntry.key()),
                () -> assertThat(getBeforeDataEntries(0, 0).getIntValue()).isEqualTo(0),
                // after data transaction
                () -> assertThat(getSenderAddress(0, 0)).isEqualTo(senderAddress),
                () -> assertThat(getTxKeyForStateUpdates(0, 0)).isEqualTo(integerEntry.key()),
                () -> assertThat(getTxIntValueForStateUpdates(0,0)).isEqualTo(integerEntry.value())
        );
    }
}
