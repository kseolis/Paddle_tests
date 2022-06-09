package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class MassTransferTransactionHandler {
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
