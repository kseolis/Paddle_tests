package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.DataTransaction;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Calculations.getTransactionCommission;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.getAppend;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionVersion;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class DataTransactionSubscriptionTest extends BaseTest {
    private long amount;
    private int assetQuantity;
    private int assetDecimals;

    private String address;
    private String senderPublicKey;
    private Account senderAccount;

    private final long wavesAmountBeforeBurn = DEFAULT_FAUCET - ONE_WAVES;
    private final byte[] compileScript = node().compileScript(SCRIPT_PERMITTING_OPERATIONS).script().bytes();

    private Base64String binaryData;
    private int integerData;
    private boolean boolData;
    private String stringData;

    @BeforeEach
    void setUp() {
        async(
                () -> {
                    amount = getRandomInt(100, 10000000);
                    assetQuantity = getRandomInt(1000, 999_999_999);
                    assetDecimals = getRandomInt(0, 8);
                },
                () -> {
                    senderAccount = new Account(DEFAULT_FAUCET);
                    address = senderAccount.address().toString();
                    senderPublicKey = senderAccount.publicKey().toString();
                },
                () -> {
                    binaryData = new Base64String(randomNumAndLetterString(6));
                    integerData = getRandomInt(1, 1000000);
                    boolData = integerData % 2 == 0;
                    stringData = randomNumAndLetterString(6);
                }
        );
    }

    @Test
    @DisplayName("Check subscription on data smart asset transaction")
    void subscribeTestForDataTransaction() {
        final DataTransaction tx = senderAccount.writeData(i -> i
                .string("Str", stringData)
                .integer("Int", integerData)
                .bool("Bool", boolData)
                .binary("Bin", binaryData)).tx();
        height = node().getHeight();
        subscribeResponseHandler(channel, senderAccount, height, height);
        System.out.println(getAppend());
        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(senderPublicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(DataTransaction.LATEST_VERSION)
        );
    }
}
