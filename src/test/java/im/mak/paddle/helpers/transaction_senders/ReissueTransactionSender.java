package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.ReissueTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;

import static im.mak.paddle.Node.node;

public class ReissueTransactionSender extends BaseTransactionSender {
    private static ReissueTransaction reissueTx;

    public static void reissueTransactionSender(Account account, Amount amount, AssetId id, long fee, int version) {
        balanceAfterTransaction = account.getBalance(id) + amount.value();
        reissueTx = ReissueTransaction.builder(amount)
                .version(version)
                .fee(fee)
                .getSignedWith(account.privateKey());
        node().waitForTransaction(node().broadcast(reissueTx).id());

        txInfo = node().getTransactionInfo(reissueTx.id());
    }

    public static ReissueTransaction getReissueTx() {
        return reissueTx;
    }

}
