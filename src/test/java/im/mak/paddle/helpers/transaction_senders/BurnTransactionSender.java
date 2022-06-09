package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.BurnTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;

import static im.mak.paddle.Node.node;

public class BurnTransactionSender extends BaseTransactionSender {
    private static BurnTransaction burnTx;

    public static void burnTransactionSender(Account account, Amount amount, AssetId assetId, long fee, int version) {
        accountWavesBalance = account.getBalance(AssetId.WAVES);
        balanceAfterTransaction = account.getBalance(assetId) - amount.value();

        burnTx = BurnTransaction.builder(amount)
                .version(version)
                .sender(account.publicKey())
                .fee(fee)
                .getSignedWith(account.privateKey());
        node().waitForTransaction(node().broadcast(burnTx).id());

        txInfo = node().getTransactionInfo(burnTx.id());
    }

    public static BurnTransaction getBurnTx() {
        return burnTx;
    }

}
