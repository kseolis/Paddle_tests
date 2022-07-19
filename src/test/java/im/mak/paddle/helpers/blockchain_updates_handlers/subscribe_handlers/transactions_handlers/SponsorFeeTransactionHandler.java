package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class SponsorFeeTransactionHandler {
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
