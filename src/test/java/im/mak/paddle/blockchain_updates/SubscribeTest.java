package im.mak.paddle.blockchain_updates;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.transactions.*;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Assets.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.TransferTransactionMetadata.getTransferRecipientAddressFromTransactionMetadata;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.AliasTransaction.getAliasFromAliasTransaction;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.BurnTransaction.getBurnAssetAmount;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.BurnTransaction.getBurnAssetId;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.IssueTransaction.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.ReissueTransaction.getReissueAssetAmount;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.ReissueTransaction.getReissueAssetId;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.SetAssetScriptTransaction.getAssetIdFromSetAssetScript;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.SetAssetScriptTransaction.getScriptFromSetAssetScript;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.SponsorFeeTransaction.getAssetIdFromSponsorFee;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.SponsorFeeTransaction.getAmountFromSponsorFee;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.TransferTransaction.*;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SubscribeTest extends BaseTest {

    @Test
    @DisplayName("Check subscription on alias transaction")
    void subscribeTestForCreateAlias() {
        long amountAfter = DEFAULT_FAUCET - MIN_FEE;
        String txId = account.createAlias(newAlias).tx().id().toString();
        height = node().getHeight();
        subscribeResponseHandler(channel, account, height, height);

        System.out.println(getAppend());
        assertAll(
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getAliasFromAliasTransaction(0)).isEqualTo(newAlias),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(CreateAliasTransaction.LATEST_VERSION),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE),

                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(amountAfter),
                () -> assertThat(getTransactionId()).isEqualTo(txId)
        );
    }

    @Test
    @DisplayName("Check subscription on issue transaction")
    void subscribeTestForIssueTransaction() {
        final int assetQuantity = getRandomInt(1000, 999_999_999);
        final int assetDecimals = getRandomInt(0, 8);
        final byte[] compileScript = node().compileScript(SCRIPT_PERMITTING_OPERATIONS).script().bytes();
        final long amountAfter = DEFAULT_FAUCET - ONE_WAVES;

        IssueTransaction tx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)
                .script(SCRIPT_PERMITTING_OPERATIONS)).tx();

        height = node().getHeight();

        String assetId = tx.assetId().toString();

        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getAssetName(0)).isEqualTo(assetName),
                () -> assertThat(getAssetDescription(0)).isEqualTo(assetDescription),
                () -> assertThat(getAssetAmount(0)).isEqualTo(assetQuantity),
                () -> assertThat(getAssetReissuable(0)).isEqualTo(true),
                () -> assertThat(getAssetDecimals(0)).isEqualTo(assetDecimals),
                () -> assertThat(getAssetScript(0)).isEqualTo(compileScript),
                () -> assertThat(getFirstTransaction().getVersion()).isEqualTo(IssueTransaction.LATEST_VERSION),
                () -> assertThat(getFirstTransaction().getFee().getAmount()).isEqualTo(ONE_WAVES),
                () -> assertThat(getTransactionId()).isEqualTo(tx.id().toString()),
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                // check waves balance from balances
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(amountAfter),
                // check assetId and balance from balances
                () -> assertThat(getIssuedAssetId(0, 1)).isEqualTo(assetId),
                () -> assertThat(getAmountBefore(0, 1)).isEqualTo(0),
                () -> assertThat(getAmountAfter(0, 1)).isEqualTo(assetQuantity),
                // check from assets
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                // check asset info
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(true),
                () -> assertThat(getScriptAfter(0, 0)).isEqualTo(compileScript)
        );
    }

    @Test
    @DisplayName("Check subscription on transfer transaction")
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
                () -> assertThat(getTransferAssetAmount(0)).isEqualTo(amountVal),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(TransferTransaction.LATEST_VERSION),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE),
                () -> assertThat(getTransferAssetId(0)).isEqualTo(""),
                () -> assertThat(getTransferTransactionPublicKeyHash(0)).isEqualTo(recipientPublicKeyHash),
                () -> assertThat(getTransactionId()).isEqualTo(tx.id().toString()),
                // check sender balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(amountAfter),
                // check recipient balance
                () -> assertThat(getAddress(0, 1)).isEqualTo(recipientAddress),
                () -> assertThat(getAmountBefore(0, 1)).isEqualTo(0),
                () -> assertThat(getAmountAfter(0, 1)).isEqualTo(amountVal),
                // check recipient address
                () -> assertThat(getTransferRecipientAddressFromTransactionMetadata(0)).isEqualTo(recipientAddress)
        );
    }

    @Test
    @DisplayName("Check subscription on reissue smart asset transaction")
    void subscribeTestForReissueSmartAsset() {
        final long amount = getRandomInt(100, 10000000);
        final long reissueFee = MIN_FEE + EXTRA_FEE;
        final long wavesAmountBeforeReissue = DEFAULT_FAUCET - ONE_WAVES;
        final long wavesAmountAfterReissue = wavesAmountBeforeReissue - reissueFee;
        final int assetQuantity = getRandomInt(1000, 999_999_999);
        final int assetDecimals = getRandomInt(0, 8);
        final byte[] compileScript = node().compileScript(SCRIPT_PERMITTING_OPERATIONS).script().bytes();
        final long quantityAfterReissue = assetQuantity + amount;

        IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)
                .script(SCRIPT_PERMITTING_OPERATIONS)).tx();

        ReissueTransaction reissueTx = account.reissue(amount, issueTx.assetId(), i -> i.reissuable(false)).tx();

        height = node().getHeight();
        String assetId = issueTx.assetId().toString();

        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(reissueFee),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(ReissueTransaction.LATEST_VERSION),
                () -> assertThat(getReissueAssetAmount(0)).isEqualTo(amount),
                () -> assertThat(getReissueAssetId(0)).isEqualTo(assetId),
                () -> assertThat(getTransactionId()).isEqualTo(reissueTx.id().toString()),
                // check waves balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(wavesAmountBeforeReissue),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(wavesAmountAfterReissue),
                // check asset balance
                () -> assertThat(getAddress(0, 1)).isEqualTo(address),
                () -> assertThat(getIssuedAssetId(0, 1)).isEqualTo(assetId),
                () -> assertThat(getAmountBefore(0, 1)).isEqualTo(assetQuantity),
                () -> assertThat(getAmountAfter(0, 1)).isEqualTo(quantityAfterReissue),
                // check asset before reissue
                () -> assertThat(getAssetIdFromAssetBefore(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerBefore(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityBefore(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getReissuableBefore(0, 0)).isEqualTo(true),
                () -> assertThat(getNameBefore(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionBefore(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsBefore(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getScriptBefore(0, 0)).isEqualTo(compileScript),
                // check asset after reissue
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(quantityAfterReissue),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(false),
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getScriptAfter(0, 0)).isEqualTo(compileScript)
        );
    }

    @Test
    @DisplayName("Check subscription on reissue asset transaction")
    void subscribeTestForReissueAsset() {
        final long amount = getRandomInt(100, 10000000);
        final long wavesAmountBeforeReissue = DEFAULT_FAUCET - ONE_WAVES;
        final long wavesAmountAfterReissue = wavesAmountBeforeReissue - MIN_FEE;
        final int assetQuantity = getRandomInt(1000, 999_999_999);
        final int assetDecimals = getRandomInt(0, 8);
        final long quantityAfterReissue = assetQuantity + amount;

        IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)).tx();

        ReissueTransaction reissueTx = account.reissue(amount, issueTx.assetId(), i -> i.reissuable(false)).tx();

        height = node().getHeight();
        String assetId = issueTx.assetId().toString();

        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(ReissueTransaction.LATEST_VERSION),
                () -> assertThat(getReissueAssetAmount(0)).isEqualTo(amount),
                () -> assertThat(getReissueAssetId(0)).isEqualTo(assetId),
                () -> assertThat(getTransactionId()).isEqualTo(reissueTx.id().toString()),
                // check waves balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(wavesAmountBeforeReissue),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(wavesAmountAfterReissue),
                // check asset balance
                () -> assertThat(getAddress(0, 1)).isEqualTo(address),
                () -> assertThat(getIssuedAssetId(0, 1)).isEqualTo(assetId),
                () -> assertThat(getAmountBefore(0, 1)).isEqualTo(assetQuantity),
                () -> assertThat(getAmountAfter(0, 1)).isEqualTo(quantityAfterReissue),
                // check asset before reissue
                () -> assertThat(getAssetIdFromAssetBefore(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerBefore(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityBefore(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getReissuableBefore(0, 0)).isEqualTo(true),
                () -> assertThat(getNameBefore(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionBefore(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsBefore(0, 0)).isEqualTo(assetDecimals),
                // check asset after reissue
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(quantityAfterReissue),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(false),
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals)
        );
    }

    @Test
    @DisplayName("Check subscription on burn smart asset transaction")
    void subscribeTestForBurnSmartAssetTransaction() {
        final long amount = getRandomInt(100, 10000000);
        final long burnSmartAssetFee = MIN_FEE + EXTRA_FEE;
        final long wavesAmountBeforeBurn = DEFAULT_FAUCET - ONE_WAVES;
        final long wavesAmountAfterBurn = wavesAmountBeforeBurn - burnSmartAssetFee;
        final int assetQuantity = getRandomInt(1000, 999_999_999);
        final int assetDecimals = getRandomInt(0, 8);
        final byte[] compileScript = node().compileScript(SCRIPT_PERMITTING_OPERATIONS).script().bytes();
        final long quantityAfterBurn = assetQuantity - amount;

        IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)
                .script(SCRIPT_PERMITTING_OPERATIONS)).tx();

        BurnTransaction burnTx = account.burn(amount, issueTx.assetId()).tx();

        height = node().getHeight();
        String assetId = issueTx.assetId().toString();

        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(burnSmartAssetFee),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(BurnTransaction.LATEST_VERSION),
                () -> assertThat(getBurnAssetId(0)).isEqualTo(assetId),
                () -> assertThat(getBurnAssetAmount(0)).isEqualTo(amount),
                () -> assertThat(getTransactionId()).isEqualTo(burnTx.id().toString()),
                // check waves balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(wavesAmountBeforeBurn),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(wavesAmountAfterBurn),
                // check asset balance
                () -> assertThat(getAddress(0, 1)).isEqualTo(address),
                () -> assertThat(getIssuedAssetId(0, 1)).isEqualTo(assetId),
                () -> assertThat(getAmountBefore(0, 1)).isEqualTo(assetQuantity),
                () -> assertThat(getAmountAfter(0, 1)).isEqualTo(quantityAfterBurn),
                // check asset before burn
                () -> assertThat(getAssetIdFromAssetBefore(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerBefore(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityBefore(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getReissuableBefore(0, 0)).isEqualTo(true),
                () -> assertThat(getNameBefore(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionBefore(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsBefore(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getScriptBefore(0, 0)).isEqualTo(compileScript),
                // check asset after burn
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(quantityAfterBurn),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(true),
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getScriptAfter(0, 0)).isEqualTo(compileScript)
        );
    }

    @Test
    @DisplayName("Check subscription on burn asset transaction")
    void subscribeTestForBurnAssetTransaction() {
        final long amount = getRandomInt(100, 10000000);
        final long wavesAmountBeforeBurn = DEFAULT_FAUCET - ONE_WAVES;
        final long wavesAmountAfterBurn = wavesAmountBeforeBurn - MIN_FEE;
        final int assetQuantity = getRandomInt(1000, 999_999_999);
        final int assetDecimals = getRandomInt(0, 8);
        final long quantityAfterBurn = assetQuantity - amount;

        IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)).tx();

        BurnTransaction burnTx = account.burn(amount, issueTx.assetId()).tx();

        height = node().getHeight();
        String assetId = issueTx.assetId().toString();

        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(BurnTransaction.LATEST_VERSION),
                () -> assertThat(getBurnAssetId(0)).isEqualTo(assetId),
                () -> assertThat(getBurnAssetAmount(0)).isEqualTo(amount),
                () -> assertThat(getTransactionId()).isEqualTo(burnTx.id().toString()),
                // check waves balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(wavesAmountBeforeBurn),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(wavesAmountAfterBurn),
                // check asset balance
                () -> assertThat(getAddress(0, 1)).isEqualTo(address),
                () -> assertThat(getIssuedAssetId(0, 1)).isEqualTo(assetId),
                () -> assertThat(getAmountBefore(0, 1)).isEqualTo(assetQuantity),
                () -> assertThat(getAmountAfter(0, 1)).isEqualTo(quantityAfterBurn),
                // check asset before burn
                () -> assertThat(getAssetIdFromAssetBefore(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerBefore(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityBefore(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getReissuableBefore(0, 0)).isEqualTo(true),
                () -> assertThat(getNameBefore(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionBefore(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsBefore(0, 0)).isEqualTo(assetDecimals),
                // check asset after burn
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(quantityAfterBurn),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(true),
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals)
        );
    }

    @Test
    @DisplayName("Check subscription on setAssetScript smart asset transaction")
    void subscribeTestForSetAssetScriptTransaction() {
        final long wavesAmountBeforeSetAssetScript = DEFAULT_FAUCET - ONE_WAVES;
        final long wavesAmountAfterSetAssetScript = wavesAmountBeforeSetAssetScript - ONE_WAVES;
        final int assetQuantity = getRandomInt(1000, 999_999_999);
        final int assetDecimals = getRandomInt(0, 8);
        final byte[] firstScript = node().compileScript(SCRIPT_PERMITTING_OPERATIONS).script().bytes();
        final Base64String newScript = node()
                .compileScript(fromFile("ride_scripts/permissionOnUpdatingKeyValues.ride")).script();

        IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)
                .script(SCRIPT_PERMITTING_OPERATIONS)).tx();
        AssetId assetId = issueTx.assetId();
        String assetIdToString = assetId.toString();

        SetAssetScriptTransaction setAssetScriptTx = account.setAssetScript(assetId, newScript).tx();

        height = node().getHeight();

        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(ONE_WAVES),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(SetAssetScriptTransaction.LATEST_VERSION),
                () -> assertThat(getAssetIdFromSetAssetScript(0)).isEqualTo(assetIdToString),
                () -> assertThat(getScriptFromSetAssetScript(0)).isEqualTo(newScript.bytes()),
                () -> assertThat(getTransactionId()).isEqualTo(setAssetScriptTx.id().toString()),
                // check waves balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(wavesAmountBeforeSetAssetScript),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(wavesAmountAfterSetAssetScript),
                // check asset before set asset script
                () -> assertThat(getAssetIdFromAssetBefore(0, 0)).isEqualTo(assetIdToString),
                () -> assertThat(getIssuerBefore(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityBefore(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getReissuableBefore(0, 0)).isEqualTo(true),
                () -> assertThat(getNameBefore(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionBefore(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsBefore(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getScriptBefore(0, 0)).isEqualTo(firstScript),
                () -> assertThat(getScriptComplexityBefore(0, 0)).isEqualTo(0),
                // check asset after set asset script
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetIdToString),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(true),
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getScriptAfter(0, 0)).isEqualTo(newScript.bytes()),
                () -> assertThat(getScriptComplexityAfter(0, 0)).isEqualTo(15)
        );
    }

    @Test
    @DisplayName("Check subscription on sponsorFee asset transaction")
    void subscribeTestForSponsorFeeTransaction() {
        final long transactionFee = MIN_FEE + EXTRA_FEE;
        final long wavesAmountBefore = DEFAULT_FAUCET - ONE_WAVES;
        final long wavesAmountAfter = wavesAmountBefore - transactionFee;
        final long sponsorFeeAmount = getRandomInt(100, 100000);
        final int assetQuantity = getRandomInt(1000, 999_999_999);
        final int assetDecimals = getRandomInt(0, 8);

        IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)).tx();
        AssetId assetId = issueTx.assetId();
        String assetIdToString = assetId.toString();

        SponsorFeeTransaction sponsorFeeTx = account.sponsorFee(assetId, sponsorFeeAmount,
                i -> i.additionalFee(EXTRA_FEE)).tx();

        height = node().getHeight();

        subscribeResponseHandler(channel, account, height, height);

        assertAll(
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(transactionFee),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(SponsorFeeTransaction.LATEST_VERSION),
                () -> assertThat(getAssetIdFromSponsorFee(0)).isEqualTo(assetIdToString),
                () -> assertThat(getAmountFromSponsorFee(0)).isEqualTo(sponsorFeeAmount),
                () -> assertThat(getTransactionId()).isEqualTo(sponsorFeeTx.id().toString()),
                // check waves balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(wavesAmountBefore),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(wavesAmountAfter),
                // check asset before sponsor fee transaction
                () -> assertThat(getAssetIdFromAssetBefore(0, 0)).isEqualTo(assetIdToString),
                () -> assertThat(getIssuerBefore(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getDecimalsBefore(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getNameBefore(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionBefore(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getReissuableBefore(0, 0)).isEqualTo(true),
                () -> assertThat(getQuantityBefore(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getScriptComplexityBefore(0, 0)).isEqualTo(0),
                // check asset after sponsor fee transaction
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetIdToString),
                () -> assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getNameAfter(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getReissuableAfter(0, 0)).isEqualTo(true),
                () -> assertThat(getQuantityAfter(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getScriptComplexityAfter(0, 0)).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("Check subscription on Exchange transaction")
    void subscribeTestForExchangeTransaction() {

    }
}
