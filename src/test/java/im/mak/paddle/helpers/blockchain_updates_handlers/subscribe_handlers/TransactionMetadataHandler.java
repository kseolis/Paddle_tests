package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.events.protobuf.Events;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.getAppend;

public class TransactionMetadataHandler {
    public static Events.TransactionMetadata getElementTransactionMetadata(int metadataIndex) {
        return getAppend().getTransactionsMetadata(metadataIndex);
    }

    public static String getLeaseTransactionMetadata(int metadataIndex) {
        return Base58.encode(getElementTransactionMetadata(metadataIndex)
                .getLeaseMeta()
                .getRecipientAddress()
                .toByteArray());
    }

    public static String getTransferRecipientAddressFromTransactionMetadata(int index) {
        return Base58.encode(getElementTransactionMetadata(index)
                .getTransfer()
                .getRecipientAddress()
                .toByteArray());
    }

    public static String getMassTransferFromTransactionMetadata(int index, int addressIndex) {
        return Base58.encode(getElementTransactionMetadata(index)
                .getMassTransfer()
                .getRecipientsAddresses(addressIndex)
                .toByteArray());
    }
}
