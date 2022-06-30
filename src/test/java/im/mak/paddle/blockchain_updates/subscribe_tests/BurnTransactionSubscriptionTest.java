package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.BurnTransaction.LATEST_VERSION;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.getTransactionId;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Assets.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Assets.getDecimalsAfter;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.getAmountAfter;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.BurnTransactionHandler.getBurnAssetAmount;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.BurnTransactionHandler.getBurnAssetId;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionVersion;
import static im.mak.paddle.helpers.transaction_senders.BurnTransactionSender.burnTransactionSender;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class BurnTransactionSubscriptionTest extends BaseTest {
    private int assetQuantity;
    private int assetDecimals;
    private String assetName;
    private String assetDescription;
    private String address;
    private String publicKey;
    private Account account;
    private long quantityAfterBurn;
    private long wavesAmountAfterBurn;
    private final long wavesAmountBeforeBurn = DEFAULT_FAUCET - ONE_WAVES;
    private final byte[] compileScript = node().compileScript(SCRIPT_PERMITTING_OPERATIONS).script().bytes();

    @BeforeEach
    void setUp() {
        async(
                () -> {
                    assetQuantity = getRandomInt(1000, 999_999_999);
                    assetDecimals = getRandomInt(0, 8);
                    assetName = getRandomInt(1, 900000) + "asset";
                    assetDescription = assetName + "test";
                },
                () -> {
                    account = new Account(DEFAULT_FAUCET);
                    address = account.address().toString();
                    publicKey = account.publicKey().toString();
                }
        );
    }

    @Test
    @DisplayName("Check subscription on burn smart asset transaction")
    void subscribeTestForBurnSmartAssetTransaction() {
        wavesAmountAfterBurn = wavesAmountBeforeBurn - SUM_FEE;
        final IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)
                .script(SCRIPT_PERMITTING_OPERATIONS)).tx();
        final AssetId assetId = issueTx.assetId();
        final Amount amount = Amount.of(getRandomInt(100, 10000), assetId);

        burnTransactionSender(account, amount, issueTx.assetId(), SUM_FEE, LATEST_VERSION);

        quantityAfterBurn = assetQuantity - amount.value();
        height = node().getHeight();

        subscribeResponseHandler(channel, account, height, height);
        checkBurnTransactionSubscription(assetId.toString(), amount.value(), SUM_FEE, compileScript);
    }

    @Test
    @DisplayName("Check subscription on burn asset transaction")
    void subscribeTestForBurnAssetTransaction() {
        wavesAmountAfterBurn = wavesAmountBeforeBurn - MIN_FEE;
        byte[] script = new byte[0];
        final IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)).tx();
        final AssetId assetId = issueTx.assetId();
        final Amount amount = Amount.of(getRandomInt(100, 10000000), assetId);

        burnTransactionSender(account, amount, issueTx.assetId(), MIN_FEE, LATEST_VERSION);

        quantityAfterBurn = assetQuantity - amount.value();
        height = node().getHeight();

        subscribeResponseHandler(channel, account, height, height);
        checkBurnTransactionSubscription(assetId.toString(), amount.value(), MIN_FEE, script);
    }

    private void checkBurnTransactionSubscription(String assetId, long amount, long burnAssetFee, byte[] script) {
        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(burnAssetFee),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(LATEST_VERSION),
                () -> assertThat(getBurnAssetId(0)).isEqualTo(assetId),
                () -> assertThat(getBurnAssetAmount(0)).isEqualTo(amount),
                () -> assertThat(getTransactionId()).isEqualTo(getTransactionId()),
                // check waves balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(wavesAmountBeforeBurn),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(wavesAmountAfterBurn),
                // check asset balance
                () -> assertThat(getAddress(0, 1)).isEqualTo(address),
                () -> assertThat(getIssuedAssetIdAmountAfter(0, 1)).isEqualTo(assetId),
                () -> assertThat(getAmountBefore(0, 1)).isEqualTo(assetQuantity),
                () -> assertThat(getAmountAfter(0, 1)).isEqualTo(quantityAfterBurn),
                // check asset before burn
                () -> assertThat(getAssetIdFromAssetBefore(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerBefore(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityBefore(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getReissuableBefore(0, 0)).isEqualTo(true),
                () -> assertThat(getNameBefore(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionBefore(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsBefore(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getScriptBefore(0, 0)).isEqualTo(script),
                // check asset after burn
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(quantityAfterBurn),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(true),
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getScriptAfter(0, 0)).isEqualTo(script)
        );
    }
}
