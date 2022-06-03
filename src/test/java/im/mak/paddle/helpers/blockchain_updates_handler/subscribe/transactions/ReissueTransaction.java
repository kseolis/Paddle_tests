package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;

public class ReissueTransaction extends Transactions {
    public static String getAssetIdFromReissueTransaction(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex).getReissue().getAssetAmount().toByteArray());
    }

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
