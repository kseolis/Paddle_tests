package im.mak.paddle.e2e.transactions;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.dapp.DAppCall;
import im.mak.paddle.dapps.AssetDAppAccount;
import im.mak.paddle.dapps.DataDApp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.InvokeScriptTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.helpers.ConstructorRideFunctions.assetsFunctionBuilder;
import static im.mak.paddle.helpers.ConstructorRideFunctions.defaultFunctionBuilder;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getBalanceAfterTransaction;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.InvokeScriptTransactionSender.*;
import static im.mak.paddle.token.Waves.WAVES;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class InvokeScriptTransactionTest {
    private static Account account;
    private static byte[] accountAddress;
    private static int intArg;
    private static String binArg;
    private static boolean boolArg;
    private static String stringArg;
    private static final String args = "assetId:ByteVector";

    @BeforeAll
    static void before() {
        async(
                () -> {
                    account = new Account(DEFAULT_FAUCET);
                    accountAddress = Base58.decode(account.address().toString());
                },
                () -> binArg = randomNumAndLetterString(10),
                () -> stringArg = randomNumAndLetterString(10),
                () -> {
                    intArg = getRandomInt(1, 999999);
                    boolArg = intArg % 2 == 0;
                }
        );
    }

    @Test
    @DisplayName("invoke transaction with DataDApp")
    void invokeScriptWithDataDAppTest() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            final String functionArgs = "intVal:Int, binVal:ByteVector, boolVal:Boolean, strVal:String";
            final String functions = "\tIntegerEntry(\"int\", intVal),\n" +
                    "\tBinaryEntry(\"byte\", binVal),\n" +
                    "\tBooleanEntry(\"bool\", boolVal),\n" +
                    "\tStringEntry(\"str\", strVal)";
            final String script = defaultFunctionBuilder(functionArgs, functions, getRandomInt(4, MAX_LIB_VERSION));
            final DataDApp dAppAccount = new DataDApp(DEFAULT_FAUCET, script);

            DAppCall dAppCall = dAppAccount.setData(intArg, binArg, boolArg, stringArg);
            Amount amount = WAVES.of(0.1);
            invokeSenderWithPayment(account, dAppAccount, dAppCall, amount, v, SUM_FEE);
            checkAssertsForSetScriptTransaction(account, SUM_FEE);
        }
    }

    @Test
    @DisplayName("invoke transaction with DeleteEntry")
    void invokeScriptDeleteEntryTest() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            final String functionArgs = "intVal:Int";
            final String functions = "\tIntegerEntry(\"int\", intVal),\n" +
                    "\tDeleteEntry(\"int\")";
            final String script = defaultFunctionBuilder(functionArgs, functions, getRandomInt(4, MAX_LIB_VERSION));
            final DataDApp dAppAccount = new DataDApp(DEFAULT_FAUCET, script);

            DAppCall dAppCall = dAppAccount.setData(intArg);
            Amount amount = WAVES.of(0.1);
            invokeSenderWithPayment(account, dAppAccount, dAppCall, amount, v, SUM_FEE);
            checkAssertsForSetScriptTransaction(account, SUM_FEE);
        }
    }

    @Test
    @DisplayName("invoke transaction with Burn transaction")
    void invokeScriptWithBurn() {
        long fee = ONE_WAVES + SUM_FEE;
        for (int v = 1; v <= LATEST_VERSION; v++) {
            final int libVersion = getRandomInt(4, MAX_LIB_VERSION);
            final String functions = "Burn(assetId, 1),\n\tBurn(issueAssetId, 1)";
            final String script = assetsFunctionBuilder(libVersion, "unit", functions, args);
            final AssetDAppAccount dAppAccount = new AssetDAppAccount(DEFAULT_FAUCET, script);
            String dAppAssetId = dAppAccount.issue(i -> i.name("outside Asset").quantity(900_000_000L))
                    .tx().assetId().toString();

            DAppCall dAppCall = dAppAccount.setDataAssetId(Base58.decode(dAppAssetId));
            invokeSender(dAppAccount, dAppCall, LATEST_VERSION, SUM_FEE, ONE_WAVES);
            checkAssertsForSetScriptTransaction(dAppAccount, fee);
        }
    }

    @Test
    @DisplayName("invoke transaction with Reissue Transaction")
    void invokeScriptWithReissue() {
        long fee = ONE_WAVES + SUM_FEE;
        for (int v = 1; v <= LATEST_VERSION; v++) {
            final int libVersion = getRandomInt(4, MAX_LIB_VERSION);
            final String functions = "Reissue(assetId, 1, true),\n\tReissue(issueAssetId, 1, true)";
            final String script = assetsFunctionBuilder(libVersion, "unit", functions, args);
            final AssetDAppAccount dAppAccount = new AssetDAppAccount(DEFAULT_FAUCET, script);
            String dAppAssetId = dAppAccount.issue(i -> i.name("outside Asset").quantity(900_000_000L))
                    .tx().assetId().toString();

            DAppCall dAppCall = dAppAccount.setDataAssetId(Base58.decode(dAppAssetId));
            invokeSender(dAppAccount, dAppCall, LATEST_VERSION, SUM_FEE, ONE_WAVES);
            checkAssertsForSetScriptTransaction(dAppAccount, fee);
        }
    }

    @Test
    @DisplayName("invoke transaction with Lease Transaction")
    void invokeScriptWithLease() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            final String functionArgs = "address:ByteVector";
            final String functions = "\tLease(Address(address), 1_000_000)";
            final String script = defaultFunctionBuilder(functionArgs, functions, getRandomInt(5, MAX_LIB_VERSION));
            final DataDApp dAppAccount = new DataDApp(DEFAULT_FAUCET, script);

            DAppCall dAppCall = dAppAccount.setData(accountAddress);
            Amount amount = WAVES.of(0.1919);
            invokeSenderWithPayment(account, dAppAccount, dAppCall, amount, v, SUM_FEE);
            checkAssertsForSetScriptTransaction(account, SUM_FEE);
        }
    }

    @Test
    @DisplayName("invoke transaction with LeaseCancel Transaction")
    void invokeScriptWithLeaseCancel() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            final String functionArgs = "leaseId:ByteVector";
            final String functions = "\tLeaseCancel(leaseId)";
            final String script = defaultFunctionBuilder(functionArgs, functions, getRandomInt(5, MAX_LIB_VERSION));
            final DataDApp dAppAccount = new DataDApp(DEFAULT_FAUCET, script);
            byte[] leaseId = Base58.decode(dAppAccount.lease(account, getRandomInt(1, 9999)).tx().id().toString());

            DAppCall dAppCall = dAppAccount.setData(leaseId);
            Amount amount = WAVES.of(0.1919);
            invokeSenderWithPayment(account, dAppAccount, dAppCall, amount, v, SUM_FEE);
            checkAssertsForSetScriptTransaction(account, SUM_FEE);
        }
    }

    @Test
    @DisplayName("invoke transaction with SponsorFee Transaction")
    void invokeScriptWithSponsorFee() {
        long fee = ONE_WAVES + SUM_FEE;
        for (int v = 1; v <= LATEST_VERSION; v++) {
            final int libVersion = getRandomInt(4, MAX_LIB_VERSION);
            final String functions = "SponsorFee(assetId, 500),\n\tSponsorFee(issueAssetId, 500)";
            final String script = assetsFunctionBuilder(libVersion, "unit", functions, args);
            final AssetDAppAccount dAppAccount = new AssetDAppAccount(DEFAULT_FAUCET, script);
            String dAppAssetId = dAppAccount.issue(i -> i.name("outside Asset").quantity(900_000_000L))
                    .tx().assetId().toString();

            DAppCall dAppCall = dAppAccount.setDataAssetId(Base58.decode(dAppAssetId));
            invokeSender(dAppAccount, dAppCall, LATEST_VERSION, SUM_FEE, ONE_WAVES);
            checkAssertsForSetScriptTransaction(dAppAccount, fee);
        }
    }

    @Test
    @DisplayName("invoke transaction with ScriptTransfer")
    void invokeScriptWithScriptTransfer() {
        final long fee = ONE_WAVES + SUM_FEE;
        final String currentArgs = args + ", " + "address:ByteVector";
        for (int v = 1; v <= LATEST_VERSION; v++) {
            final long wavesAmount = getRandomInt(100, 100_000_000);
            final int libVersion = getRandomInt(4, MAX_LIB_VERSION);
            final String functions = "ScriptTransfer(Address(address), 500, assetId),\n" +
                    "\tScriptTransfer(Address(address), 500, issueAssetId),\n" +
                    "\tScriptTransfer(Address(address), " + wavesAmount + ", unit)";
            final String script = assetsFunctionBuilder(libVersion, "unit", functions, currentArgs);
            final AssetDAppAccount dAppAccount = new AssetDAppAccount(DEFAULT_FAUCET, script);
            String dAppAssetId = dAppAccount.issue(i -> i.name("outside Asset").quantity(900_000_000L))
                    .tx().assetId().toString();

            DAppCall dAppCall = dAppAccount.setDataAssetAndAddress(Base58.decode(dAppAssetId), accountAddress);
            invokeSender(dAppAccount, dAppCall, LATEST_VERSION, SUM_FEE, ONE_WAVES);
            setBalanceAfterTransaction(getBalanceAfterTransaction() - wavesAmount);
            checkAssertsForSetScriptTransaction(dAppAccount, fee);
        }
    }

    private void checkAssertsForSetScriptTransaction(Account account, long fee) {
        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getWavesBalance()).isEqualTo(getBalanceAfterTransaction()),
                () -> assertThat(getInvokeScriptTx().dApp()).isEqualTo(getDAppCall().getDApp()),
                () -> assertThat(getInvokeScriptTx().function()).isEqualTo(getDAppCall().getFunction()),
                () -> assertThat(getInvokeScriptTx().sender()).isEqualTo(account.publicKey()),
                () -> assertThat(getInvokeScriptTx().fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(getInvokeScriptTx().fee().value()).isEqualTo(fee),
                () -> assertThat(getInvokeScriptTx().type()).isEqualTo(16)
        );
    }
}
