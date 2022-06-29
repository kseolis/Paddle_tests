package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.IssueTransaction;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.getFirstTransaction;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.getTransactionId;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Assets.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Assets.getScriptAfter;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.getAmountAfter;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.IssueTransactionHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getChainId;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getSenderPublicKeyFromTransaction;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.Constants.DEFAULT_FAUCET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class IssueTransactionSubscriptionTest extends BaseTest {
    private int assetDecimals;
    private String address;
    private String publicKey;
    private Account account;
    private String assetName;
    private String assetDescription;
    private long fee = ONE_WAVES;
    private byte[] compileScript = node().compileScript(SCRIPT_PERMITTING_OPERATIONS).script().bytes();
    private long amountAfter = DEFAULT_FAUCET - ONE_WAVES;

    @BeforeEach
    void setUp() {
        assetDecimals = getRandomInt(0, 8);
        account = new Account(DEFAULT_FAUCET);
        address = account.address().toString();
        publicKey = account.publicKey().toString();
        assetName = getRandomInt(1, 900000) + "asset";
        assetDescription = assetName + "test";
    }

    @Test
    @DisplayName("Check subscription on issue smart asset transaction")
    void subscribeTestForIssueSmartAssetTransaction() {
        final int assetQuantity = getRandomInt(1000, 999_999_999);
        final boolean reissuable = true;
        IssueTransaction tx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(reissuable)
                .script(SCRIPT_PERMITTING_OPERATIONS)).tx();
        height = node().getHeight();

        final String assetId = tx.assetId().toString();
        subscribeResponseHandler(channel, account, height, height);
        checkIssueTransactionSubscription(tx, assetId, assetQuantity, reissuable);
    }

    @Test
    @DisplayName("Check subscription on issue transaction")
    void subscribeTestForIssueTransaction() {
        final int assetQuantity = getRandomInt(1000, 999_999_999);
        compileScript = new byte[0];
        final boolean reissuable = false;
        IssueTransaction tx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(reissuable)).tx();
        height = node().getHeight();

        final String assetId = tx.assetId().toString();
        subscribeResponseHandler(channel, account, height, height);
        checkIssueTransactionSubscription(tx, assetId, assetQuantity, reissuable);
    }

    @Test
    @DisplayName("Check subscription on issue transaction")
    void subscribeTestForIssueNftTransaction() {
        compileScript = new byte[0];
        fee = MIN_FEE;
        amountAfter = DEFAULT_FAUCET - fee;
        assetDecimals = 0;
        IssueTransaction tx = account.issueNft(i -> i.name(assetName).description(assetDescription)).tx();
        height = node().getHeight();

        final String assetId = tx.assetId().toString();
        subscribeResponseHandler(channel, account, height, height);
        checkIssueTransactionSubscription(tx, assetId, 1, false);
    }

    private void checkIssueTransactionSubscription
            (IssueTransaction tx, String assetId, int assetQuantity, boolean reissuable) {
        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getAssetName(0)).isEqualTo(assetName),
                () -> assertThat(getAssetDescription(0)).isEqualTo(assetDescription),
                () -> assertThat(getAssetAmount(0)).isEqualTo(assetQuantity),
                () -> assertThat(getAssetReissuable(0)).isEqualTo(reissuable),
                () -> assertThat(getAssetDecimals(0)).isEqualTo(assetDecimals),
                () -> assertThat(getAssetScript(0)).isEqualTo(compileScript),
                () -> assertThat(getFirstTransaction().getVersion()).isEqualTo(IssueTransaction.LATEST_VERSION),
                () -> assertThat(getFirstTransaction().getFee().getAmount()).isEqualTo(fee),
                () -> assertThat(getTransactionId()).isEqualTo(tx.id().toString()),
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                // check waves balance from balances
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(amountAfter),
                // check assetId and balance from balances
                () -> assertThat(getIssuedAssetIdAmountAfter(0, 1)).isEqualTo(assetId),
                () -> assertThat(getAmountBefore(0, 1)).isEqualTo(0),
                () -> assertThat(getAmountAfter(0, 1)).isEqualTo(assetQuantity),
                // check from assets
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                // check asset info
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(reissuable),
                () -> assertThat(getScriptAfter(0, 0)).isEqualTo(compileScript)
        );
    }
}
