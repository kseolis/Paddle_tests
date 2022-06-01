package im.mak.paddle.blockchain_updates;

import com.wavesplatform.transactions.IssueTransaction;
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
                () -> assertThat(getBalanceUpdate(0).getAmountBefore()).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getBalanceUpdate(0).getAmountAfter().getAmount()).isEqualTo(amountAfter),
                () -> assertThat(getTransactionId()).isEqualTo(txId)
        );
    }

    @Test
    @DisplayName("Check information on the issue transaction in the subscription")
    void subscribeTestForIssueTransaction() {
        IssueTransaction tx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)
                .script(SCRIPT_PERMITTING_OPERATIONS)).tx();

        height = node().getHeight();

        String assetId = tx.assetId().toString();
        String publicKey = account.publicKey().toString();
        long amountAfter = DEFAULT_FAUCET - ONE_WAVES;

        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getTransaction().getIssue().getName()).isEqualTo(assetName),
                () -> assertThat(getTransaction().getIssue().getDescription()).isEqualTo(assetDescription),
                () -> assertThat(getTransaction().getIssue().getAmount()).isEqualTo(assetQuantity),
                () -> assertThat(getTransaction().getIssue().getReissuable()).isEqualTo(true),
                () -> assertThat(getTransaction().getIssue().getDecimals()).isEqualTo(assetDecimals),
                () -> assertThat(getTransaction().getIssue().getScript().toByteArray()).isEqualTo(compileScript),
                () -> assertThat(getTransaction().getVersion()).isEqualTo(IssueTransaction.LATEST_VERSION),
                () -> assertThat(getTransaction().getFee().getAmount()).isEqualTo(ONE_WAVES),
                () -> assertThat(getTransactionId()).isEqualTo(tx.id().toString()),
                // check waves balance from balances
                () -> assertThat(getBalanceUpdate(0).getAmountBefore()).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getBalanceUpdate(0).getAmountAfter().getAmount()).isEqualTo(amountAfter),
                // check assetId and balance from balances
                () -> assertThat(getIssuedAssetIdFromBalance(0, 1)).isEqualTo(assetId),
                () -> assertThat(getBalanceUpdate(1).getAmountAfter().getAmount()).isEqualTo(assetQuantity),
                // check from assets
                () -> assertThat(getAssetIdFromAssets(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerFromAssets(0, 0)).isEqualTo(publicKey),
                // check asset info
                () -> assertThat(getAssets(0, 0).getName()).isEqualTo(assetName),
                () -> assertThat(getAssets(0, 0).getDescription()).isEqualTo(assetDescription),
                () -> assertThat(getAssets(0, 0).getVolume()).isEqualTo(assetQuantity),
                () -> assertThat(getAssets(0, 0).getDecimals()).isEqualTo(assetDecimals),
                () -> assertThat(getAssets(0, 0).getReissuable()).isEqualTo(true),
                () -> assertThat(getAssets(0, 0).getLastUpdated()).isEqualTo(height),
                () -> assertThat(getAssets(0, 0).getScriptInfo().getScript().toByteArray()).isEqualTo(compileScript)
        );
    }
}
