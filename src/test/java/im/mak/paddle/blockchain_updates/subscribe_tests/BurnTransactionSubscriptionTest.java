package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.BurnTransaction;
import com.wavesplatform.transactions.IssueTransaction;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.getTransactionId;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Assets.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Assets.getDecimalsAfter;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Balances.getAmountAfter;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.BurnTransaction.getBurnAssetAmount;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.BurnTransaction.getBurnAssetId;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.getTransactionVersion;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.Constants.MIN_FEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class BurnTransactionSubscriptionTest extends BaseTest {
    private long amount;
    private int assetQuantity;
    private int assetDecimals;
    private String address;
    private String publicKey;
    private Account account;
    private String assetName;
    private String assetDescription;
    private long quantityAfterBurn;
    private final long burnSmartAssetFee = MIN_FEE + EXTRA_FEE;
    private final long wavesAmountBeforeBurn = DEFAULT_FAUCET - ONE_WAVES;
    private final long wavesAmountAfterBurn = wavesAmountBeforeBurn - burnSmartAssetFee;
    private final byte[] compileScript = node().compileScript(SCRIPT_PERMITTING_OPERATIONS).script().bytes();

    @BeforeEach
    void setUp() {
        async(
                () -> {
                    amount = getRandomInt(100, 10000000);
                    assetQuantity = getRandomInt(1000, 999_999_999);
                    assetDecimals = getRandomInt(0, 8);
                    quantityAfterBurn = assetQuantity - amount;
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
        final IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)
                .script(SCRIPT_PERMITTING_OPERATIONS)).tx();
        final BurnTransaction burnTx = account.burn(amount, issueTx.assetId()).tx();
        height = node().getHeight();
        final String assetId = issueTx.assetId().toString();

        subscribeResponseHandler(channel, account, height, height);
        assertAll(
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(burnSmartAssetFee),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(BurnTransaction.LATEST_VERSION),
                () -> assertThat(getBurnAssetId(0)).isEqualTo(assetId),
                () -> assertThat(getBurnAssetAmount(0)).isEqualTo(amount),
                () -> assertThat(getTransactionId()).isEqualTo(burnTx.id().toString()),
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
                () -> assertThat(getScriptBefore(0, 0)).isEqualTo(compileScript),
                // check asset after burn
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(quantityAfterBurn),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(true),
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getScriptAfter(0, 0)).isEqualTo(compileScript)
        );
    }

    @Test
    @DisplayName("Check subscription on burn asset transaction")
    void subscribeTestForBurnAssetTransaction() {
        final IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)).tx();
        final BurnTransaction burnTx = account.burn(amount, issueTx.assetId()).tx();
        final String assetId = issueTx.assetId().toString();
        height = node().getHeight();

        subscribeResponseHandler(channel, account, height, height);
        assertAll(
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(BurnTransaction.LATEST_VERSION),
                () -> assertThat(getBurnAssetId(0)).isEqualTo(assetId),
                () -> assertThat(getBurnAssetAmount(0)).isEqualTo(amount),
                () -> assertThat(getTransactionId()).isEqualTo(burnTx.id().toString()),
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
                // check asset after burn
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(quantityAfterBurn),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(true),
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals)
        );
    }
}
