package im.mak.paddle.blockchain_updates;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.TransferTransaction;
import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
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
        String txId = account.createAlias(newAlias).tx().id().toString();
        height = node().getHeight();
        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getTransaction().getCreateAlias().getAlias()).isEqualTo(newAlias),
                () -> assertThat(getTransaction().getVersion()).isEqualTo(LATEST_VERSION),
                () -> assertThat(getTransaction().getFee().getAmount()).isEqualTo(MIN_FEE),

                () -> assertThat(getAddressFromTransactionState(0, 0)).isEqualTo(address),
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
                () -> assertThat(getAddressFromTransactionState(0, 0)).isEqualTo(address),
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

    @Test
    @DisplayName("Check information on the transfer transaction in the subscription")
    void subscribeTestForTransferTransaction() {
        Account recipient = new Account();
        String recipientAddress = recipient.address().toString();
        Amount amount = Amount.of(1);
        String recipientPublicKeyHash = Base58.encode(recipient.address().publicKeyHash());
        TransferTransaction tx = account.transfer(recipient, Amount.of(1)).tx();
        height = node().getHeight();
        subscribeResponseHandler(channel, account, height, height);
        long amountAfter = DEFAULT_FAUCET - MIN_FEE - amount.value();

        System.out.println(getAppend());

        assertAll(
                () -> assertThat(getTransaction().getTransfer().getAmount().getAmount()).isEqualTo(amount.value()),
                () -> assertThat(getTransaction().getVersion()).isEqualTo(TransferTransaction.LATEST_VERSION),
                () -> assertThat(getTransaction().getFee().getAmount()).isEqualTo(MIN_FEE),
                () -> assertThat(getTransactionId()).isEqualTo(tx.id().toString()),
                () -> assertThat(getTransferTransactionPublicKeyHash()).isEqualTo(recipientPublicKeyHash),
                // check sender balance
                () -> assertThat(getAddressFromTransactionState(0, 0)).isEqualTo(address),
                () -> assertThat(getBalanceUpdate(0).getAmountBefore()).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getBalanceUpdate(0).getAmountAfter().getAmount()).isEqualTo(amountAfter),
                // check recipient balance
                () -> assertThat(getAddressFromTransactionState(0, 1)).isEqualTo(recipientAddress),
                () -> assertThat(getBalanceUpdate(1).getAmountBefore()).isEqualTo(0),
                () -> assertThat(getBalanceUpdate(1).getAmountAfter().getAmount()).isEqualTo(amount.value())
        );
    }
}
