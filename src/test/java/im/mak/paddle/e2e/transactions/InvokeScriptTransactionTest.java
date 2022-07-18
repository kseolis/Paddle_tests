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

import java.util.ArrayList;
import java.util.List;

import static com.wavesplatform.transactions.InvokeScriptTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.helpers.ConstructorRideFunctions.assetsFunctionBuilder;
import static im.mak.paddle.helpers.ConstructorRideFunctions.defaultFunctionBuilder;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.InvokeScriptTransactionSender.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class InvokeScriptTransactionTest {
    private static Account callerAccount;
    private static byte[] callerAddress;

    private static DataDApp dAppAccount;
    private static byte[] dAppAddress;

    private static AssetDAppAccount assetDAppAccount;
    private static byte[] assetDAppAddress;

    private static AssetId assetId;

    private static int intArg;
    private static String binArg;
    private static boolean boolArg;
    private static String stringArg;

    private static Amount wavesAmount;
    private static Amount assetAmount;

    private static final String args = "assetId:ByteVector";
    private static final List<Amount> amounts = new ArrayList<>();

    @BeforeAll
    static void before() {
        async(
                () -> {
                    callerAccount = new Account(DEFAULT_FAUCET);
                    callerAddress = Base58.decode(callerAccount.address().toString());
                },
                () -> binArg = randomNumAndLetterString(10),
                () -> stringArg = randomNumAndLetterString(10),
                () -> {
                    intArg = getRandomInt(1, 999999);
                    boolArg = intArg % 2 == 0;
                },
                () -> {
                    assetDAppAccount = new AssetDAppAccount(DEFAULT_FAUCET, "true");
                    assetDAppAddress = Base58.decode(assetDAppAccount.address().toString());
                    assetId = assetDAppAccount.issue(i -> i.name("outside Asset").quantity(900_000_000L))
                            .tx().assetId();
                },
                () -> {
                    dAppAccount = new DataDApp(DEFAULT_FAUCET, "true");
                    dAppAddress = Base58.decode(dAppAccount.address().toString());
                }
        );
        assetDAppAccount.transfer(callerAccount, Amount.of(300_000_000L, assetId));
        assetDAppAccount.transfer(dAppAccount, Amount.of(300_000_000L, assetId));
        wavesAmount = Amount.of(getRandomInt(100, 100000));
        assetAmount = Amount.of(getRandomInt(100, 100000), assetId);
    }

    @Test
    @DisplayName("invoke transaction with DataDApp and issue asset payment")
    void invokeScriptWithDataDAppTest() {
        amounts.clear();
        amounts.add(wavesAmount);
        final String functionArgs = "intVal:Int, binVal:ByteVector, boolVal:Boolean, strVal:String";
        final String functions = "\tIntegerEntry(\"int\", intVal),\n\tBinaryEntry(\"byte\", binVal),\n" +
                "\tBooleanEntry(\"bool\", boolVal),\n\tStringEntry(\"str\", strVal)";
        final String script = defaultFunctionBuilder(functionArgs, functions, getRandomInt(4, MAX_LIB_VERSION));
        dAppAccount.setScript(script);
        final DAppCall dAppCall = dAppAccount.setData(intArg, binArg, boolArg, stringArg);
        setFee(SUM_FEE);
        setExtraFee(0);

        for (int v = 1; v <= LATEST_VERSION; v++) {
            setVersion(v);
            balancesAfterPaymentInvoke(callerAccount, dAppAccount, amounts, assetId);
            invokeSenderWithPayment(callerAccount, dAppAccount, dAppCall, amounts);
            checkInvokeTransaction(callerAccount, dAppAccount, SUM_FEE);
        }
    }

    @Test
    @DisplayName("invoke transaction with DeleteEntry")
    void invokeScriptDeleteEntryTest() {
        amounts.clear();
        amounts.add(wavesAmount);

        final String functionArgs = "intVal:Int";
        final String functions = "\tIntegerEntry(\"int\", intVal),\n\tDeleteEntry(\"int\")";
        final String script = defaultFunctionBuilder(functionArgs, functions, getRandomInt(4, MAX_LIB_VERSION));
        dAppAccount.setScript(script);
        DAppCall dAppCall = dAppAccount.setData(intArg);
        setFee(SUM_FEE);
        setExtraFee(0);

        for (int v = 1; v <= LATEST_VERSION; v++) {
            setVersion(v);
            balancesAfterPaymentInvoke(callerAccount, dAppAccount, amounts, assetId);
            invokeSenderWithPayment(callerAccount, dAppAccount, dAppCall, amounts);
            checkInvokeTransaction(callerAccount, dAppAccount, SUM_FEE);
        }
    }

    @Test
    @DisplayName("invoke transaction with Burn transaction")
    void invokeScriptWithBurn() {
        amounts.clear();
        amounts.add(assetAmount);

        long fee = ONE_WAVES + SUM_FEE;
        final int libVersion = getRandomInt(4, MAX_LIB_VERSION);

        final String functions = "Burn(assetId, " + assetAmount.value() + "),\n\tBurn(issueAssetId, 1)";

        final String script = assetsFunctionBuilder(libVersion, "unit", functions, args);
        assetDAppAccount.setScript(script);

        final DAppCall dAppCall = assetDAppAccount.setDataAssetId(Base58.decode(assetId.toString()));

        setFee(SUM_FEE);
        setExtraFee(ONE_WAVES);

        for (int v = 1; v <= LATEST_VERSION; v++) {
            setVersion(v);
            balancesAfterBurnAssetInvoke(callerAccount, assetDAppAccount, amounts, assetId);
            invokeSender(callerAccount, assetDAppAccount, dAppCall);
            checkInvokeTransaction(callerAccount, assetDAppAccount, fee);
        }
    }

    @Test
    @DisplayName("invoke transaction with Reissue Transaction")
    void invokeScriptWithReissue() {
        long fee = ONE_WAVES + SUM_FEE;
        final int libVersion = getRandomInt(4, MAX_LIB_VERSION);

        amounts.clear();
        amounts.add(assetAmount);

        final String functions = "Reissue(assetId," + assetAmount.value() + ",true),\n\tReissue(issueAssetId,1,true)";
        final String script = assetsFunctionBuilder(libVersion, "unit", functions, args);

        assetDAppAccount.setScript(script);
        final DAppCall dAppCall = assetDAppAccount.setDataAssetId(Base58.decode(assetId.toString()));

        setFee(SUM_FEE);
        setExtraFee(ONE_WAVES);

        for (int v = 1; v <= LATEST_VERSION; v++) {
            setVersion(v);
            balancesAfterReissueAssetInvoke(callerAccount, assetDAppAccount, amounts, assetId);
            invokeSender(callerAccount, assetDAppAccount, dAppCall);
            checkInvokeTransaction(callerAccount, assetDAppAccount, fee);
        }
    }

    @Test
    @DisplayName("invoke with Lease Transaction and WAVES payment")
    void invokeScriptWithLease() {
        amounts.clear();
        amounts.add(Amount.of(199_000));

        final String functionArgs = "address:ByteVector";
        final String functions = "\tLease(Address(address), 1_000_000)";
        final String script = defaultFunctionBuilder(functionArgs, functions, getRandomInt(5, MAX_LIB_VERSION));

        dAppAccount.setScript(script);

        final DAppCall dAppCall = dAppAccount.setData(callerAddress);

        setFee(SUM_FEE);
        setExtraFee(0);

        for (int v = 1; v <= LATEST_VERSION; v++) {
            setVersion(v);
            invokeSenderWithPayment(callerAccount, dAppAccount, dAppCall, amounts);
            checkInvokeTransaction(callerAccount, dAppAccount, SUM_FEE);
        }
    }

    @Test
    @DisplayName("invoke with LeaseCancel Transaction and WAVES payment")
    void invokeScriptWithLeaseCancel() {
        amounts.clear();
        amounts.add(Amount.of(199_000));
        final String functionArgs = "leaseId:ByteVector";
        final String functions = "\tLeaseCancel(leaseId)";
        final String script = defaultFunctionBuilder(functionArgs, functions, getRandomInt(5, MAX_LIB_VERSION));
        dAppAccount.setScript(script);
        setFee(SUM_FEE);
        setExtraFee(0);

        for (int v = 1; v <= LATEST_VERSION; v++) {
            byte[] leaseId = Base58.decode(dAppAccount.lease(callerAccount, getRandomInt(1, 9999)).tx().id().toString());
            final DAppCall dAppCall = dAppAccount.setData(leaseId);
            setVersion(v);
            balancesAfterPaymentInvoke(callerAccount, dAppAccount, amounts, assetId);
            invokeSenderWithPayment(callerAccount, dAppAccount, dAppCall, amounts);
            checkInvokeTransaction(callerAccount, dAppAccount, SUM_FEE);
        }
    }

    @Test
    @DisplayName("invoke transaction with SponsorFee Transaction")
    void invokeScriptWithSponsorFee() {
        amounts.clear();
        long fee = ONE_WAVES + SUM_FEE;
        final int libVersion = getRandomInt(4, MAX_LIB_VERSION);
        final String functions = "SponsorFee(assetId, 500),\n\tSponsorFee(issueAssetId, 500)";
        final String script = assetsFunctionBuilder(libVersion, "unit", functions, args);
        assetDAppAccount.setScript(script);
        amounts.add(Amount.of(500, assetId));
        final DAppCall dAppCall = assetDAppAccount.setDataAssetId(Base58.decode(assetId.toString()));
        setFee(SUM_FEE);
        setExtraFee(ONE_WAVES);

        for (int v = 1; v <= LATEST_VERSION; v++) {
            setVersion(v);
            balancesAfterPaymentInvoke(callerAccount, assetDAppAccount, amounts, assetId);
            invokeSender(callerAccount, assetDAppAccount, dAppCall);
            checkInvokeTransaction(callerAccount, assetDAppAccount, fee);
        }
    }

    @Test
    @DisplayName("invoke transaction with ScriptTransfer")
    void invokeScriptWithScriptTransfer() {
        final long fee = ONE_WAVES + SUM_FEE;
        final int libVersion = getRandomInt(4, MAX_LIB_VERSION);

        amounts.clear();
        amounts.add(wavesAmount);
        amounts.add(assetAmount);

        final String currentArgs = args + ", " + "address:ByteVector";
        final String functions = "ScriptTransfer(Address(address), " + assetAmount.value() + ", assetId),\n" +
                "\tScriptTransfer(Address(address), 500, issueAssetId),\n" +
                "\tScriptTransfer(Address(address), " + wavesAmount.value() + ", unit)";
        final String script = assetsFunctionBuilder(libVersion, "unit", functions, currentArgs);

        assetDAppAccount.setScript(script);

        final DAppCall dAppCall = assetDAppAccount.setDataAssetAndAddress(Base58.decode(assetId.toString()), dAppAddress);

        setFee(SUM_FEE);
        setExtraFee(ONE_WAVES);

        for (int v = 1; v <= LATEST_VERSION; v++) {
            setVersion(v);
            balancesAfterScriptTransferInvoke(assetDAppAccount, dAppAccount, amounts, assetId);
            invokeSender(assetDAppAccount, assetDAppAccount, dAppCall);
            checkInvokeTransaction(assetDAppAccount, dAppAccount, fee);
        }
    }

    @Test
    @DisplayName("invoke with payments")
    void invokeScriptPayments() {
        final long fee = ONE_WAVES + SUM_FEE;
        final String arg = "intVal:Int";
        final int libVersion = getRandomInt(4, MAX_LIB_VERSION);
        final String functions = "IntegerEntry(\"int\", intVal)";
        final String script = assetsFunctionBuilder(libVersion, "unit", functions, arg);
        dAppAccount = new DataDApp(DEFAULT_FAUCET, script);
        final DAppCall dAppCall = dAppAccount.setData(getRandomInt(1, 1000));
        AssetId callerAssetId = callerAccount.issue(i -> i.name("is_asset").quantity(324_993_000)).tx().assetId();
        amounts.add(Amount.of(500, callerAssetId));
        setFee(SUM_FEE);
        setExtraFee(ONE_WAVES);

        for (int v = 1; v <= LATEST_VERSION; v++) {
            setVersion(v);
            balancesAfterPaymentInvoke(callerAccount, dAppAccount, amounts, assetId);
            invokeSenderWithPayment(callerAccount, dAppAccount, dAppCall, amounts);
            checkInvokeTransaction(callerAccount, dAppAccount, fee);
        }
    }

    private void checkInvokeTransaction(Account caller, Account dApp, long fee) {
        assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED);
        assertThat(getInvokeScriptTx().dApp()).isEqualTo(getDAppCall().getDApp());
        assertThat(getInvokeScriptTx().function()).isEqualTo(getDAppCall().getFunction());
        assertThat(getInvokeScriptTx().sender()).isEqualTo(caller.publicKey());
        assertThat(getInvokeScriptTx().fee().assetId()).isEqualTo(AssetId.WAVES);
        assertThat(getInvokeScriptTx().fee().value()).isEqualTo(fee);
        assertThat(getInvokeScriptTx().type()).isEqualTo(16);

        assertThat(caller.getWavesBalance()).isEqualTo(getCallerBalanceWavesAfterTransaction());
        assertThat(dApp.getWavesBalance()).isEqualTo(getDAppBalanceWavesAfterTransaction());
        if (assetId != null) {
            assertThat(caller.getBalance(assetId)).isEqualTo(getCallerBalanceIssuedAssetsAfterTransaction());
            assertThat(dApp.getBalance(assetId)).isEqualTo(getDAppBalanceIssuedAssetsAfterTransaction());
        }
        System.out.println("PASSED");
    }
}