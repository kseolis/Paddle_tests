package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.events.protobuf.Events;

public class Leasing extends TransactionStateUpdates {
    public static Events.StateUpdate.LeasingUpdate getLeasingForAddress(int txStateUpdIndex, int index) {
        return getTransactionStateUpdate(txStateUpdIndex).getLeasingForAddress(index);
    }

    public static String getAddressFromLeasingForAddress(int txStateUpdIndex, int index) {
        return Base58.encode(getLeasingForAddress(txStateUpdIndex, index).getAddress().toByteArray());
    }

    public static long getOutAfterFromLeasingForAddress(int txStateUpdIndex, int index) {
        return getLeasingForAddress(txStateUpdIndex, index).getOutAfter();
    }

    public static long getInAfterFromLeasingForAddress(int txStateUpdIndex, int index) {
        return getLeasingForAddress(txStateUpdIndex, index).getInAfter();
    }

    public static Events.StateUpdate.LeaseUpdate getIndividualLeases(int txStateUpdIndex, int index) {
        return getTransactionStateUpdate(txStateUpdIndex).getIndividualLeases(index);
    }

    public static String getLeaseIdFromIndividualLeases(int txStateUpdIndex, int index) {
        return Base58.encode(getIndividualLeases(txStateUpdIndex, index).getLeaseId().toByteArray());
    }

    public static String getStatusAfterFromIndividualLeases(int txStateUpdIndex, int index) {
        return getIndividualLeases(txStateUpdIndex, index).getStatusAfter().toString();
    }

    public static long getAmountFromIndividualLeases(int txStateUpdIndex, int index) {
        return getIndividualLeases(txStateUpdIndex, index).getAmount();
    }

    public static String getSenderFromIndividualLeases(int txStateUpdIndex, int index) {
        return Base58.encode(getIndividualLeases(txStateUpdIndex, index).getSender().toByteArray());
    }

    public static String getRecipientFromIndividualLeases(int txStateUpdIndex, int index) {
        return Base58.encode(getIndividualLeases(txStateUpdIndex, index).getRecipient().toByteArray());
    }

    public static String getOriginalTransactionIdFromIndividualLeases(int txStateUpdIndex, int index) {
        return Base58.encode(getIndividualLeases(txStateUpdIndex, index).getOriginTransactionId().toByteArray());
    }
}
