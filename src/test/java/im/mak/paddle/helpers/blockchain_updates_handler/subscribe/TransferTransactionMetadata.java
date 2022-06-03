package im.mak.paddle.helpers.blockchain_updates_handler.subscribe;

import com.wavesplatform.crypto.base.Base58;

public class TransferTransactionMetadata extends TransactionMetadata {
    public static String getTransferRecipientAddressFromTransactionMetadata(int index) {
        return Base58.encode(getElementTransactionMetadata(index)
                .getTransfer()
                .getRecipientAddress()
                .toByteArray());
    }
}
