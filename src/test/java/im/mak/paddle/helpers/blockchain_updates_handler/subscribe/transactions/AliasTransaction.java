package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.getTransactionAtIndex;

public class AliasTransaction {
    public static String getAliasFromAliasTransaction(int txIndex) {
        return getTransactionAtIndex(txIndex).getCreateAlias().getAlias();
    }
}
