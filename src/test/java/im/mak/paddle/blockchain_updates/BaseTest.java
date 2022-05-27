package im.mak.paddle.blockchain_updates;

import im.mak.paddle.Account;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import org.junit.jupiter.api.BeforeAll;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.DEFAULT_FAUCET;

public class BaseTest {
    private static int height;
    protected static Account account;

    protected static final Channel channel = ManagedChannelBuilder
            .forAddress("devnet1-htz-nbg1-3.wavesnodes.com", 6881)
            .usePlaintext()
            .build();

    @BeforeAll
    static void setUp() {
        height = node().getHeight();
        account = new Account(DEFAULT_FAUCET);
    }

    public static int getHeight() {
        return height;
    }

    public static void setHeight(int height) {
        BaseTest.height = height;
    }
}
