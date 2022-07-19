package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.CreateAliasTransaction;
import im.mak.paddle.Account;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.MIN_FEE;

public class CreateAliasTransactionSender extends BaseTransactionSender {
    private static CreateAliasTransaction createAliasTx;

    public static void createAliasTransactionSender(Account account, String alias, long fee, int version) {
        accountWavesBalance = account.getWavesBalance();
        balanceAfterTransaction = accountWavesBalance - fee;
        System.out.println(accountWavesBalance);
        createAliasTx = CreateAliasTransaction
                .builder(alias)
                .fee(fee)
                .version(version)
                .getSignedWith(account.privateKey());

        node().waitForTransaction(node().broadcast(createAliasTx).id());
        txInfo = node().getTransactionInfo(createAliasTx.id());
    }

    public static CreateAliasTransaction getCreateAliasTx() {
        return createAliasTx;
    }
}
