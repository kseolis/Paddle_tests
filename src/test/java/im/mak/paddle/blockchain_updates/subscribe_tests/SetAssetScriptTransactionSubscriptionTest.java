package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.SetAssetScriptTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
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
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Assets.getScriptComplexityAfter;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.SetAssetScriptTransaction.getAssetIdFromSetAssetScript;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.SetAssetScriptTransaction.getScriptFromSetAssetScript;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SetAssetScriptTransactionSubscriptionTest extends BaseTest {
    private int assetQuantity;
    private int assetDecimals;
    private String address;
    private String publicKey;
    private Account account;
    private String assetName;
    private String assetDescription;
    private final long wavesAmountBeforeSetAssetScript = DEFAULT_FAUCET - ONE_WAVES;
    private final long wavesAmountAfterSetAssetScript = wavesAmountBeforeSetAssetScript - ONE_WAVES;
    private final byte[] firstScript = node().compileScript(SCRIPT_PERMITTING_OPERATIONS).script().bytes();
    private final Base64String newScript = node()
            .compileScript(fromFile("ride_scripts/permissionOnUpdatingKeyValues.ride")).script();

    @BeforeEach
    void setUp() {
        async(
                () -> assetQuantity = getRandomInt(1000, 999_999_999),
                () -> assetDecimals = getRandomInt(0, 8),
                () -> assetQuantity = getRandomInt(1000, 999_999_999),
                () -> {
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
    @DisplayName("Check subscription on setAssetScript smart asset transaction")
    void subscribeTestForSetAssetScriptTransaction() {

        IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)
                .script(SCRIPT_PERMITTING_OPERATIONS)).tx();
        AssetId assetId = issueTx.assetId();
        String assetIdToString = assetId.toString();

        SetAssetScriptTransaction setAssetScriptTx = account.setAssetScript(assetId, newScript).tx();

        height = node().getHeight();

        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(ONE_WAVES),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(SetAssetScriptTransaction.LATEST_VERSION),
                () -> assertThat(getAssetIdFromSetAssetScript(0)).isEqualTo(assetIdToString),
                () -> assertThat(getScriptFromSetAssetScript(0)).isEqualTo(newScript.bytes()),
                () -> assertThat(getTransactionId()).isEqualTo(setAssetScriptTx.id().toString()),
                // check waves balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(wavesAmountBeforeSetAssetScript),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(wavesAmountAfterSetAssetScript),
                // check asset before set asset script
                () -> assertThat(getAssetIdFromAssetBefore(0, 0)).isEqualTo(assetIdToString),
                () -> assertThat(getIssuerBefore(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityBefore(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getReissuableBefore(0, 0)).isEqualTo(true),
                () -> assertThat(getNameBefore(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionBefore(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsBefore(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getScriptBefore(0, 0)).isEqualTo(firstScript),
                () -> assertThat(getScriptComplexityBefore(0, 0)).isEqualTo(0),
                // check asset after set asset script
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetIdToString),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(true),
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getScriptAfter(0, 0)).isEqualTo(newScript.bytes()),
                () -> assertThat(getScriptComplexityAfter(0, 0)).isEqualTo(15)
        );
    }
}
