package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.getTransactionAtIndex;

public class LeaseCancelTransaction {
    public static String getLeaseCancelLeaseId(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex).getLeaseCancel().getLeaseId().toByteArray());
    }
}
