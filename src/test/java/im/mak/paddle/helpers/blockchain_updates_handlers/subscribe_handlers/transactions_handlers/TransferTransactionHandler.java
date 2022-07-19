package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class TransferTransactionHandler {
    public static String getTransferTransactionPublicKeyHash(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getTransfer()
                .getRecipient()
                .getPublicKeyHash()
                .toByteArray());
    }

    public static long getTransferAssetAmount(int txIndex) {
        return getTransactionAtIndex(txIndex).getTransfer().getAmount().getAmount();
    }

    public static String getTransferAssetId(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getTransfer()
                .getAmount()
                .getAssetId()
                .toByteArray());
    }
}
