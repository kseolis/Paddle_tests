package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.events.protobuf.Events;
import im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.TransactionStateUpdates;

public class Balances extends TransactionStateUpdates {

    public static Events.StateUpdate.BalanceUpdate getBalanceUpdateFromBalances(int txStateUpdIndex, int index) {
        return getTransactionStateUpdate(txStateUpdIndex).getBalances(index);
    }

    public static String getIssuedAssetIdFromBalance(int txStateUpdIndex, int balanceUpdIndex) {
        return Base58.encode(getBalanceUpdateFromBalances(txStateUpdIndex, balanceUpdIndex)
                .getAmountAfter()
                .getAssetId()
                .toByteArray());
    }

    public static String getAddressFromTransactionState(int txStateUpdIndex, int balancesIndex) {
        return Base58.encode(getBalanceUpdateFromBalances(txStateUpdIndex, balancesIndex)
                .getAddress()
                .toByteArray());
    }
}
