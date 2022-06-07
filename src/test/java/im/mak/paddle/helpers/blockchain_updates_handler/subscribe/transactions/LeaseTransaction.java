package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.getTransactionAtIndex;

public class LeaseTransaction {
    public static String getLeaseTransactionPublicKeyHash(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getLease()
                .getRecipient()
                .getPublicKeyHash()
                .toByteArray());
    }

    public static long getLeaseAssetAmount(int txIndex) {
        return getTransactionAtIndex(txIndex).getLease().getAmount();
    }
}
