package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.getTransactionAtIndex;

public class MassTransferTransaction {
    public static String getAttachmentFromMassTransfer(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex).getMassTransfer().getAttachment().toByteArray());
    }

    public static String getAssetIdFromMassTransfer(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex).getMassTransfer().getAssetId().toByteArray());
    }

    public static long getRecipientAmountFromMassTransfer(int txIndex, int transferIndex) {
        return getTransactionAtIndex(txIndex).getMassTransfer().getTransfers(transferIndex).getAmount();
    }


    public static String getRecipientPublicKeyHashFromMassTransfer(int txIndex, int transferIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getMassTransfer()
                .getTransfers(transferIndex)
                .getRecipient()
                .getPublicKeyHash()
                .toByteArray());
    }
}
