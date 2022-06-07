package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates;

import com.wavesplatform.events.protobuf.Events;
import im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler;

import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.getAppend;

public class TransactionStateUpdates {
    public static Events.StateUpdate getTransactionStateUpdate(int index) {
        return getAppend().getTransactionStateUpdates(index);
    }
}
