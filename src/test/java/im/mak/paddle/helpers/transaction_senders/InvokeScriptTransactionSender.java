package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.InvokeScriptTransaction;
import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
import im.mak.paddle.dapp.DAppCall;

import static im.mak.paddle.Node.node;

public class InvokeScriptTransactionSender extends BaseTransactionSender {
    private static InvokeScriptTransaction invokeScriptTx;
    private static DAppCall dAppCall;
    private static long dAppAccountBalance;
    private static long dAppAccountBalanceAfterTransaction;

    public static void invokeSenderWithPayment(Account payer, Account dAppAccount, DAppCall call, Amount amount, int version, long fee) {
        accountWavesBalance = payer.getWavesBalance();
        balanceAfterTransaction = payer.getWavesBalance() - fee - amount.value();
        dAppAccountBalance = dAppAccount.getWavesBalance();
        dAppAccountBalanceAfterTransaction = dAppAccountBalance + amount.value();
        dAppCall = call;

        invokeScriptTx = InvokeScriptTransaction
                .builder(dAppAccount.address(), dAppCall.getFunction())
                .payments(amount)
                .version(version)
                .getSignedWith(payer.privateKey());

        node().waitForTransaction(node().broadcast(invokeScriptTx).id());

        txInfo = node().getTransactionInfo(invokeScriptTx.id());
    }


    public static void invokeSender(Account dAppAccount, DAppCall call, int version, long fee, long extraFee) {
        accountWavesBalance = dAppAccount.getWavesBalance();
        balanceAfterTransaction = dAppAccount.getWavesBalance() - fee - extraFee;
        dAppAccountBalance = dAppAccount.getWavesBalance();
        dAppAccountBalanceAfterTransaction = dAppAccountBalance;
        dAppCall = call;

        invokeScriptTx = InvokeScriptTransaction
                .builder(dAppAccount.address(), dAppCall.getFunction())
                .version(version)
                .extraFee(extraFee)
                .getSignedWith(dAppAccount.privateKey());
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

    public static long getDAppAccountBalance() {
        return dAppAccountBalance;
    }

    public static long getDAppAccountBalanceAfterTransaction() {
        return dAppAccountBalanceAfterTransaction;
    }

}
