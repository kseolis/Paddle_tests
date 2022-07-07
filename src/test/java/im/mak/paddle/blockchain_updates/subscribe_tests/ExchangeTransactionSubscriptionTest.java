package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.exchange.Order;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.ExchangeTransaction.LATEST_VERSION;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Calculations.*;
import static im.mak.paddle.helpers.Calculations.getSellerBalanceAfterTransactionAmountAsset;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.SubscribeHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.ExchangeTransactionHandler.*;
import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.*;
import static im.mak.paddle.helpers.transaction_senders.ExchangeTransactionSender.exchangeTransactionSender;
import static im.mak.paddle.helpers.transaction_senders.ExchangeTransactionSender.getExchangeTx;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.Constants.ORDER_V_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ExchangeTransactionSubscriptionTest extends BaseTest {
    private Account buyer;
    private PrivateKey buyerPrivateKey;
    private String buyerPublicKey;
    private String buyerAddress;

    private Account seller;
    private PrivateKey sellerPrivateKey;
    private String sellerPublicKey;
    private String sellerAddress;

    private AssetId assetId;

    private int assetQuantity;
    private long fee;
    private String assetName;
    private String assetDescription;
    private IssueTransaction issueTx;
    private Amount amount;
    private Amount price;
    private Order buy;
    private Order sell;
    private final int assetDecimals = 8;

    @BeforeEach
    void setUp() {
        async(
                () -> {
                    buyer = new Account(DEFAULT_FAUCET);
                    buyerPrivateKey = buyer.privateKey();
                    buyerAddress = buyer.address().toString();
                    buyerPublicKey = buyer.publicKey().toString();

                    assetName = getRandomInt(1, 900000) + "asset";
                    assetQuantity = getRandomInt(100000, 999_999_999);
                    assetDescription = assetName + "test";
                    issueTx = buyer.issue(i -> i.name(assetName)
                            .quantity(assetQuantity)
                            .description(assetDescription)
                            .decimals(assetDecimals)
                            .reissuable(true)).tx();
                    assetId = issueTx.assetId();
                },
                () -> {
                    seller = new Account(DEFAULT_FAUCET);
                    sellerPrivateKey = seller.privateKey();
                    sellerPublicKey = seller.publicKey().toString();
                    sellerAddress = seller.address().toString();
                }
        );
    }

    @Test
    @DisplayName("Check subscription on Exchange transaction")
    void subscribeTestForExchangeTransaction() {
        long sumSellerTokens = seller.getWavesBalance() - MIN_FEE_FOR_EXCHANGE;
        long offerForToken = getRandomInt(1000, 50000);
        long amountBefore = seller.getWavesBalance() - ONE_WAVES;

        amount = Amount.of(sumSellerTokens, AssetId.WAVES);
        price = Amount.of(offerForToken, assetId);
        buy = Order.buy(amount, price, buyer.publicKey()).version(ORDER_V_3).getSignedWith(buyerPrivateKey);
        sell = Order.sell(amount, price, buyer.publicKey()).version(ORDER_V_4).getSignedWith(sellerPrivateKey);

        exchangeTransactionSender(buyer, seller, buy, sell, amount.value(), price.value(), 0, LATEST_VERSION);
        height = node().getHeight();
        subscribeResponseHandler(channel, buyer, height, height);

        checkExchangeSubscribe(MIN_FEE_FOR_EXCHANGE, "");
        checkBalancesForExchangeWithWaves(amountBefore);
    }

    @Test
    @DisplayName("Check subscription on Exchange transaction for two smart assets")
    void subscribeTestForExchangeTwoSmartAssetsTransaction() {
        fee = MIN_FEE_FOR_EXCHANGE + EXCHANGE_FEE_FOR_SMART_ASSETS;
        long sumBuyerTokens = getRandomInt(1, 500) * (long) Math.pow(10, 8);
        long wavesBuyerAmountBefore = buyer.getWavesBalance() - ONE_WAVES;
        long wavesSellerAmountBefore = seller.getWavesBalance() - ONE_WAVES;

        AssetId firstSmartAssetId = seller.issue(i -> i.name(assetName).script(SCRIPT_PERMITTING_OPERATIONS)
                .quantity(assetQuantity).decimals(DEFAULT_DECIMALS)).tx().assetId();
        AssetId secondSmartAssetId = buyer.issue(i -> i.name("S_Smart_Asset").script(SCRIPT_PERMITTING_OPERATIONS)
                .quantity(assetQuantity).decimals(DEFAULT_DECIMALS)).tx().assetId();

        amount = Amount.of(MIN_TRANSACTION_SUM, firstSmartAssetId);
        price = Amount.of(sumBuyerTokens, secondSmartAssetId);
        buy = Order.buy(amount, price, buyer.publicKey()).version(ORDER_V_4).getSignedWith(buyerPrivateKey);
        sell = Order.sell(amount, price, buyer.publicKey()).version(ORDER_V_4).getSignedWith(sellerPrivateKey);

        exchangeTransactionSender
                (buyer, seller, buy, sell, amount.value(), price.value(), EXCHANGE_FEE_FOR_SMART_ASSETS, LATEST_VERSION);
        height = node().getHeight();
        subscribeResponseHandler(channel, buyer, height, height);
        checkExchangeSubscribe(fee, amount.assetId().toString());
        checkBalancesForExchangeWithAssets(wavesBuyerAmountBefore, wavesSellerAmountBefore, EXCHANGE_FEE_FOR_SMART_ASSETS);
    }

    @Test
    @DisplayName("Check subscription on Exchange transaction for one smart asset")
    void subscribeTestForExchangeOneSmartAssetTransaction() {
        fee = MIN_FEE_FOR_EXCHANGE + EXTRA_FEE;
        long sumBuyerTokens = getRandomInt(1, 500) * (long) Math.pow(10, 8);
        long wavesBuyerAmountBefore = buyer.getWavesBalance();
        long wavesSellerAmountBefore = seller.getWavesBalance() - ONE_WAVES;

        AssetId smartAssetId = seller.issue(i -> i.name("v_Asset").script(SCRIPT_PERMITTING_OPERATIONS)
                .quantity(assetQuantity).decimals(DEFAULT_DECIMALS)).tx().assetId();

        amount = Amount.of(MIN_TRANSACTION_SUM, smartAssetId);
        price = Amount.of(sumBuyerTokens, assetId);
        buy = Order.buy(amount, price, buyer.publicKey()).version(ORDER_V_4).getSignedWith(buyerPrivateKey);
        sell = Order.sell(amount, price, buyer.publicKey()).version(ORDER_V_4).getSignedWith(sellerPrivateKey);

        exchangeTransactionSender
                (buyer, seller, buy, sell, amount.value(), price.value(), EXTRA_FEE, LATEST_VERSION);
        height = node().getHeight();
        subscribeResponseHandler(channel, buyer, height, height);
        checkExchangeSubscribe(fee, amount.assetId().toString());
        checkBalancesForExchangeWithAssets(wavesBuyerAmountBefore, wavesSellerAmountBefore, EXTRA_FEE);
    }

    private void checkExchangeSubscribe(long fee, String amountAssetId) {
        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(buyerPublicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(fee),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(LATEST_VERSION),
                () -> assertThat(getTransactionId()).isEqualTo(getExchangeTx().id().toString()),
                () -> assertThat(getAmountFromExchange(0)).isEqualTo(amount.value()),
                () -> assertThat(getPriceFromExchange(0)).isEqualTo(price.value()),
                () -> assertThat(getBuyMatcherFeeFromExchange(0)).isEqualTo(MIN_FEE_FOR_EXCHANGE),
                () -> assertThat(getSellMatcherFeeFromExchange(0)).isEqualTo(MIN_FEE_FOR_EXCHANGE),
                // buy order
                () -> assertThat(getSenderPublicKeyFromExchange(0, 0)).isEqualTo(buyerPublicKey),
                () -> assertThat(getMatcherPublicKeyFromExchange(0, 0)).isEqualTo(buyerPublicKey),
                () -> assertThat(getAmountAssetIdFromExchange(0, 0)).isEqualTo(amountAssetId),
                () -> assertThat(getOrderAmountFromExchange(0, 0)).isEqualTo(amount.value()),
                () -> assertThat(getOrderPriceFromExchange(0, 0)).isEqualTo(price.value()),
                () -> assertThat(getMatcherFeeFromExchange(0, 0)).isEqualTo(MIN_FEE_FOR_EXCHANGE),
                () -> assertThat(getOrderVersionFromExchange(0, 0)).isEqualTo(buy.version()),
                // sell order
                () -> assertThat(getSenderPublicKeyFromExchange(0, 1)).isEqualTo(sellerPublicKey),
                () -> assertThat(getMatcherPublicKeyFromExchange(0, 1)).isEqualTo(buyerPublicKey),
                () -> assertThat(getAmountAssetIdFromExchange(0, 1)).isEqualTo(amountAssetId),
                () -> assertThat(getOrderAmountFromExchange(0, 1)).isEqualTo(amount.value()),
                () -> assertThat(getOrderPriceFromExchange(0, 1)).isEqualTo(price.value()),
                () -> assertThat(getMatcherFeeFromExchange(0, 1)).isEqualTo(MIN_FEE_FOR_EXCHANGE),
                () -> assertThat(getOrderSideFromExchange(0, 1)).isEqualTo("SELL"),
                () -> assertThat(getOrderVersionFromExchange(0, 1)).isEqualTo(sell.version())
        );
    }

    private void checkBalancesForExchangeWithWaves(long amountBefore) {
        // check waves balance from balances buyer
        assertThat(getAddress(0, 0)).isEqualTo(buyerAddress);
        assertThat(getAmountBefore(0, 0)).isEqualTo(amountBefore);
        assertThat(getAmountAfter(0, 0)).isEqualTo(getBuyerBalanceAfterTransactionAmountAsset());
        // check asset balance from balances buyer
        assertThat(getAddress(0, 1)).isEqualTo(buyerAddress);
        assertThat(getAmountBefore(0, 1)).isEqualTo(assetQuantity);
        assertThat(getAmountAfter(0, 1)).isEqualTo(getBuyerBalanceAfterTransactionPriceAsset());
        assertThat(getAssetIdAmountAfter(0, 1)).isEqualTo(assetId.toString());
        // check waves balance from balances seller
        assertThat(getAddress(0, 2)).isEqualTo(sellerAddress);
        assertThat(getAmountBefore(0, 2)).isEqualTo(DEFAULT_FAUCET);
        assertThat(getAmountAfter(0, 2)).isEqualTo(getSellerBalanceAfterTransactionAmountAsset());
        // check asset balance from balances seller
        assertThat(getAddress(0, 3)).isEqualTo(sellerAddress);
        assertThat(getAmountBefore(0, 3)).isEqualTo(0);
        assertThat(getAmountAfter(0, 3)).isEqualTo(getSellerBalanceAfterTransactionPriceAsset());
        assertThat(getAssetIdAmountAfter(0, 3)).isEqualTo(assetId.toString());
    }

    private void checkBalancesForExchangeWithAssets(long wavesBuyerAmountBefore, long wavesSellerAmountBefore, long tokenTypeFee) {
        // check waves balance from balances buyer
        assertThat(getAddress(0, 0)).isEqualTo(buyerAddress);
        assertThat(getAmountBefore(0, 0)).isEqualTo(wavesBuyerAmountBefore);
        assertThat(getAmountAfter(0, 0)).isEqualTo(wavesBuyerAmountBefore - tokenTypeFee);
        // check asset balance from balances buyer
        assertThat(getAddress(0, 1)).isEqualTo(buyerAddress);
        assertThat(getAmountBefore(0, 1)).isEqualTo(assetQuantity);
        assertThat(getAmountAfter(0, 1)).isEqualTo(getBuyerBalanceAfterTransactionPriceAsset());
        assertThat(getAssetIdAmountAfter(0, 1)).isEqualTo(getPriceAssetId().toString());
        // check asset balance from balances
        assertThat(getAddress(0, 2)).isEqualTo(buyerAddress);
        assertThat(getAmountBefore(0, 2)).isEqualTo(0);
        assertThat(getAmountAfter(0, 2)).isEqualTo(getBuyerBalanceAfterTransactionAmountAsset());
        assertThat(getAssetIdAmountAfter(0, 2)).isEqualTo(getAmountAssetId().toString());
        // check asset balance from balances seller
        assertThat(getAddress(0, 3)).isEqualTo(sellerAddress);
        assertThat(getAmountBefore(0, 3)).isEqualTo(wavesSellerAmountBefore);
        assertThat(getAmountAfter(0, 3)).isEqualTo(wavesSellerAmountBefore - MIN_FEE_FOR_EXCHANGE);
        // check asset balance from balances buyer
        assertThat(getAddress(0, 4)).isEqualTo(sellerAddress);
        assertThat(getAmountBefore(0, 4)).isEqualTo(0);
        assertThat(getAmountAfter(0, 4)).isEqualTo(getSellerBalanceAfterTransactionPriceAsset());
        assertThat(getAssetIdAmountAfter(0, 4)).isEqualTo(getPriceAssetId().toString());
        // check asset balance from balances
        assertThat(getAddress(0, 5)).isEqualTo(sellerAddress);
        assertThat(getAmountBefore(0, 5)).isEqualTo(assetQuantity);
        assertThat(getAmountAfter(0, 5)).isEqualTo(getSellerBalanceAfterTransactionAmountAsset());
        assertThat(getAssetIdAmountAfter(0, 5)).isEqualTo(getAmountAssetId().toString());
    }
}
