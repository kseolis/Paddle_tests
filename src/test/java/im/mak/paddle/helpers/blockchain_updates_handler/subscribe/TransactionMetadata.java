package im.mak.paddle.helpers.blockchain_updates_handler.subscribe;

import com.wavesplatform.events.protobuf.Events;

public class TransactionMetadata extends SubscribeHandler {
    public static Events.TransactionMetadata getElementTransactionMetadata(int metadataIndex) {
        return getAppend().getTransactionsMetadata(metadataIndex);
    }
}
