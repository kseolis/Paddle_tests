package im.mak.paddle.blockchain_updates;

import im.mak.paddle.Account;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import org.junit.jupiter.api.BeforeEach;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.util.Constants.DEFAULT_FAUCET;

public class BaseTest {
    protected int height;
    protected String address;
    protected String publicKey;
    protected Account account;

    protected final String newAlias = randomNumAndLetterString(16);
    protected final String assetName = getRandomInt(1, 900000) + "asset";
    protected final String assetDescription = assetName + "test";

    protected final Channel channel = ManagedChannelBuilder
            .forAddress("devnet1-htz-nbg1-3.wavesnodes.com", 6881)
            .usePlaintext()
            .build();

    @BeforeEach
    void setUp() {
        account = new Account(DEFAULT_FAUCET);
        address = account.address().toString();
        publicKey = account.publicKey().toString();
    }
}
