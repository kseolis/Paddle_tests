package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.getTransactionAtIndex;

public class SetAssetScriptTransaction {
    public static String getAssetIdFromSetAssetScript(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getSetAssetScript()
                .getAssetId()
                .toByteArray());
    }

    public static byte[] getScriptFromSetAssetScript(int txIndex) {
        return getTransactionAtIndex(txIndex)
                .getSetAssetScript()
                .getScript()
                .toByteArray();
    }
}
