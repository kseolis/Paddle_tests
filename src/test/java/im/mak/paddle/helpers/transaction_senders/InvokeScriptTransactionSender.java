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

    public static void invokeIntDAppSender(Account sender, IntDApp dAppAccount, Amount amount, int version, long fee) {
        balanceAfterTransaction = sender.getWavesBalance() - fee - amount.value();
        dAppCall = dAppAccount.setInt(getRandomInt(1, 50));

        invokeScriptTx = InvokeScriptTransaction
                .builder(dAppAccount.address(), dAppCall.getFunction())
                .payments(amount)
                .version(version)
                .getSignedWith(sender.privateKey());

        node().waitForTransaction(node().broadcast(invokeScriptTx).id());

        txInfo = node().getTransactionInfo(invokeScriptTx.id());
    }

    public static InvokeScriptTransaction getInvokeScriptTx() {
        return invokeScriptTx;
    }

    public static DAppCall getDAppCall() {
        return dAppCall;
    }


}
