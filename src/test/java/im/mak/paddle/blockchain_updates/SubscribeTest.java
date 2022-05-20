package im.mak.paddle.blockchain_updates;

import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdates;
import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdatesApiGrpc;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static com.google.common.truth.Truth.assertThat;
import static com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdatesApiGrpc.newBlockingStub;

public class SubscribeTest extends BaseTest {

    @Test
    void subscribeTest() {
        int fromHeight = getHeight() - 10;

        BlockchainUpdates.SubscribeRequest subscribeRequest = BlockchainUpdates.SubscribeRequest
                .newBuilder()
                .setFromHeight(fromHeight)
                .setToHeight(getHeight())
                .build();

        BlockchainUpdatesApiGrpc.BlockchainUpdatesApiBlockingStub stub = newBlockingStub(channel);

        Iterator<BlockchainUpdates.SubscribeEvent> subscribe = stub.subscribe(subscribeRequest);

        try {
            while (subscribe.hasNext()) {
                System.out.println(subscribe.next());
                assertThat(subscribe).isNotNull();
            }
        } catch (StatusRuntimeException ignored) {}
    }
}
