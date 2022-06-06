package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.CreateAliasTransaction;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.getTransactionId;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.AliasTransaction.getAliasFromAliasTransaction;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.*;
import static im.mak.paddle.util.Constants.DEFAULT_FAUCET;
import static im.mak.paddle.util.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class AliasTransactionSubscriptionTest extends BaseTest {
    private final long amountAfter = DEFAULT_FAUCET - MIN_FEE;
    private String txId;
    private String address;
    private String publicKey;
    protected final String newAlias = randomNumAndLetterString(16);

    @BeforeEach
    void setUp() {
        Account account = new Account(DEFAULT_FAUCET);
        txId = account.createAlias(newAlias).tx().id().toString();
        height = node().getHeight();
        address = account.address().toString();
        publicKey = account.publicKey().toString();
        subscribeResponseHandler(channel, account, height, height);
    }

    @Test
    @DisplayName("Check subscription on alias transaction")
    void subscribeTestForCreateAlias() {
        assertAll(
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getAliasFromAliasTransaction(0)).isEqualTo(newAlias),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(CreateAliasTransaction.LATEST_VERSION),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE),
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(amountAfter),
                () -> assertThat(getTransactionId()).isEqualTo(txId)
        );
    }
}
