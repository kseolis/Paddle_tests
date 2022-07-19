package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.SetAssetScriptTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.Account;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.ONE_WAVES;

public class SetAssetScriptTransactionSender extends BaseTransactionSender {
    private static SetAssetScriptTransaction setAssetScriptTx;

    public static void setAssetScriptTransactionSender(Account account, Base64String script, AssetId assetId, int version) {
        balanceAfterTransaction = account.getWavesBalance() - ONE_WAVES;
        setAssetScriptTx = SetAssetScriptTransaction
                .builder(assetId, script).version(version).getSignedWith(account.privateKey());
        node().waitForTransaction(node().broadcast(setAssetScriptTx).id());
        txInfo = node().getTransactionInfo(setAssetScriptTx.id());
    }

    public static SetAssetScriptTransaction getSetAssetScriptTx() {
        return setAssetScriptTx;
    }

}
