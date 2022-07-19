package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers;

import com.google.protobuf.ByteString;
import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.events.protobuf.Events;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.TransactionMetadataHandler.getElementTransactionMetadata;

public class invokeTransactionMetadata {
    public static Events.TransactionMetadata.InvokeScriptMetadata getInvokeMetadata(int metadataIndex) {
        return getElementTransactionMetadata(metadataIndex).getInvokeScript();
    }

    public static String getInvokeMetadataDAppAddress(int metadataIndex) {
        return Base58.encode(getInvokeMetadata(metadataIndex).getDAppAddress().toByteArray());
    }

    public static String getInvokeMetadataFunctionName(int metadataIndex) {
        return getInvokeMetadata(metadataIndex).getFunctionName();
    }

    public static long getInvokeMetadataArgIntegerValue(int metadataIndex, int argIndex) {
        return getInvokeMetadata(metadataIndex).getArguments(argIndex).getIntegerValue();
    }

    public static ByteString getInvokeMetadataArgBinaryValue(int metadataIndex, int argIndex) {
        return getInvokeMetadata(metadataIndex).getArguments(argIndex).getBinaryValue();
    }

    public static String getInvokeMetadataArgStringValue(int metadataIndex, int argIndex) {
        return getInvokeMetadata(metadataIndex).getArguments(argIndex).getStringValue();
    }

    public static boolean getInvokeMetadataArgBooleanValue(int metadataIndex, int argIndex) {
        return getInvokeMetadata(metadataIndex).getArguments(argIndex).getBooleanValue();
    }

    public static ByteString getInvokeMetadataArgStringValueBytes(int metadataIndex, int argIndex) {
        return getInvokeMetadata(metadataIndex).getArguments(argIndex).getStringValueBytes();
    }

    public static long getInvokeMetadataPaymentsAmount(int metadataIndex, int argIndex) {
        return getInvokeMetadata(metadataIndex).getPayments(argIndex).getAmount();
    }

    public static String getInvokeMetadataPaymentsAsset(int metadataIndex, int argIndex) {
        return Base58.encode(
                getInvokeMetadata(metadataIndex).getPayments(argIndex).getAssetId().toByteArray()
        );
    }

    public static String getInvokeMetadataResultDataKey(int metadataIndex, int dataIndex) {
        return getInvokeMetadata(metadataIndex).getResult().getData(dataIndex).getKey();
    }

    public static long getInvokeMetadataResultDataIntegerValue(int metadataIndex, int dataIndex) {
        return getInvokeMetadata(metadataIndex).getResult().getData(dataIndex).getIntValue();
    }

    public static String getInvokeMetadataResultDataStringValue(int metadataIndex, int dataIndex) {
        return getInvokeMetadata(metadataIndex).getResult().getData(dataIndex).getStringValue();
    }

    public static boolean getInvokeMetadataResultDataBoolValue(int metadataIndex, int dataIndex) {
        return getInvokeMetadata(metadataIndex).getResult().getData(dataIndex).getBoolValue();
    }
    public static ByteString getInvokeMetadataResultDataBinaryValue(int metadataIndex, int dataIndex) {
        return getInvokeMetadata(metadataIndex).getResult().getData(dataIndex).getBinaryValue();
    }

    public static ByteString getInvokeMetadataResultDataStringValueBytes(int metadataIndex, int dataIndex) {
        return getInvokeMetadata(metadataIndex).getResult().getData(dataIndex).getStringValueBytes();
    }
}
