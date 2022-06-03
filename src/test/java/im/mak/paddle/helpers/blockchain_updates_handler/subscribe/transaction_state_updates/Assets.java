package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.events.protobuf.Events;

public class Assets extends TransactionStateUpdates {

    public static Events.StateUpdate.AssetDetails getAssetBefore(int txStateUpdIndex, int assetIndex) {
        return getTransactionStateUpdate(txStateUpdIndex)
                .getAssets(assetIndex)
                .getBefore();
    }

    public static Events.StateUpdate.AssetDetails getAssetAfter(int txStateUpdIndex, int assetIndex) {
        return getTransactionStateUpdate(txStateUpdIndex)
                .getAssets(assetIndex)
                .getAfter();
    }

    public static String getAssetIdFromAssetBefore(int txStateUpdIndex, int assetIndex) {
        return Base58.encode(getAssetBefore(txStateUpdIndex, assetIndex).getAssetId().toByteArray());
    }

    public static String getAssetIdFromAssetAfter(int txStateUpdIndex, int assetIndex) {
        return Base58.encode(getAssetAfter(txStateUpdIndex, assetIndex).getAssetId().toByteArray());
    }

    public static String getIssuerBefore(int txStateUpdIndex, int assetIndex) {
        return Base58.encode(getAssetBefore(txStateUpdIndex, assetIndex).getIssuer().toByteArray());
    }

    public static String getIssuerAfter(int txStateUpdIndex, int assetIndex) {
        return Base58.encode(getAssetAfter(txStateUpdIndex, assetIndex).getIssuer().toByteArray());
    }

    public static int getDecimalsBefore(int txStateUpdIndex, int assetIndex) {
        return getAssetBefore(txStateUpdIndex, assetIndex).getDecimals();
    }

    public static int getDecimalsAfter(int txStateUpdIndex, int assetIndex) {
        return getAssetAfter(txStateUpdIndex, assetIndex).getDecimals();
    }

    public static String getNameBefore(int txStateUpdIndex, int assetIndex) {
        return getAssetBefore(txStateUpdIndex, assetIndex).getName();
    }

    public static String getNameAfter(int txStateUpdIndex, int assetIndex) {
        return getAssetAfter(txStateUpdIndex, assetIndex).getName();
    }

    public static String getDescriptionBefore(int txStateUpdIndex, int assetIndex) {
        return getAssetBefore(txStateUpdIndex, assetIndex).getDescription();
    }

    public static String getDescriptionAfter(int txStateUpdIndex, int assetIndex) {
        return getAssetAfter(txStateUpdIndex, assetIndex).getDescription();
    }

    public static boolean getReissuableBefore(int txStateUpdIndex, int assetIndex) {
        return getAssetBefore(txStateUpdIndex, assetIndex).getReissuable();
    }

    public static boolean getReissuableAfter(int txStateUpdIndex, int assetIndex) {
        return getAssetAfter(txStateUpdIndex, assetIndex).getReissuable();
    }

    public static long getQuantityBefore(int txStateUpdIndex, int assetIndex) {
        return getAssetBefore(txStateUpdIndex, assetIndex).getVolume();
    }

    public static long getQuantityAfter(int txStateUpdIndex, int assetIndex) {
        return getAssetAfter(txStateUpdIndex, assetIndex).getVolume();
    }

    public static byte[] getScriptBefore(int txStateUpdIndex, int assetIndex) {
        return getAssetBefore(txStateUpdIndex, assetIndex).getScriptInfo().getScript().toByteArray();
    }

    public static byte[] getScriptAfter(int txStateUpdIndex, int assetIndex) {
        return getAssetAfter(txStateUpdIndex, assetIndex).getScriptInfo().getScript().toByteArray();
    }
}
