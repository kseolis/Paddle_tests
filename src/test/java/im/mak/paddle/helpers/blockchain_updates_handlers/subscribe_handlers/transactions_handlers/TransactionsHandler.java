package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.protobuf.transaction.TransactionOuterClass;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.getMicroBlockInfo;

public class TransactionsHandler {

    public static TransactionOuterClass.Transaction getTransactionAtIndex(int index) {
        return getMicroBlockInfo().getTransactions(index).getTransaction();
    }

    public static long getChainId(int index) {
        return getTransactionAtIndex(index).getChainId();
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
