package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.InvokeScriptTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.dapp.DAppCall;

import java.util.List;

import static im.mak.paddle.Node.node;

public class InvokeScriptTransactionSender extends BaseTransactionSender {
    private static InvokeScriptTransaction invokeScriptTx;
    private static DAppCall dAppCall;

    private static long callerBalanceWavesAfterTransaction;
    private static long callerBalanceIssuedAssetsAfterTransaction;
    private static long dAppBalanceWavesAfterTransaction;
    private static long dAppBalanceIssuedAssetsAfterTransaction;

    public static void invokeSenderWithPayment(Account caller, Account dAppAccount, DAppCall call, List<Amount> amounts) {
        dAppCall = call;

        invokeScriptTx = InvokeScriptTransaction
                .builder(dAppAccount.address(), dAppCall.getFunction())
                .payments(amounts)
                .version(version)
                .extraFee(extraFee)
                .getSignedWith(caller.privateKey());

        node().waitForTransaction(node().broadcast(invokeScriptTx).id());

        txInfo = node().getTransactionInfo(invokeScriptTx.id());
    }


    public static void invokeSender(Account caller, Account dAppAccount, DAppCall call) {
        dAppCall = call;

        invokeScriptTx = InvokeScriptTransaction
                .builder(dAppAccount.address(), dAppCall.getFunction())
                .version(version)
                .extraFee(extraFee)
                .getSignedWith(caller.privateKey());
        node().waitForTransaction(node().broadcast(invokeScriptTx).id());

        txInfo = node().getTransactionInfo(invokeScriptTx.id());
    }

    public static InvokeScriptTransaction getInvokeScriptTx() {
        return invokeScriptTx;
    }

    public static DAppCall getDAppCall() {
        return dAppCall;
    }

    public static String getInvokeScriptId() {
        return invokeScriptTx.id().toString();
    }

    public static long getCallerBalanceWavesAfterTransaction() {
        return callerBalanceWavesAfterTransaction;
    }

    public static long getCallerBalanceIssuedAssetsAfterTransaction() {
        return callerBalanceIssuedAssetsAfterTransaction;
    }

    public static long getDAppBalanceWavesAfterTransaction() {
        return dAppBalanceWavesAfterTransaction;
    }

    public static long getDAppBalanceIssuedAssetsAfterTransaction() {
        return dAppBalanceIssuedAssetsAfterTransaction;
    }

    public static void balancesAfterPaymentInvoke(Account caller, Account dApp, List<Amount> amounts, AssetId id) {
        callerBalanceWavesAfterTransaction = caller.getWavesBalance() - fee - extraFee;
        callerBalanceIssuedAssetsAfterTransaction = caller.getBalance(id);
        dAppBalanceWavesAfterTransaction = dApp.getWavesBalance();
        dAppBalanceIssuedAssetsAfterTransaction = dApp.getBalance(id);

        if (!amounts.isEmpty()) {
            amounts.forEach(
                    a -> {
                        if (a.assetId().isWaves()) {
                            callerBalanceWavesAfterTransaction -= a.value();
                            dAppBalanceWavesAfterTransaction += a.value();
                        } else if (a.assetId().equals(id)) {
                            callerBalanceIssuedAssetsAfterTransaction += a.value();
                            dAppBalanceIssuedAssetsAfterTransaction -= a.value();
                        }
                    }
            );
        }
    }

    public static void balancesAfterBurnAssetInvoke(Account caller, Account dApp, List<Amount> amounts, AssetId id) {
        callerBalanceWavesAfterTransaction = caller.getWavesBalance() - fee - extraFee;
        callerBalanceIssuedAssetsAfterTransaction = caller.getBalance(id);
        dAppBalanceWavesAfterTransaction = dApp.getWavesBalance();
        dAppBalanceIssuedAssetsAfterTransaction = dApp.getBalance(id);

        if (!amounts.isEmpty()) {
            amounts.forEach(
                    a -> {
                        if (a.assetId().isWaves()) {
                            callerBalanceWavesAfterTransaction -= a.value();
                            dAppBalanceWavesAfterTransaction += a.value();
                        } else if (a.assetId().equals(id)) {
                            dAppBalanceIssuedAssetsAfterTransaction -= a.value();
                        }
                    }
            );
        }
    }

    public static void balancesAfterReissueAssetInvoke(Account caller, Account dApp, List<Amount> amounts, AssetId id) {
        callerBalanceWavesAfterTransaction = caller.getWavesBalance() - fee - extraFee;
        callerBalanceIssuedAssetsAfterTransaction = caller.getBalance(id);
        dAppBalanceWavesAfterTransaction = dApp.getWavesBalance();
        dAppBalanceIssuedAssetsAfterTransaction = dApp.getBalance(id);

        if (!amounts.isEmpty()) {
            amounts.forEach(
                    a -> {
                        if (a.assetId().isWaves()) {
                            callerBalanceWavesAfterTransaction -= a.value();
                            dAppBalanceWavesAfterTransaction += a.value();
                        } else if (a.assetId().equals(id)) {
                            dAppBalanceIssuedAssetsAfterTransaction += a.value();
                        }
                    }
            );
        }
    }

    public static void balancesAfterCallerInvokeAsset(Account caller, Account dApp, List<Amount> amounts, AssetId id) {
        callerBalanceWavesAfterTransaction = caller.getWavesBalance() - fee - extraFee;
        callerBalanceIssuedAssetsAfterTransaction = caller.getBalance(id);
        dAppBalanceWavesAfterTransaction = dApp.getWavesBalance();
        dAppBalanceIssuedAssetsAfterTransaction = dApp.getBalance(id);

        if (!amounts.isEmpty()) {
            amounts.forEach(
                    a -> {
                        if (a.assetId().isWaves()) {
                            callerBalanceWavesAfterTransaction -= a.value();
                            dAppBalanceWavesAfterTransaction += a.value();
                        } else if (a.assetId().equals(id)) {
                            callerBalanceIssuedAssetsAfterTransaction -= a.value();
                            dAppBalanceIssuedAssetsAfterTransaction += a.value();
                        }
                    }
            );
        }
    }
}
