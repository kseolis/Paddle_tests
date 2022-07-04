package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.exchange.Order;

import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.wavesplatform.transactions.ExchangeTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Calculations.*;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.ExchangeTransactionSender.exchangeTransactionSender;
import static im.mak.paddle.helpers.transaction_senders.ExchangeTransactionSender.getExchangeTx;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ExchangeTransactionTest {
    private static Account alice;
    private static Account bob;
    private static Account cat;

    private static AssetId testAssetId;
    private static AssetId firstSmartAssetId;
    private static AssetId secondSmartAssetId;
    private long fee;

    @BeforeAll
    static void before() {
        async(
                () -> {
                    alice = new Account(DEFAULT_FAUCET);
                    testAssetId = alice.issue(i -> i.name("Test_Asset")
                            .quantity(4000L).decimals(DEFAULT_DECIMALS)).tx().assetId();
                },
                () -> {
                    cat = new Account(DEFAULT_FAUCET);
                    firstSmartAssetId = cat.issue(i -> i.name("F_Smart_Asset").script(SCRIPT_PERMITTING_OPERATIONS)
                            .quantity(4000L).decimals(DEFAULT_DECIMALS)).tx().assetId();
                },
                () -> {
                    bob = new Account(DEFAULT_FAUCET);
                    secondSmartAssetId = bob.issue(i -> i.name("S_Smart_Asset").script(SCRIPT_PERMITTING_OPERATIONS)
                            .quantity(4000L).decimals(DEFAULT_DECIMALS)).tx().assetId();
                }
        );
    }

    @Test
    @DisplayName("Exchange maximum tokens for maximum price")
    void exchangeMaxAssets() {
        fee = MIN_FEE_FOR_EXCHANGE;
        long sumSellerTokens = bob.getWavesBalance() - MIN_FEE_FOR_EXCHANGE;
        long offerForToken = getRandomInt(1, 50);

        for (int v = 1; v <= LATEST_VERSION; v++) {
            Amount amountsTokensForExchange = Amount.of(sumSellerTokens, AssetId.WAVES);
            long amountValue = amountsTokensForExchange.value();
            Amount pricePerToken = Amount.of(offerForToken, testAssetId);
            long priceValue = pricePerToken.value();

            Order buyerOrder = Order.buy(amountsTokensForExchange, pricePerToken, alice.publicKey()).version(v)
                    .getSignedWith(alice.privateKey());
            Order sellOrder = Order.sell(amountsTokensForExchange, pricePerToken, alice.publicKey()).version(v)
                    .getSignedWith(bob.privateKey());

            exchangeTransactionSender(alice, bob, buyerOrder, sellOrder, amountValue, priceValue, 0, v);
            checkAssertsForExchangeTransaction(alice, bob, buyerOrder, sellOrder, amountValue);
            node().faucet().transfer(bob, DEFAULT_FAUCET, AssetId.WAVES);
        }
    }

    @Test
    @DisplayName("Exchange minimum tokens, issued asset is smart")
    void exchangeOneSmartAsset() {
        fee = MIN_FEE_FOR_EXCHANGE + EXTRA_FEE;
        long sumSellerTokens = getRandomInt(1, 50) * (long) Math.pow(10, 8);

        Amount amountsTokensForExchange = Amount.of(50, testAssetId);
        long amountValue = amountsTokensForExchange.value();
        Amount pricePerToken = Amount.of(sumSellerTokens, firstSmartAssetId);
        long priceValue = pricePerToken.value();

        Order buyerOrder = Order.buy(amountsTokensForExchange, pricePerToken, cat.publicKey()).version(ORDER_V_4)
                .getSignedWith(cat.privateKey());
        Order sellOrder = Order.sell(amountsTokensForExchange, pricePerToken, cat.publicKey()).version(ORDER_V_3)
                .getSignedWith(alice.privateKey());

        exchangeTransactionSender(cat, alice, buyerOrder, sellOrder, amountValue, priceValue, EXTRA_FEE, LATEST_VERSION);
        checkAssertsForExchangeTransaction(cat, alice, buyerOrder, sellOrder, amountValue);
    }

    @Test
    @DisplayName("Exchange transaction two smart assets")
    void exchangeTwoSmartAssets() {
        fee = MIN_FEE_FOR_EXCHANGE + EXCHANGE_FEE_FOR_SMART_ASSETS;

        long sumBuyerTokens = getRandomInt(1, 50) * (long) Math.pow(10, 8);

        Amount amountsTokensForExchange = Amount.of(MIN_TRANSACTION_SUM, firstSmartAssetId);
        long amountValue = amountsTokensForExchange.value();

        Amount pricePerToken = Amount.of(sumBuyerTokens, secondSmartAssetId);
        long priceValue = pricePerToken.value();

        Order buyerOrder = Order.buy(amountsTokensForExchange, pricePerToken, bob.publicKey()).version(ORDER_V_3)
                .getSignedWith(bob.privateKey());
        Order sellOrder = Order.sell(amountsTokensForExchange, pricePerToken, bob.publicKey()).version(ORDER_V_4)
                .getSignedWith(cat.privateKey());
        exchangeTransactionSender
                (bob, cat, buyerOrder, sellOrder, amountValue, priceValue, EXCHANGE_FEE_FOR_SMART_ASSETS, LATEST_VERSION);
        checkAssertsForExchangeTransaction(bob, cat, buyerOrder, sellOrder, amountValue);
    }

    private void checkAssertsForExchangeTransaction(Account from, Account to, Order buy, Order sell, long amount) {
        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(getExchangeTx().fee().value()).isEqualTo(fee),
                () -> assertThat(getExchangeTx().fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(getExchangeTx().assetPair()).isEqualTo(buy.assetPair()),
                () -> assertThat(getExchangeTx().sender()).isEqualTo(from.publicKey()),
                () -> assertThat(getExchangeTx().orders()).isEqualTo(List.of(buy, sell)),
                () -> assertThat(getExchangeTx().amount()).isEqualTo(amount),
                () -> assertThat(getExchangeTx().price()).isEqualTo(buy.price().value()),
                () -> assertThat(getExchangeTx().type()).isEqualTo(7),
                () -> assertThat(from.getBalance(getAmountAssetId())).isEqualTo(getBuyerBalanceAfterTransactionAmountAsset()),
                () -> assertThat(to.getBalance(getAmountAssetId())).isEqualTo(getSellerBalanceAfterTransactionAmountAsset()),
                () -> assertThat(from.getBalance(getPriceAssetId())).isEqualTo(getBuyerBalanceAfterTransactionPriceAsset()),
                () -> assertThat(to.getBalance(getPriceAssetId())).isEqualTo(getSellerBalanceAfterTransactionPriceAsset())
        );
    }
}
