package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.SetScriptTransaction;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.Account;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.EXTRA_FEE_FOR_SET_SCRIPT;
import static im.mak.paddle.util.Constants.MIN_FEE_FOR_SET_SCRIPT;

public class SetScriptTransactionSender extends BaseTransactionSender {
    private static SetScriptTransaction setScriptTx;
    private static long fee;

    public static void setScriptTransactionSender(Account account, Base64String script, long moreFee, int version) {
        fee = MIN_FEE_FOR_SET_SCRIPT + moreFee + EXTRA_FEE_FOR_SET_SCRIPT;
        balanceAfterTransaction = account.getWavesBalance() - fee;

        setScriptTx = SetScriptTransaction
                .builder(script)
                .fee(fee)
                .version(version)
                .getSignedWith(account.privateKey());
        node().waitForTransaction(node().broadcast(setScriptTx).id());

        txInfo = node().getTransactionInfo(setScriptTx.id());
    }

    public static SetScriptTransaction getSetScriptTx() {
        return setScriptTx;
    }


    public static long getFee() {
        return fee;
    }
}
