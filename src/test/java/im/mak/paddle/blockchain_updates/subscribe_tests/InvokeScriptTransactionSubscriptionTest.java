package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import im.mak.paddle.dapps.IntDApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.InvokeScriptTransaction.LATEST_VERSION;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.invokeTransactionMetadata.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.DataEntries.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.InvokeScriptTransactionHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionVersion;
import static im.mak.paddle.helpers.transaction_senders.InvokeScriptTransactionSender.*;
import static im.mak.paddle.token.Waves.WAVES;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class InvokeScriptTransactionSubscriptionTest extends BaseTest {
    private Account account;
    private String address;
    private String publicKey;

    private static IntDApp accWithDApp;
    private String accWithDAppPublicKeyHash;
    private static String accWithDAppAddress;
    private static String accWithDAppFunctionName;

    private final Base64String script = node()
            .compileScript(fromFile("ride_scripts/permissionOnUpdatingKeyValues.ride")).script();

    @BeforeEach
    void setUp() {
        async(
                () -> {
                    account = new Account(DEFAULT_FAUCET);
                    address = account.address().toString();
                    publicKey = account.publicKey().toString();
                },
                () -> {
                    accWithDApp = new IntDApp(DEFAULT_FAUCET);
                    accWithDAppPublicKeyHash = Base58.encode(accWithDApp.address().publicKeyHash());
                    accWithDAppAddress = accWithDApp.address().toString();
                }
        );
    }

    @Test
    @DisplayName("Check subscription on setScript transaction")
    void subscribeTestForSetScriptTransaction() {
        Amount amount = WAVES.of(0.1);
        int dAppBeforeValue = 0;
        for (int v = 1; v <= LATEST_VERSION; v++) {
            invokeIntDAppSender(account, accWithDApp, amount, v, SUM_FEE);
            accWithDAppFunctionName = getDAppCall().getFunction().name();
            Long dAppValue = (Long) getInvokeScriptTx().function().args().get(0).valueAsObject();
            String dAppKey = "int";

            height = node().getHeight();
            subscribeResponseHandler(channel, account, height, height);

            checkInvokeSubscribe(v, amount.value(), dAppKey, dAppValue.intValue(), dAppBeforeValue);
            dAppBeforeValue = dAppValue.intValue();
        }
    }

    private void checkInvokeSubscribe(int version, long amount, String dAppKey, int dAppValue, int dAppBeforeValue) {
        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(SUM_FEE),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(version),
                () -> assertThat(getTransactionId()).isEqualTo(getInvokeScriptId()),

                () -> assertThat(getInvokeTransactionAmount(0, 0)).isEqualTo(amount),
                () -> assertThat(getInvokeTransactionPublicKeyHash(0)).isEqualTo(accWithDAppPublicKeyHash),
                () -> assertThat(getInvokeMetadataDAppAddress(0)).isEqualTo(accWithDAppAddress),
                () -> assertThat(getInvokeMetadataFunctionName(0)).isEqualTo(accWithDAppFunctionName),
                () -> assertThat(getInvokeMetadataResultDataKey(0, 0)).isEqualTo(dAppKey),
                () -> assertThat(getInvokeMetadataArgIntegerValue(0, 0)).isEqualTo(dAppValue),
                // check waves account balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(getAccountWavesBalance()),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(getBalanceAfterTransaction()),
                // check waves dAppAccount balance
                () -> assertThat(getAddress(0, 1)).isEqualTo(accWithDAppAddress),
                () -> assertThat(getAmountBefore(0, 1)).isEqualTo(getDAppAccountBalance()),
                () -> assertThat(getAmountAfter(0, 1)).isEqualTo(getDAppAccountBalanceAfterTransaction()),
                // data entries
                () -> assertThat(getSenderAddress(0, 0)).isEqualTo(accWithDAppAddress),
                () -> assertThat(getTxKeyForStateUpdates(0, 0)).isEqualTo(dAppKey),
                () -> assertThat(getTxIntValueForStateUpdates(0, 0)).isEqualTo(dAppValue),

                () -> assertThat(getBeforeDataEntriesKey(0, 0)).isEqualTo(dAppKey),
                () -> assertThat(getBeforeDataEntriesIntegerValue(0, 0)).isEqualTo(dAppBeforeValue)
        );
    }
}
