package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.protobuf.transaction.TransactionOuterClass;
import im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler;

public class Transactions extends SubscribeHandler {
    public static TransactionOuterClass.Transaction getTransactionAtIndex(int index) {
        return getMicroBlockInfo().getTransactions(index).getTransaction();
    }

    public static String getSenderPublicKeyFromTransaction(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getSenderPublicKey()
                .toByteArray());
    }

    public static long getTransactionFeeAmount(int txIndex) {
        return getTransactionAtIndex(txIndex).getFee().getAmount();
    }

    public static long getTransactionVersion(int txIndex) {
        return getTransactionAtIndex(txIndex).getVersion();
    }
}
