package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;

public class SponsorFeeTransaction extends Transactions {
    public static String getAssetIdFromSponsorFee(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getSponsorFee()
                .getMinFee()
                .getAssetId()
                .toByteArray());
    }

    public static long getAmountFromSponsorFee(int txIndex) {
        return getTransactionAtIndex(txIndex)
                .getSponsorFee()
                .getMinFee()
                .getAmount();
    }
}
