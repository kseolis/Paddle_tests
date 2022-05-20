package im.mak.paddle.blockchain_updates;

import com.wavesplatform.events.protobuf.Events;
import com.wavesplatform.transactions.IssueTransaction;
import im.mak.paddle.Account;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.DEFAULT_FAUCET;

public class BaseTest {
    private static IssueTransaction issueTransaction;

    private static int height;

    protected static final Channel channel = ManagedChannelBuilder
            .forAddress("devnet1-htz-nbg1-3.wavesnodes.com", 6881)
            .usePlaintext()
            .build();

    @BeforeEach
    void setUp() {
        Account account = new Account(DEFAULT_FAUCET);
        issueTransaction = account.issue(i -> i.name("Tst_issue_asset")
                .description("tst_asset")
                .script("2 * 2 == 4")
        ).tx();
        height = node().getHeight();
    }

    @Test
    void baseTest() {
        Events.TransactionMetadata transactionMetadata = Events.TransactionMetadata.newBuilder().build();
        System.out.println("\n\n\n\n\n\n\n\n\n\n transactionMetadata " + transactionMetadata.getMetadataCase());

        Events.StateUpdate.AssetInfo assetInfo = Events.StateUpdate.AssetInfo.newBuilder().build();
        System.out.println("\n\n assetInfo " + assetInfo.getName());

        Events.BlockchainUpdated.Rollback rollback = Events.BlockchainUpdated.Rollback.newBuilder().build();

        Events.StateUpdate.AssetStateUpdate assetStateUpdate = Events.StateUpdate.AssetStateUpdate.newBuilder().build();
        System.out.println("\n\n assetStateUpdate before " + assetStateUpdate.getBefore());
        System.out.println("\n\n assetStateUpdate after " + assetStateUpdate.getAfter());

        Events.StateUpdate.BalanceUpdate balanceUpdate = Events.StateUpdate.BalanceUpdate.newBuilder().build();
        System.out.println("\n\n balanceUpdate before " + balanceUpdate.getAmountBefore());
        System.out.println("\n\n balanceUpdate after " + balanceUpdate.getAmountAfter());

        Events.StateUpdate.LeaseUpdate leaseUpdate = Events.StateUpdate.LeaseUpdate.newBuilder().build();
        System.out.println("\n\n leaseUpdate " + leaseUpdate.getLeaseId());

        Events.StateUpdate.DataEntryUpdate dataEntryUpdate = Events.StateUpdate.DataEntryUpdate.newBuilder().build();
        System.out.println("\n\n\n\n\n\n\n\n\n\n dataEntryUpdate " + dataEntryUpdate.getDataEntry());
    }

    public static IssueTransaction getIssueTransaction() {
        return issueTransaction;
    }

    public static int getHeight() {
        return height;
    }

    public static void setHeight(int height) {
        BaseTest.height = height;
    }
}
