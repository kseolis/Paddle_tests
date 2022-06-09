package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates;

import com.wavesplatform.events.protobuf.Events;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.getAppend;

public class TransactionStateUpdates {
    public static Events.StateUpdate getTransactionStateUpdate(int index) {
        return getAppend().getTransactionStateUpdates(index);
    }
}
