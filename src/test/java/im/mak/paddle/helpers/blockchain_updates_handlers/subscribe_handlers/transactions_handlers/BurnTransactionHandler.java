package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class BurnTransactionHandler {
    public static long getBurnAssetAmount(int txIndex) {
        return getTransactionAtIndex(txIndex).getBurn().getAssetAmount().getAmount();
    }

    public static String getBurnAssetId(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getBurn()
                .getAssetAmount()
                .getAssetId()
                .toByteArray());
    }
}
