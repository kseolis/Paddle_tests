package im.mak.paddle.blockchain_updates;

import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdates.GetBlockUpdatesRangeRequest;
import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdates.GetBlockUpdatesRangeResponse;

import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdatesApiGrpc.BlockchainUpdatesApiBlockingStub;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdatesApiGrpc.newBlockingStub;

class GetBlockUpdatesRangeTest extends BaseTest {
    @Test
    void getBlockUpdatesRangeTest() {
        GetBlockUpdatesRangeRequest request = GetBlockUpdatesRangeRequest
                .newBuilder()
                .setFromHeight(getHeight() - 10)
                .setToHeight(getHeight())
                .build();

        BlockchainUpdatesApiBlockingStub stub = newBlockingStub(channel);

        GetBlockUpdatesRangeResponse response = stub.getBlockUpdatesRange(request);

        assertThat(response).isNotNull();
    }
}