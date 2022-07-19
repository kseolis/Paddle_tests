package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class AliasTransactionHandler {
    public static String getAliasFromAliasTransaction(int txIndex) {
        return getTransactionAtIndex(txIndex).getCreateAlias().getAlias();
    }
}
