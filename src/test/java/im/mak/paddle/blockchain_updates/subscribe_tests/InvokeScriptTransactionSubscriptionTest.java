package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import im.mak.paddle.dapp.DAppCall;
import im.mak.paddle.dapps.AssetDAppAccount;
import im.mak.paddle.dapps.DataDApp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.wavesplatform.transactions.InvokeScriptTransaction.LATEST_VERSION;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.ConstructorRideFunctions.assetsFunctionBuilder;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.invokeTransactionMetadata.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.DataEntries.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.InvokeScriptTransactionHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionVersion;
import static im.mak.paddle.helpers.transaction_senders.InvokeScriptTransactionSender.*;
import static im.mak.paddle.token.Waves.WAVES;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class InvokeScriptTransactionSubscriptionTest extends BaseTest {
    private DAppCall dAppCall;
    private String dAppAccountAddress;
    private String dAppAccountPublicKey;
    private String dAppAccountPublicKeyHash;
    private String dAppFunctionName;
    private String dAppScript;
    private String dAppAssetId;
    private List<Amount> amounts = new ArrayList<>();

    private static final String args = "assetId:ByteVector";

    @Test
    @DisplayName("Check subscription on setScript transaction")
    void subscribeTestForSetScriptTransaction() {
        final Amount amount = WAVES.of(0.1);
        DataDApp dAppAccount = new DataDApp(DEFAULT_FAUCET, dAppScript);
        Account callerAccount = new Account(DEFAULT_FAUCET);
        dAppCall = dAppAccount.setData(getRandomInt(1, 1010), "lskajdh2398", true, "String");
        prepareInvoke(dAppAccount);
        setVersion(LATEST_VERSION);
        setFee(SUM_FEE);
        setExtraFee(ONE_WAVES);

        invokeSender(callerAccount, dAppAccount, dAppCall);
        height = node().getHeight();
        subscribeResponseHandler(channel, dAppAccount, height, height);
        System.out.println(getAppend());
        checkInvokeSubscribe(amount.value(), "ByteVector", dAppAssetId, ONE_WAVES + SUM_FEE);
    }

    @Test
    @DisplayName("subscribe Invoke Burn Transaction")
    void subscribeTestForInvokeTransaction() {
        Account callerAccount = new Account(DEFAULT_FAUCET);
        final Amount amount = Amount.of(0);
        final String functions = "Burn(assetId, 1),\n" +
                "\tBurn(issueAssetId, 1)";
        dAppScript = assetsFunctionBuilder(5, "unit", functions, args);
        final AssetDAppAccount dAppAccount = new AssetDAppAccount(DEFAULT_FAUCET, dAppScript);
        dAppAssetId = dAppAccount.issue(i -> i.name("outside Asset").quantity(900_000_000L))
                .tx().assetId().toString();
        dAppCall = dAppAccount.setDataAssetId(Base58.decode(dAppAssetId));
        prepareInvoke(dAppAccount);


        invokeSender(callerAccount, dAppAccount, dAppCall);
        height = node().getHeight();
        subscribeResponseHandler(channel, dAppAccount, height, height);
        System.out.println(getAppend());
        checkInvokeSubscribe(amount.value(), "ByteVector", dAppAssetId, ONE_WAVES + SUM_FEE);
    }

    private void checkInvokeSubscribe(long amount, String dAppKey, String dAppValue, long fee) {
        assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID);
        assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(dAppAccountPublicKey);
        assertThat(getTransactionFeeAmount(0)).isEqualTo(fee);
        assertThat(getTransactionVersion(0)).isEqualTo(LATEST_VERSION);
        assertThat(getTransactionId()).isEqualTo(getInvokeScriptId());

        assertThat(getInvokeTransactionPaymentAmount(0, 0)).isEqualTo(amount);
        assertThat(getInvokeTransactionPublicKeyHash(0)).isEqualTo(dAppAccountPublicKeyHash);
        assertThat(getInvokeMetadataDAppAddress(0)).isEqualTo(dAppAccountAddress);
        assertThat(getInvokeMetadataFunctionName(0)).isEqualTo(dAppFunctionName);
        assertThat(getInvokeMetadataResultDataKey(0, 0)).isEqualTo(dAppKey);
        assertThat(getInvokeMetadataArgStringValue(0, 0)).isEqualTo(dAppValue);
        // check waves account balance
        assertThat(getAddress(0, 0)).isEqualTo(dAppAccountAddress);
        assertThat(getAmountBefore(0, 0)).isEqualTo(getAccountWavesBalance());
        assertThat(getAmountAfter(0, 0)).isEqualTo(getBalanceAfterTransaction());
        // check waves dAppAccount balance
        assertThat(getAddress(0, 1)).isEqualTo(dAppAccountAddress);
//        assertThat(getAmountBefore(0, 1)).isEqualTo(getDAppAccountBalance());
        assertThat(getAmountAfter(0, 1)).isEqualTo(getDAppBalanceWavesAfterTransaction());
        // data entries
        assertThat(getSenderAddress(0, 0)).isEqualTo(dAppAccountAddress);
        assertThat(getTxKeyForStateUpdates(0, 0)).isEqualTo(dAppKey);
        assertThat(getTxStringValueForStateUpdates(0, 0)).isEqualTo(dAppValue);

        assertThat(getBeforeDataEntriesKey(0, 0)).isEqualTo(dAppKey);
        assertThat(getBeforeDataEntriesStringValue(0, 0)).isEqualTo(dAppValue);
    }

    private void prepareInvoke(Account dAppAccount) {
        dAppAccountPublicKey = dAppAccount.publicKey().toString();
        dAppAccountPublicKeyHash = Base58.encode(dAppAccount.address().publicKeyHash());
        dAppAccountAddress = dAppAccount.address().toString();
        dAppFunctionName = dAppCall.getFunction().name();
    }
}
/*
*
        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(SUM_FEE),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(LATEST_VERSION),
                () -> assertThat(getTransactionId()).isEqualTo(getInvokeScriptId()),

                () -> assertThat(getInvokeTransactionAmount(0, 0)).isEqualTo(amount),
                () -> assertThat(getInvokeTransactionPublicKeyHash(0)).isEqualTo(accWithDAppPublicKeyHash),
                () -> assertThat(getInvokeMetadataDAppAddress(0)).isEqualTo(accWithDAppAddress),
                () -> assertThat(getInvokeMetadataFunctionName(0)).isEqualTo(accWithDAppFunctionName),
                () -> assertThat(getInvokeMetadataResultDataKey(0, 0)).isEqualTo(dAppKey),
                () -> assertThat(getInvokeMetadataArgStringValue(0, 0)).isEqualTo(dAppValue),
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
                () -> assertThat(getTxStringValueForStateUpdates(0, 0)).isEqualTo(dAppValue),

                () -> assertThat(getBeforeDataEntriesKey(0, 0)).isEqualTo(dAppKey),
                () -> assertThat(getBeforeDataEntriesStringValue(0, 0)).isEqualTo(dAppBeforeValue)
        );*/