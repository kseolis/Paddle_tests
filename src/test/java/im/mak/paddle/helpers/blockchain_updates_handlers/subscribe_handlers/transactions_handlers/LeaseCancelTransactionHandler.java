package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class LeaseCancelTransactionHandler {
    public static String getLeaseCancelLeaseId(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex).getLeaseCancel().getLeaseId().toByteArray());
    }
}
