package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import static com.wavesplatform.protobuf.transaction.TransactionOuterClass.DataTransactionData.DataEntry;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class DataTransactionHandler {
    public static String getKeyFromDataTx(int txIndex, int dataIndex) {
        return getDataEntryFromDataTx(txIndex, dataIndex).getKey();
    }

    public static DataEntry.ValueCase getValueFromDataTx(int txIndex, int dataIndex) {
        return getDataEntryFromDataTx(txIndex, dataIndex).getValueCase();
    }

    private static DataEntry getDataEntryFromDataTx(int txIndex, int dataIndex) {
        return getTransactionAtIndex(txIndex)
                .getDataTransaction()
                .getData(dataIndex);
    }
}
