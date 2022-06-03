package im.mak.paddle.blockchain_updates;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.ReissueTransaction;
import com.wavesplatform.transactions.TransferTransaction;
import com.wavesplatform.transactions.common.Amount;
import im.mak.paddle.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.CreateAliasTransaction.LATEST_VERSION;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Assets.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.TransferTransactionMetadata.getTransferRecipientAddressFromTransactionMetadata;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.ReissueTransaction.getReissueAssetAmount;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.ReissueTransaction.getReissueAssetId;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.TransferTransaction.getTransferTransactionPublicKeyHash;
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
                () -> assertThat(getFirstTransaction().getCreateAlias().getAlias()).isEqualTo(newAlias),
                () -> assertThat(getFirstTransaction().getVersion()).isEqualTo(LATEST_VERSION),
                () -> assertThat(getFirstTransaction().getFee().getAmount()).isEqualTo(MIN_FEE),

                () -> assertThat(getAddressFromTransactionState(0, 0)).isEqualTo(address),
                () -> assertThat(getBalanceUpdateFromBalances(0, 0).getAmountBefore()).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getBalanceUpdateFromBalances(0, 0).getAmountAfter().getAmount()).isEqualTo(amountAfter),
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
        long amountAfter = DEFAULT_FAUCET - ONE_WAVES;

        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getFirstTransaction().getIssue().getName()).isEqualTo(assetName),
                () -> assertThat(getFirstTransaction().getIssue().getDescription()).isEqualTo(assetDescription),
                () -> assertThat(getFirstTransaction().getIssue().getAmount()).isEqualTo(assetQuantity),
                () -> assertThat(getFirstTransaction().getIssue().getReissuable()).isEqualTo(true),
                () -> assertThat(getFirstTransaction().getIssue().getDecimals()).isEqualTo(assetDecimals),
                () -> assertThat(getFirstTransaction().getIssue().getScript().toByteArray()).isEqualTo(compileScript),
                () -> assertThat(getFirstTransaction().getVersion()).isEqualTo(IssueTransaction.LATEST_VERSION),
                () -> assertThat(getFirstTransaction().getFee().getAmount()).isEqualTo(ONE_WAVES),
                () -> assertThat(getTransactionId()).isEqualTo(tx.id().toString()),
                () -> assertThat(getAddressFromTransactionState(0, 0)).isEqualTo(address),
                // check waves balance from balances
                () -> assertThat(getBalanceUpdateFromBalances(0, 0).getAmountBefore()).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getBalanceUpdateFromBalances(0, 0).getAmountAfter().getAmount()).isEqualTo(amountAfter),
                // check assetId and balance from balances
                () -> assertThat(getIssuedAssetIdFromBalance(0, 1)).isEqualTo(assetId),
                () -> assertThat(getBalanceUpdateFromBalances(0, 1).getAmountAfter().getAmount()).isEqualTo(assetQuantity),
                // check from assets
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerFromAssetAfter(0, 0)).isEqualTo(publicKey),
                // check asset info
                () -> assertThat(getAfterAsset(0, 0).getName()).isEqualTo(assetName),
                () -> assertThat(getAfterAsset(0, 0).getDescription()).isEqualTo(assetDescription),
                () -> assertThat(getAfterAsset(0, 0).getVolume()).isEqualTo(assetQuantity),
                () -> assertThat(getAfterAsset(0, 0).getDecimals()).isEqualTo(assetDecimals),
                () -> assertThat(getAfterAsset(0, 0).getReissuable()).isEqualTo(true),
                () -> assertThat(getAfterAsset(0, 0).getLastUpdated()).isEqualTo(height),
                () -> assertThat(getAfterAsset(0, 0).getScriptInfo().getScript().toByteArray()).isEqualTo(compileScript)
        );
    }

    @Test
    @DisplayName("Check information on the transfer transaction in the subscription")
    void subscribeTestForTransferTransaction() {
        Account recipient = new Account();
        String recipientAddress = recipient.address().toString();

        Amount amount = Amount.of(getRandomInt(1, 10000));
        long amountVal = amount.value();
        long amountAfter = DEFAULT_FAUCET - MIN_FEE - amountVal;

        String recipientPublicKeyHash = Base58.encode(recipient.address().publicKeyHash());

        TransferTransaction tx = account.transfer(recipient, amount).tx();
        height = node().getHeight();
        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getFirstTransaction().getTransfer().getAmount().getAmount()).isEqualTo(amountVal),
                () -> assertThat(getFirstTransaction().getVersion()).isEqualTo(TransferTransaction.LATEST_VERSION),
                () -> assertThat(getFirstTransaction().getFee().getAmount()).isEqualTo(MIN_FEE),
                () -> assertThat(getTransferTransactionPublicKeyHash(0)).isEqualTo(recipientPublicKeyHash),
                () -> assertThat(getTransactionId()).isEqualTo(tx.id().toString()),
                // check sender balance
                () -> assertThat(getAddressFromTransactionState(0, 0)).isEqualTo(address),
                () -> assertThat(getBalanceUpdateFromBalances(0, 0).getAmountBefore()).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getBalanceUpdateFromBalances(0, 0).getAmountAfter().getAmount()).isEqualTo(amountAfter),
                // check recipient balance
                () -> assertThat(getAddressFromTransactionState(0, 1)).isEqualTo(recipientAddress),
                () -> assertThat(getBalanceUpdateFromBalances(0, 1).getAmountBefore()).isEqualTo(0),
                () -> assertThat(getBalanceUpdateFromBalances(0, 1).getAmountAfter().getAmount()).isEqualTo(amountVal),
                // check recipient address
                () -> assertThat(getTransferRecipientAddressFromTransactionMetadata(0)).isEqualTo(recipientAddress)
        );
    }

    @Test
    @DisplayName("Check information on the reissue transaction in the subscription")
    void subscribeTestForReissueTransaction() {
        long amount = getRandomInt(100, 10000000);
        long reissueFee = MIN_FEE + EXTRA_FEE;
        IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)
                .script(SCRIPT_PERMITTING_OPERATIONS)).tx();

        ReissueTransaction reissueTx = account.reissue(amount, issueTx.assetId(), i -> i.reissuable(false)).tx();

        height = node().getHeight();
        long amountAfterVal = assetQuantity + amount;
        String assetId = issueTx.assetId().toString();
        long amountAfter = DEFAULT_FAUCET - ONE_WAVES;

        subscribeResponseHandler(channel, account, height, height);

        System.out.println(getAppend());

        assertAll(
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(reissueFee),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(ReissueTransaction.LATEST_VERSION),
                () -> assertThat(getReissueAssetAmount(0)).isEqualTo(amount),
                () -> assertThat(getReissueAssetId(0)).isEqualTo(assetId),
                () -> assertThat(getTransactionId()).isEqualTo(reissueTx.id().toString()),

                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerFromAssetAfter(0, 0)).isEqualTo(publicKey)
        );
    }
}
