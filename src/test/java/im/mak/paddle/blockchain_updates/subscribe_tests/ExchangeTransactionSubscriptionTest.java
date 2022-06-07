package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.ExchangeTransaction;
import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.exchange.Order;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.ExchangeTransaction.LATEST_VERSION;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Calculations.*;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.getTransactionId;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.SubscribeHandler.subscribeResponseHandler;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transaction_state_updates.Balances.*;
import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.ExchangeTransaction.*;
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
    private String assetName;
    private String assetDescription;
    private IssueTransaction issueTx;
    private TransactionInfo txInfo;
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

        Amount amount = Amount.of(sumSellerTokens, AssetId.WAVES);
        Amount price = Amount.of(offerForToken, assetId);

        Order buy = Order.buy(amount, price, buyer.publicKey()).version(ORDER_V_3).getSignedWith(buyerPrivateKey);
        Order sell = Order.sell(amount, price, buyer.publicKey()).version(ORDER_V_4).getSignedWith(sellerPrivateKey);

        exchangeTx(buyer, seller, buy, sell, amount.value(), price.value(), 0);
        height = node().getHeight();
        subscribeResponseHandler(channel, buyer, height, height);

        assertAll(
                () -> assertThat(getChainId(0)).isEqualTo(DEVNET_CHAIN_ID),
                () -> assertThat(getSenderPublicKeyFromTransaction(0)).isEqualTo(buyerPublicKey),
                () -> assertThat(getTransactionFeeAmount(0)).isEqualTo(MIN_FEE_FOR_EXCHANGE),
                () -> assertThat(getTransactionVersion(0)).isEqualTo(ExchangeTransaction.LATEST_VERSION),
                () -> assertThat(getTransactionId()).isEqualTo(txInfo.tx().id().toString()),
                () -> assertThat(getAmountFromExchange(0)).isEqualTo(amount.value()),
                () -> assertThat(getPriceFromExchange(0)).isEqualTo(price.value()),
                () -> assertThat(getBuyMatcherFeeFromExchange(0)).isEqualTo(MIN_FEE_FOR_EXCHANGE),
                () -> assertThat(getSellMatcherFeeFromExchange(0)).isEqualTo(MIN_FEE_FOR_EXCHANGE),
                // buy order
                () -> assertThat(getSenderPublicKeyFromExchange(0, 0)).isEqualTo(buyerPublicKey),
                () -> assertThat(getMatcherPublicKeyFromExchange(0, 0)).isEqualTo(buyerPublicKey),
                () -> assertThat(getPriceAssetIdFromExchange(0, 0)).isEqualTo(""),
                () -> assertThat(getOrderAmountFromExchange(0, 0)).isEqualTo(amount.value()),
                () -> assertThat(getOrderPriceFromExchange(0, 0)).isEqualTo(price.value()),
                () -> assertThat(getMatcherFeeFromExchange(0, 0)).isEqualTo(MIN_FEE_FOR_EXCHANGE),
                () -> assertThat(getOrderVersionFromExchange(0, 0)).isEqualTo(buy.version()),
                // sell order
                () -> assertThat(getSenderPublicKeyFromExchange(0, 1)).isEqualTo(sellerPublicKey),
                () -> assertThat(getMatcherPublicKeyFromExchange(0, 1)).isEqualTo(buyerPublicKey),
                () -> assertThat(getPriceAssetIdFromExchange(0, 1)).isEqualTo(""),
                () -> assertThat(getOrderAmountFromExchange(0, 1)).isEqualTo(amount.value()),
                () -> assertThat(getOrderPriceFromExchange(0, 1)).isEqualTo(price.value()),
                () -> assertThat(getMatcherFeeFromExchange(0, 1)).isEqualTo(MIN_FEE_FOR_EXCHANGE),
                () -> assertThat(getOrderSideFromExchange(0, 1)).isEqualTo("SELL"),
                () -> assertThat(getOrderVersionFromExchange(0, 1)).isEqualTo(sell.version()),
                // check waves balance from balances buyer
                () -> assertThat(getAddress(0, 0)).isEqualTo(buyerAddress),
                () -> assertThat(getAmountBefore(0, 0)).isEqualTo(DEFAULT_FAUCET - ONE_WAVES),
                () -> assertThat(getAmountAfter(0, 0)).isEqualTo(getBuyerBalanceAfterTransactionAmountAssetId()),
                // check asset balance from balances buyer
                () -> assertThat(getAddress(0, 1)).isEqualTo(buyerAddress),
                () -> assertThat(getAmountBefore(0, 1)).isEqualTo(assetQuantity),
                () -> assertThat(getAmountAfter(0, 1)).isEqualTo(getBuyerBalanceAfterTransactionPriceAsset()),
                () -> assertThat(getIssuedAssetIdAmountAfter(0, 1)).isEqualTo(assetId.toString()),
                // check waves balance from balances seller
                () -> assertThat(getAddress(0, 2)).isEqualTo(sellerAddress),
                () -> assertThat(getAmountBefore(0, 2)).isEqualTo(DEFAULT_FAUCET),
                () -> assertThat(getAmountAfter(0, 2)).isEqualTo(getSellerBalanceAfterTransactionAmountAssetId()),
                // check asset balance from balances seller
                () -> assertThat(getAddress(0, 3)).isEqualTo(sellerAddress),
                () -> assertThat(getAmountBefore(0, 3)).isEqualTo(0),
                () -> assertThat(getAmountAfter(0, 3)).isEqualTo(getSellerBalanceAfterTransactionPriceAsset()),
                () -> assertThat(getIssuedAssetIdAmountAfter(0, 3)).isEqualTo(assetId.toString())
        );
    }

    private void exchangeTx(Account from, Account to, Order buy, Order sell, long amount, long price, long extraFee) {
        calculateBalancesAfterTransaction(from, to, buy, amount, assetDecimals);
        ExchangeTransaction tx = ExchangeTransaction
                .builder(buy, sell, amount, price, MIN_FEE_FOR_EXCHANGE, MIN_FEE_FOR_EXCHANGE)
                .extraFee(extraFee)
                .version(LATEST_VERSION)
                .getSignedWith(from.privateKey());
        node().waitForTransaction(node().broadcast(tx).id());
        txInfo = node().getTransactionInfo(tx.id());
    }
}
