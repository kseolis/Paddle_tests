package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.events.protobuf.Events;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.TransactionStateUpdates.getTransactionStateUpdate;

public class Balances {
    public static Events.StateUpdate.BalanceUpdate getBalanceUpdate(int txStateUpdIndex, int index) {
        return getTransactionStateUpdate(txStateUpdIndex).getBalances(index);
    }

    public static String getAddress(int txStateUpdIndex, int balancesIndex) {
        return Base58.encode(getBalanceUpdate(txStateUpdIndex, balancesIndex)
                .getAddress()
                .toByteArray());
    }

    public static String getIssuedAssetIdAmountAfter(int txStateUpdIndex, int balanceUpdIndex) {
        return Base58.encode(getBalanceUpdate(txStateUpdIndex, balanceUpdIndex)
                .getAmountAfter()
                .getAssetId()
                .toByteArray());
    }

    public static long getAmountBefore(int txStateUpdIndex, int balancesIndex) {
        return getBalanceUpdate(txStateUpdIndex, balancesIndex).getAmountBefore();
    }

    public static long getAmountAfter(int txStateUpdIndex, int balancesIndex) {
        return getBalanceUpdate(txStateUpdIndex, balancesIndex).getAmountAfter().getAmount();
    }
}
