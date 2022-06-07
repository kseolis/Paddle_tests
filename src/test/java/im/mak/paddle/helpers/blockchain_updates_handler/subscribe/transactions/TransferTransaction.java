package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.getTransactionAtIndex;

public class TransferTransaction {
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
