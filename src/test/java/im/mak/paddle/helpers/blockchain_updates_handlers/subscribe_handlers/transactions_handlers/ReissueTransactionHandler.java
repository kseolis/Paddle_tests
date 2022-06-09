package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class ReissueTransactionHandler {
    public static long getReissueAssetAmount(int txIndex) {
        return getTransactionAtIndex(txIndex).getReissue().getAssetAmount().getAmount();
    }

    public static String getReissueAssetId(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getReissue()
                .getAssetAmount()
                .getAssetId()
                .toByteArray());
    }
}
