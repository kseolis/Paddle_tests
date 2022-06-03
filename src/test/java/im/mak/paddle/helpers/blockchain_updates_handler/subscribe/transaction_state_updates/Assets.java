package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.events.protobuf.Events;

public class Assets extends TransactionStateUpdates {

    public static Events.StateUpdate.AssetDetails getAfterAsset(int txStateUpdIndex, int assetIndex) {
        return getTransactionStateUpdate(txStateUpdIndex)
                .getAssets(assetIndex)
                .getAfter();
    }

    public static String getAssetIdFromAssetAfter(int txStateUpdIndex, int assetIndex) {
        return Base58.encode(getAfterAsset(txStateUpdIndex, assetIndex).getAssetId().toByteArray());
    }

    public static String getIssuerFromAssetAfter(int txStateUpdIndex, int assetIndex) {
        return Base58.encode(getAfterAsset(txStateUpdIndex, assetIndex).getIssuer().toByteArray());
    }
}
