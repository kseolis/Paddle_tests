package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;

public class BurnTransaction extends Transactions {
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
