package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.getTransactionAtIndex;

public class SponsorFeeTransaction {
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
