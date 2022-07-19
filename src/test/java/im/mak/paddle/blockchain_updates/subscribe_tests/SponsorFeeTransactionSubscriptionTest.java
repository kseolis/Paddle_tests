package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import im.mak.paddle.dapps.DefaultDApp420Complexity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.SponsorFeeTransaction.LATEST_VERSION;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Assets.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Assets.getScriptComplexityAfter;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.SponsorFeeTransactionHandler.getAmountFromSponsorFee;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.SponsorFeeTransactionHandler.getAssetIdFromSponsorFee;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.helpers.transaction_senders.SponsorFeeTransactionSender.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SponsorFeeTransactionSubscriptionTest extends BaseTest {
    private static int assetQuantity;
    private static int assetDecimals;

    private static Account account;
    private static String accAddress;
    private static String accPublicKey;

    private static AssetId assetId;
    private static String assetIdToString;
    private static String assetName;
    private static String assetDescription;

    private static DefaultDApp420Complexity dAppAccount;
    private static String dAppAddress;
    private static String dAppPublicKey;


    private static long sponsorFeeAmount;
    private long wavesAmountBefore;
    private long wavesAmountAfter;

    @BeforeAll
    static void setUp() {
        async(
                () -> sponsorFeeAmount = getRandomInt(100, 100000),
                () -> {
                    account = new Account(DEFAULT_FAUCET);
                    accAddress = account.address().toString();
                    accPublicKey = account.publicKey().toString();
                },
                () -> {
                    dAppAccount = new DefaultDApp420Complexity(DEFAULT_FAUCET);
                    dAppAddress = dAppAccount.address().toString();
                    dAppPublicKey = dAppAccount.publicKey().toString();
                }
        );
    }

    @Test
    @DisplayName("Check subscription on sponsorFee asset transaction")
    void subscribeTestForSponsorFeeTransaction() {
        assetName = getRandomInt(1, 900000) + "asset";
        assetDescription = assetName + "test";
        assetQuantity = getRandomInt(1000, 999_999_999);
        assetDecimals = getRandomInt(0, 8);
        wavesAmountBefore = account.getWavesBalance() - ONE_WAVES;
        wavesAmountAfter = wavesAmountBefore - SUM_FEE;

        IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)).tx();
        assetId = issueTx.assetId();
        assetIdToString = assetId.toString();

        sponsorFeeTransactionSender(account, sponsorFeeAmount, assetId, SUM_FEE, LATEST_VERSION);
        height = node().getHeight();

        subscribeResponseHandler(channel, account, height, height);
        checkSponsorFeeSubscribe(assetIdToString, accAddress, accPublicKey, SUM_FEE);
    }

    @Test
    @DisplayName("Check subscription on sponsorFee DAppAccount asset transaction")
    void subscribeTestForDAppAccountSponsorFeeTransaction() {
        assetName = getRandomInt(1, 900000) + "asset DApp";
        assetDescription = assetName + "test DApp";
        assetQuantity = getRandomInt(1000, 999_999_999);
        assetDecimals = getRandomInt(0, 8);
        wavesAmountBefore = dAppAccount.getWavesBalance() - ONE_WAVES - EXTRA_FEE;
        wavesAmountAfter = wavesAmountBefore - SUM_FEE;
        IssueTransaction issueTx = dAppAccount.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)).tx();
        assetId = issueTx.assetId();
        assetIdToString = assetId.toString();

        sponsorFeeTransactionSender(dAppAccount, sponsorFeeAmount, assetId, SUM_FEE, LATEST_VERSION);
        height = node().getHeight();

        subscribeResponseHandler(channel, dAppAccount, height, height);
        checkSponsorFeeSubscribe(assetIdToString, dAppAddress, dAppPublicKey, SUM_FEE);
    }

    @Test
    @DisplayName("Check subscription on cancel sponsorFee asset transaction")
    void subscribeTestForCancelSponsorFeeTransaction() {
        assetName = getRandomInt(1, 900000) + "asset";
        assetDescription = assetName + "test";
        assetQuantity = getRandomInt(1000, 999_999_999);
        assetDecimals = getRandomInt(0, 8);
        sponsorFeeAmount = 0;
        wavesAmountBefore = account.getWavesBalance() - ONE_WAVES;
        wavesAmountAfter = wavesAmountBefore - MIN_FEE;
        IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)).tx();
        assetId = issueTx.assetId();
        assetIdToString = assetId.toString();

        cancelSponsorFeeSender(account, account, dAppAccount, assetId, LATEST_VERSION);

        height = node().getHeight();
        subscribeResponseHandler(channel, account, height, height);

        checkSponsorFeeSubscribe(assetIdToString, accAddress, accPublicKey, MIN_FEE);
    }

    private void checkSponsorFeeSubscribe(String assetId, String address, String publicKey, long fee) {
        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(fee),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(LATEST_VERSION),
                () -> assertThat(getAssetIdFromSponsorFee(0)).isEqualTo(assetId),
                () -> assertThat(getAmountFromSponsorFee(0)).isEqualTo(sponsorFeeAmount),
                () -> assertThat(getTransactionId()).isEqualTo(getSponsorTx().id().toString()),
                // check waves balance
                () -> assertThat(getAddress(0, 0)).isEqualTo(address),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(wavesAmountBefore),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(wavesAmountAfter),
                // check asset before sponsor fee transaction
                () -> assertThat(getAssetIdFromAssetBefore(0, 0)).isEqualTo(assetId),
                () -> assertThat(getIssuerBefore(0, 0)).isEqualTo(publicKey),
                () -> assertThat(getDecimalsBefore(0, 0)).isEqualTo(assetDecimals),
                () -> assertThat(getNameBefore(0, 0)).isEqualTo(assetName),
                () -> assertThat(getDescriptionBefore(0, 0)).isEqualTo(assetDescription),
                () -> assertThat(getReissuableBefore(0, 0)).isEqualTo(true),
                () -> assertThat(getQuantityBefore(0, 0)).isEqualTo(assetQuantity),
                () -> assertThat(getScriptComplexityBefore(0, 0)).isEqualTo(0),
                // check asset after sponsor fee transaction
                () -> assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId),
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

