package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.getTransactionAtIndex;

public class IssueTransaction {
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
