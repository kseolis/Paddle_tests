package im.mak.paddle.blockchain_updates;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.common.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.CreateAliasTransaction.LATEST_VERSION;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.SubscribeHandlers.*;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SubscribeTest extends BaseTest {

    @Test
    @DisplayName("Check information on the alias creation transaction in the subscription")
    void subscribeTestForCreateAlias() {
        long amountAfter = DEFAULT_FAUCET - MIN_FEE;
        String address = account.address().toString();
        String txId = account.createAlias(newAlias).tx().id().toString();
        height = node().getHeight();
        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getTransaction().getCreateAlias().getAlias()).isEqualTo(newAlias),
                () -> assertThat(getTransaction().getVersion()).isEqualTo(LATEST_VERSION),
                () -> assertThat(getTransaction().getFee().getAmount()).isEqualTo(MIN_FEE),

                () -> assertThat(getAddressFromSubscribeEvent()).isEqualTo(address),
                () -> assertThat(getBalanceUpdate().getAmountBefore()).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getBalanceUpdate().getAmountAfter().getAmount()).isEqualTo(amountAfter),
                () -> assertThat(getTransactionId()).isEqualTo(txId)
        );
    }

    @Test
    @DisplayName("Check information on the issue transaction in the subscription")
    void subscribeTestForIssueTransaction() {
        account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)
                .script(SCRIPT_PERMITTING_OPERATIONS));
        height = node().getHeight();
        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getTransaction().getIssue().getName()).isEqualTo(assetName),
                () -> assertThat(getTransaction().getIssue().getDescription()).isEqualTo(assetDescription),
                () -> assertThat(getTransaction().getIssue().getAmount()).isEqualTo(assetQuantity),
                () -> assertThat(getTransaction().getIssue().getReissuable()).isEqualTo(true),
                () -> assertThat(getTransaction().getIssue().getDecimals()).isEqualTo(assetDecimals),
                () -> assertThat(getTransaction().getIssue().getScript().toByteArray()).isEqualTo(compileScript),
                () -> assertThat(getTransaction().getVersion()).isEqualTo(IssueTransaction.LATEST_VERSION),
                () -> assertThat(getTransaction().getFee().getAmount()).isEqualTo(ONE_WAVES)
        );
    }
}