/*
        assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID);
        assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(publicKey);
        assertThat(getTransactionFeeAmount(0)).isEqualTo(fee);
        assertThat(getTransactionVersion(0)).isEqualTo(LATEST_VERSION);
        assertThat(getAssetIdFromSponsorFee(0)).isEqualTo(assetId);
        assertThat(getAmountFromSponsorFee(0)).isEqualTo(sponsorFeeAmount);
        assertThat(getTransactionId()).isEqualTo(getSponsorTx().id().toString());
        // check waves balance
        assertThat(getAddress(0, 0)).isEqualTo(address);
        assertThat(getAmountBefore(0, 0)).isEqualTo(wavesAmountBefore);
        assertThat(getAmountAfter(0, 0)).isEqualTo(wavesAmountAfter);
        // check asset before sponsor fee transaction
        assertThat(getAssetIdFromAssetBefore(0, 0)).isEqualTo(assetId);
        assertThat(getIssuerBefore(0, 0)).isEqualTo(publicKey);
        assertThat(getDecimalsBefore(0, 0)).isEqualTo(assetDecimals);
        assertThat(getNameBefore(0, 0)).isEqualTo(assetName);
        assertThat(getDescriptionBefore(0, 0)).isEqualTo(assetDescription);
        assertThat(getReissuableBefore(0, 0)).isEqualTo(true);
        assertThat(getQuantityBefore(0, 0)).isEqualTo(assetQuantity);
        assertThat(getScriptComplexityBefore(0, 0)).isEqualTo(0);
        // check asset after sponsor fee transaction
        assertThat(getAssetIdFromAssetAfter(0, 0)).isEqualTo(assetId);
        assertThat(getIssuerAfter(0, 0)).isEqualTo(publicKey);
        assertThat(getDecimalsAfter(0, 0)).isEqualTo(assetDecimals);
        assertThat(getNameAfter(0, 0)).isEqualTo(assetName);
        assertThat(getDescriptionAfter(0, 0)).isEqualTo(assetDescription);
        assertThat(getReissuableAfter(0, 0)).isEqualTo(true);
        assertThat(getQuantityAfter(0, 0)).isEqualTo(assetQuantity);
        assertThat(getScriptComplexityAfter(0, 0)).isEqualTo(0);
    */
