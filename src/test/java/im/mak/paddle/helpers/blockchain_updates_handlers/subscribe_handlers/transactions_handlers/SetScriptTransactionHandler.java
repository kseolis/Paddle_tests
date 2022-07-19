package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class SetScriptTransactionHandler {
    public static byte[] getScriptFromSetScript(int txIndex) {
        return getTransactionAtIndex(txIndex)
                .getSetScript()
                .getScript()
                .toByteArray();
    }
}
