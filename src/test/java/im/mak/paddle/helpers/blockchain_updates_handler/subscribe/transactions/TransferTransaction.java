package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;

public class TransferTransaction extends Transactions {
    public static String getTransferTransactionPublicKeyHash(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getTransfer()
                .getRecipient()
                .getPublicKeyHash()
                .toByteArray());
    }
}
