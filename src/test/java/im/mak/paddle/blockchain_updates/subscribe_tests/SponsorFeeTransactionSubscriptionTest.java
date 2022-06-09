package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.SponsorFeeTransaction;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.getTransactionId;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Assets.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Assets.getScriptComplexityAfter;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.SponsorFeeTransactionHandler.getAmountFromSponsorFee;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.SponsorFeeTransactionHandler.getAssetIdFromSponsorFee;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SponsorFeeTransactionSubscriptionTest extends BaseTest {
    private int assetQuantity;
    private int assetDecimals;
    private String address;
    private String publicKey;
    private Account account;
    private String assetName;
    private String assetDescription;
    private long sponsorFeeAmount;
    private final long transactionFee = MIN_FEE + EXTRA_FEE;
    private final long wavesAmountBefore = DEFAULT_FAUCET - ONE_WAVES;
    private final long wavesAmountAfter = wavesAmountBefore - transactionFee;

    @BeforeEach
    void setUp() {
        async(
                () -> assetDecimals = getRandomInt(0, 8),
                () -> sponsorFeeAmount = getRandomInt(100, 100000),
                () -> assetQuantity = getRandomInt(1000, 999_999_999),
                () -> {
                    assetName = getRandomInt(1, 900000) + "asset";
                    assetDescription = assetName + "test";
                },
                () -> {
                    account = new Account(DEFAULT_FAUCET);
                    address = account.address().toString();
                    publicKey = account.publicKey().toString();
                }
        );
    }

    @Test
    @DisplayName("Check subscription on sponsorFee asset transaction")
    void subscribeTestForSponsorFeeTransaction() {
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
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
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
}
