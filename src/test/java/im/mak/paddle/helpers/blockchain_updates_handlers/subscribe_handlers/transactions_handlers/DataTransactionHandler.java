package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import com.google.protobuf.ByteString;

import static com.wavesplatform.protobuf.transaction.TransactionOuterClass.DataTransactionData.DataEntry;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class DataTransactionHandler {
    public static String getKeyFromDataTx(int txIndex, int dataIndex) {
        return getDataEntryFromDataTx(txIndex, dataIndex).getKey();
    }

    public static long getIntValueFromDataTx(int txIndex, int dataIndex) {
        return getDataEntryFromDataTx(txIndex, dataIndex).getIntValue();
    }

    public static String getStringValueFromDataTx(int txIndex, int dataIndex) {
        return getDataEntryFromDataTx(txIndex, dataIndex).getStringValue();
    }

    public static boolean getBooleanValueFromDataTx(int txIndex, int dataIndex) {
        return getDataEntryFromDataTx(txIndex, dataIndex).getBoolValue();
    }

    public static ByteString getByteStringValueFromDataTx(int txIndex, int dataIndex) {
        return getDataEntryFromDataTx(txIndex, dataIndex).getBinaryValue();
    }

    private static DataEntry getDataEntryFromDataTx(int txIndex, int dataIndex) {
        return getTransactionAtIndex(txIndex)
                .getDataTransaction()
                .getData(dataIndex);
    }
}
