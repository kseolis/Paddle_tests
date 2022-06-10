package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.LeaseCancelTransaction;
import com.wavesplatform.transactions.LeaseTransaction;
import com.wavesplatform.transactions.common.Id;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.MIN_FEE;

public class LeaseTransactionSender extends BaseTransactionSender {
    private static LeaseTransaction leaseTx;
    private static LeaseCancelTransaction leaseCancelTx;
    private static long effectiveBalanceAfterSendTransaction;
    private static long balanceAfterReceiving;

    public static void leaseTransactionSender(long amount, Account from, Account to, int version) {
        effectiveBalanceAfterSendTransaction = from.getWavesBalanceDetails().effective() - MIN_FEE - amount;
        balanceAfterReceiving = to.getWavesBalanceDetails().effective() + amount;

        leaseTx = LeaseTransaction
                .builder(to.address(), amount)
                .version(version)
                .getSignedWith(from.privateKey());

        node().waitForTransaction(node().broadcast(leaseTx).id());

        txInfo = node().getTransactionInfo(leaseTx.id());
    }

    public static void leaseCancelTransactionSender(Account from, Account to, Id index, long leaseSum, int version) {
        effectiveBalanceAfterSendTransaction = from.getWavesBalanceDetails().effective() - MIN_FEE + leaseSum;
        balanceAfterReceiving = to.getWavesBalanceDetails().effective() - leaseSum;

        leaseCancelTx = LeaseCancelTransaction
                .builder(index)
                .version(version)
                .getSignedWith(from.privateKey());

        node().waitForTransaction(node().broadcast(leaseCancelTx).id());
        txInfo = node().getTransactionInfo(leaseCancelTx.id());
    }

    public static LeaseTransaction getLeaseTx() {
        return leaseTx;
    }

    public static LeaseCancelTransaction getLeaseCancelTx() {
        return leaseCancelTx;
    }

    public static long getEffectiveBalanceAfterSendTransaction() {
        return effectiveBalanceAfterSendTransaction;
    }

    public static long getBalanceAfterReceiving() {
        return balanceAfterReceiving;
    }

}
