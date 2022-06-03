package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.events.protobuf.Events;

public class Balances extends TransactionStateUpdates {

    public static Events.StateUpdate.BalanceUpdate getBalanceUpdate(int txStateUpdIndex, int index) {
        return getTransactionStateUpdate(txStateUpdIndex).getBalances(index);
    }

    public static String getIssuedAssetId(int txStateUpdIndex, int balanceUpdIndex) {
        return Base58.encode(getBalanceUpdate(txStateUpdIndex, balanceUpdIndex)
                .getAmountAfter()
                .getAssetId()
                .toByteArray());
    }

    public static String getAddress(int txStateUpdIndex, int balancesIndex) {
        return Base58.encode(getBalanceUpdate(txStateUpdIndex, balancesIndex)
                .getAddress()
                .toByteArray());
    }

    public static long getAmountBefore(int txStateUpdIndex, int balancesIndex) {
        return getBalanceUpdate(txStateUpdIndex, balancesIndex).getAmountBefore();
    }

    public static long getAmountAfter(int txStateUpdIndex, int balancesIndex) {
        return getBalanceUpdate(txStateUpdIndex, balancesIndex).getAmountAfter().getAmount();
    }
}
