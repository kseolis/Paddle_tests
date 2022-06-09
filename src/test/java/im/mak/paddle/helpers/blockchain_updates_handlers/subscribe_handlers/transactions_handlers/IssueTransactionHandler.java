package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class IssueTransactionHandler {
    public static String getAssetName(int txIndex) {
        return getTransactionAtIndex(txIndex).getIssue().getName();
    }

    public static String getAssetDescription(int txIndex) {
        return getTransactionAtIndex(txIndex).getIssue().getDescription();
    }

    public static long getAssetAmount(int txIndex) {
        return getTransactionAtIndex(txIndex).getIssue().getAmount();
    }

    public static boolean getAssetReissuable(int txIndex) {
        return getTransactionAtIndex(txIndex).getIssue().getReissuable();
    }

    public static int getAssetDecimals(int txIndex) {
        return getTransactionAtIndex(txIndex).getIssue().getDecimals();
    }

    public static byte[] getAssetScript(int txIndex) {
        return getTransactionAtIndex(txIndex).getIssue().getScript().toByteArray();
    }
}
