package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.InvokeScriptTransaction;
import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
import im.mak.paddle.dapp.DAppCall;
import im.mak.paddle.dapps.IntDApp;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;

public class InvokeScriptTransactionSender extends BaseTransactionSender {
    private static InvokeScriptTransaction invokeScriptTx;
    private static DAppCall dAppCall;
    private static long dAppAccountBalance;
    private static long dAppAccountBalanceAfterTransaction;

    public static void invokeIntDAppSender(Account account, IntDApp dAppAccount, Amount amount, int version, long fee) {
        accountWavesBalance = account.getWavesBalance();
        balanceAfterTransaction = account.getWavesBalance() - fee - amount.value();
        dAppAccountBalance = dAppAccount.getWavesBalance();
        dAppAccountBalanceAfterTransaction = dAppAccountBalance + amount.value();

        dAppCall = dAppAccount.setInt(getRandomInt(1, 500));

        invokeScriptTx = InvokeScriptTransaction
                .builder(dAppAccount.address(), dAppCall.getFunction())
                .payments(amount)
                .version(version)
                .getSignedWith(account.privateKey());

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
