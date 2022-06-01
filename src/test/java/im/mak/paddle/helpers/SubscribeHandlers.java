package im.mak.paddle.helpers;

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
    private static Events.StateUpdate.BalanceUpdate balanceUpdate;
    private static String addressFromSubscribeEvent;

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
        BlockOuterClass.MicroBlock microBlockInfo = append
                .getMicroBlock()
                .getMicroBlock()
                .getMicroBlock();


        if (microBlockInfo.getTransactionsCount() > 0) {
            balanceUpdate = append.getTransactionStateUpdates(0).getBalances(0);

            String senderPublicKey = Base58.encode(microBlockInfo
                    .getTransactions(0)
                    .getTransaction()
                    .getSenderPublicKey()
                    .toByteArray());

            addressFromSubscribeEvent = Base58.encode(balanceUpdate
                    .getAddress()
                    .toByteArray());

            transactionId = Base58.encode(append.getTransactionIds(0).toByteArray());

            if (senderPublicKey.equalsIgnoreCase(account.publicKey().toString())) {
                transaction = microBlockInfo.getTransactions(0).getTransaction();
            }
        }
    }

    public static TransactionOuterClass.Transaction getTransaction() {
        return transaction;
    }

    public static Events.BlockchainUpdated.Append getAppend() {
        return append;
    }

    public static Events.StateUpdate.BalanceUpdate getBalanceUpdate() {
        return balanceUpdate;
    }

    public static String getAddressFromSubscribeEvent() {
        return addressFromSubscribeEvent;
    }

    public static String getTransactionId() {
        return transactionId;
    }

}
