package im.mak.paddle.blockchain_updates;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

public class BaseTest {
    protected int height;

    protected final Channel channel = ManagedChannelBuilder
            .forAddress("devnet1-htz-nbg1-3.wavesnodes.com", 6881)
            .usePlaintext()
            .build();
}
