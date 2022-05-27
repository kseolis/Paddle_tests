package im.mak.paddle.blockchain_updates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.protobuf.format.JsonFormat;
import com.wavesplatform.crypto.base.Base64;
import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdates;
import com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdatesApiGrpc;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.blockchain_updates.serialized_json.Update;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.internal.ClazzCreator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static com.wavesplatform.events.api.grpc.protobuf.BlockchainUpdatesApiGrpc.newBlockingStub;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;

public class SubscribeTest extends BaseTest {
    @Test
    void subscribeTestForCreateAlias() {
        String newAlias = randomNumAndLetterString(16);
        account.createAlias(newAlias);
        checkSubscribeResponse();
    }

    private void checkSubscribeResponse() {
        BlockchainUpdates.SubscribeRequest request = BlockchainUpdates.SubscribeRequest
                .newBuilder()
                .setFromHeight(getHeight() - 1)
                .setToHeight(getHeight())
                .build();

        BlockchainUpdatesApiGrpc.BlockchainUpdatesApiBlockingStub stub = newBlockingStub(channel);
        Iterator<BlockchainUpdates.SubscribeEvent> subscribe = stub.subscribe(request);

        checkJsonSchema(subscribe);
    }


    private void checkJsonSchema(Iterator<BlockchainUpdates.SubscribeEvent> subscribe) {
        String response;
        Update deserialized;
        try {
            while (subscribe.hasNext()) {
                response = new JsonFormat().printToString(subscribe.next())
                        .replaceAll("\\\\[va]", "±");
                System.out.println(response);
                deserialized = deserialize(response, Update.class);
                System.out.println(deserialized);
            }
        } catch (StatusRuntimeException ignored) {
        }
    }

    public <T> T deserialize(String jsonString, Class<T> clazz) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(jsonString, clazz);
    }
}
