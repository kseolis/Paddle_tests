package im.mak.paddle.helpers;

import com.google.common.base.Functions;
import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdates;
import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdatesApiGrpc;
import com.wavesplatform.events.protobuf.Events;
import com.wavesplatform.protobuf.block.BlockOuterClass;
import com.wavesplatform.protobuf.transaction.TransactionOuterClass;
import im.mak.paddle.Account;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;

import java.util.Iterator;

import static com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdatesApiGrpc.newBlockingStub;

public class SubscribeHandlers {

    private static TransactionOuterClass.Transaction transaction;
    private static String transactionId;
    private static Events.BlockchainUpdated.Append append;
    private static BlockOuterClass.MicroBlock microBlockInfo;

    public static void subscribeResponseHandler(Channel channel, Account account, int fromHeight, int toHeight) {
        BlockchainUpdates.SubscribeRequest request = BlockchainUpdates.SubscribeRequest
                .newBuilder()
                .setFromHeight(fromHeight)
                .setToHeight(toHeight)
                .build();

        BlockchainUpdatesApiGrpc.BlockchainUpdatesApiBlockingStub stub = newBlockingStub(channel);
        Iterator<BlockchainUpdates.SubscribeEvent> subscribe = stub.subscribe(request);

        try {
            while (subscribe.hasNext()) {
                subscribeEventHandler(subscribe.next().getUpdate(), account);
            }
        } catch (StatusRuntimeException ignored) {
        }
    }

    private static void subscribeEventHandler(Events.BlockchainUpdated subscribeEventUpdate, Account account) {
        append = subscribeEventUpdate.getAppend();
        microBlockInfo = append
                .getMicroBlock()
                .getMicroBlock()
                .getMicroBlock();


        if (microBlockInfo.getTransactionsCount() > 0) {

            String transactionSenderPublicKey = Base58.encode(microBlockInfo
                    .getTransactions(0)
                    .getTransaction()
                    .getSenderPublicKey()
                    .toByteArray());

            transactionId = Base58.encode(append.getTransactionIds(0).toByteArray());

            if (transactionSenderPublicKey.equalsIgnoreCase(account.publicKey().toString())) {
                transaction = microBlockInfo.getTransactions(0).getTransaction();
            }
        }
    }

    public static Events.BlockchainUpdated.Append getAppend() {
        return append;
    }

    public static BlockOuterClass.MicroBlock getMicroBlockInfo() {
        return microBlockInfo;
    }

    public static TransactionOuterClass.Transaction getTransaction() {
        return transaction;
    }

    public static String getTransactionId() {
        return transactionId;
    }

    public static Events.StateUpdate getTransactionStateUpdate(int index) {
        return append.getTransactionStateUpdates(index);
    }

    public static Events.StateUpdate.BalanceUpdate getBalanceUpdate(int index) {
        return append.getTransactionStateUpdates(0).getBalances(index);
    }

    public static String getIssuedAssetIdFromBalance(int txStateUpdIndex, int balanceUpdIndex) {
        return Base58.encode(append
                .getTransactionStateUpdates(txStateUpdIndex)
                .getBalances(balanceUpdIndex)
                .getAmountAfter().getAssetId().toByteArray());
    }

    public static Events.StateUpdate.AssetDetails getAssets(int txStateUpdIndex, int assetIndex) {
        return append.getTransactionStateUpdates(txStateUpdIndex)
                .getAssets(assetIndex)
                .getAfter();
    }

    public static String getAssetIdFromAssets(int txStateUpdIndex, int assetIndex) {
       return Base58.encode(getAssets(txStateUpdIndex, assetIndex).getAssetId().toByteArray());
    }

    public static String getIssuerFromAssets(int txStateUpdIndex, int assetIndex) {
        return Base58.encode(getAssets(txStateUpdIndex, assetIndex).getIssuer().toByteArray());
    }

    public static String getTransferTransactionPublicKeyHash() {
        return Base58.encode(getTransaction()
                .getTransfer()
                .getRecipient()
                .getPublicKeyHash()
                .toByteArray());
    }

    public static String getAddressFromTransactionState(int txStateUpdIndex, int balancesIndex) {
        return Base58.encode(getAppend()
                .getTransactionStateUpdates(txStateUpdIndex)
                .getBalances(balancesIndex)
                .getAddress()
                .toByteArray());
    }

    public static Events.TransactionMetadata getElementTransactionMetadata(int metadataIndex) {
        return getAppend().getTransactionsMetadata(metadataIndex);
    }

    public static String getTransferRecipientFromTransactionMetadata(int index) {
        return Base58.encode(getElementTransactionMetadata(index)
                .getTransfer()
                .getRecipientAddress()
                .toByteArray());
    }
}
