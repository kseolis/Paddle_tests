package im.mak.paddle.blockchain_updates;

import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdates.GetBlockUpdateRequest;
import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdates.GetBlockUpdateResponse;
import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdatesApiGrpc.BlockchainUpdatesApiBlockingStub;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

import static com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdatesApiGrpc.newBlockingStub;

class GetBlockUpdateTest extends BaseTest {

    @Test
    void getBlockUpdateBaseTest() throws UnsupportedOperationException {
        GetBlockUpdateRequest request = GetBlockUpdateRequest
                .newBuilder()
                .setHeight(height)
                .build();

        BlockchainUpdatesApiBlockingStub stub = newBlockingStub(channel);

        GetBlockUpdateResponse response = stub.getBlockUpdate(request);

        System.out.println(response);

        assertThat(response).isNotNull();
    }
}
