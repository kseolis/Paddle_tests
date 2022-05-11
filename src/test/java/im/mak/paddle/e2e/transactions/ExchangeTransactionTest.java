package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.ExchangeTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.exchange.Order;

import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ExchangeTransactionTest {
    private static final byte decimals = 8;
    private static final byte exchangeVersion = 3;
    private static final byte orderV3 = 3;
    private static final byte orderV4 = 4;

    private static Account alice;
    private static Account bob;
    private static Account cat;

    private static AssetId testAssetId;
    private static AssetId firstSmartAssetId;
    private static AssetId secondSmartAssetId;

    private AssetId amountAssetId;
    private AssetId priceAssetId;


    private long buyerBalanceAfterTransactionAmountAssetId;
    private long sellerBalanceAfterTransactionAmountAssetId;
    private long buyerBalanceAfterTransactionPriceAsset;
    private long sellerBalanceAfterTransactionPriceAsset;

    @BeforeAll
    static void before() {
        async(
                () -> {
                    alice = new Account(DEFAULT_FAUCET);
                    testAssetId = alice.issue(i -> i.name("Test_Asset").quantity(1000L).decimals(decimals)).tx().assetId();
                },
                () -> {
                    bob = new Account(DEFAULT_FAUCET);
                    secondSmartAssetId = bob.issue(i -> i.name("S_Smart_Asset").script("{-# SCRIPT_TYPE ASSET #-} true")
                            .quantity(4000L).decimals(decimals)).tx().assetId();
                },
                () -> {
                    cat = new Account(DEFAULT_FAUCET);
                    firstSmartAssetId = cat.issue(i -> i.name("F_Smart_Asset").script("{-# SCRIPT_TYPE ASSET #-} true")
                            .quantity(4000L).decimals(decimals)).tx().assetId();
                }
        );
    }

    @Test
    @DisplayName("Exchange maximum tokens for maximum price")
    void exchangeMaxAssets() {
        long sumSellerTokens = bob.getWavesBalance() - MIN_FEE_FOR_EXCHANGE;
        long offerForToken = getRandomInt(1, 50);

        Amount amountsTokensForExchange = Amount.of(sumSellerTokens, AssetId.WAVES);
        Amount pricePerToken = Amount.of(offerForToken, testAssetId);

        Order buyerOrder = Order.buy(amountsTokensForExchange, pricePerToken, alice.publicKey()).version(orderV3)
                .getSignedWith(alice.privateKey());
        Order sellOrder = Order.sell(amountsTokensForExchange, pricePerToken, alice.publicKey()).version(orderV4)
                .getSignedWith(bob.privateKey());

        exchangeTransaction(
                alice,
                bob,
                buyerOrder,
                sellOrder,
                amountsTokensForExchange.value(),
                pricePerToken.value(),
                0
        );
    }

    @Test
    @DisplayName("Exchange minimum tokens, issued asset is smart")
    void exchangeOneSmartAsset() {
        long sumSellerTokens = getRandomInt(1, 50) * (long) Math.pow(10, 8);

        Amount amountsTokensForExchange = Amount.of(50, testAssetId);
        Amount pricePerToken = Amount.of(sumSellerTokens, firstSmartAssetId);

        Order buyerOrder = Order.buy(amountsTokensForExchange, pricePerToken, cat.publicKey()).version(orderV4)
                .getSignedWith(cat.privateKey());
        Order sellOrder = Order.sell(amountsTokensForExchange, pricePerToken, cat.publicKey()).version(orderV3)
                .getSignedWith(alice.privateKey());

        exchangeTransaction(
                cat,
                alice,
                buyerOrder,
                sellOrder,
                amountsTokensForExchange.value(),
                pricePerToken.value(),
                EXTRA_FEE
        );
    }

    @Test
    @DisplayName("Exchange transaction two smart assets")
    void exchangeTwoSmartAssets() {
        long sumBuyerTokens = getRandomInt(1, 50) * (long) Math.pow(10, 8);
        Amount amountsTokensForExchange = Amount.of(MIN_TRANSFER_SUM, firstSmartAssetId);
        Amount pricePerToken = Amount.of(sumBuyerTokens, secondSmartAssetId);

        Order buyerOrder = Order.buy(amountsTokensForExchange, pricePerToken, bob.publicKey()).version(orderV3)
                .getSignedWith(bob.privateKey());
        Order sellOrder = Order.sell(amountsTokensForExchange, pricePerToken, bob.publicKey()).version(orderV3)
                .getSignedWith(cat.privateKey());

        exchangeTransaction(
                bob,
                cat,
                buyerOrder,
                sellOrder,
                amountsTokensForExchange.value(),
                pricePerToken.value(),
                EXTRA_FEE * 2
        );
    }

    private void exchangeTransaction
            (Account from, Account to, Order buy, Order sell, long amount, long price, long extraFee) {
        calculateBalancesAfterTransaction(from, to, buy, amount);
        long fee = MIN_FEE_FOR_EXCHANGE + extraFee;
        ExchangeTransaction tx = ExchangeTransaction
                .builder(buy, sell, amount, price, MIN_FEE_FOR_EXCHANGE, MIN_FEE_FOR_EXCHANGE)
                .extraFee(extraFee)
                .version(exchangeVersion)
                .getSignedWith(from.privateKey());

        node().waitForTransaction(node().broadcast(tx).id());

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.fee().value()).isEqualTo(fee),
                () -> assertThat(tx.fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(tx.assetPair()).isEqualTo(buy.assetPair()),
                () -> assertThat(tx.sender()).isEqualTo(from.publicKey()),
                () -> assertThat(tx.orders()).isEqualTo(List.of(buy, sell)),
                () -> assertThat(tx.amount()).isEqualTo(amount),
                () -> assertThat(tx.price()).isEqualTo(buy.price().value()),
                () -> assertThat(tx.type()).isEqualTo(7),
                () -> assertThat(from.getBalance(amountAssetId)).isEqualTo(buyerBalanceAfterTransactionAmountAssetId),
                () -> assertThat(to.getBalance(amountAssetId)).isEqualTo(sellerBalanceAfterTransactionAmountAssetId),
                () -> assertThat(from.getBalance(priceAssetId)).isEqualTo(buyerBalanceAfterTransactionPriceAsset),
                () -> assertThat(to.getBalance(priceAssetId)).isEqualTo(sellerBalanceAfterTransactionPriceAsset)
        );
    }

    private void calculateBalancesAfterTransaction(Account from, Account to, Order buy, long amount) {
        amountAssetId = buy.assetPair().left();
        priceAssetId = buy.assetPair().right();

        buyerBalanceAfterTransactionAmountAssetId = from.getBalance(amountAssetId) + amount;
        sellerBalanceAfterTransactionAmountAssetId = to.getBalance(amountAssetId) - amount;

        if (amountAssetId.isWaves()) {
            sellerBalanceAfterTransactionAmountAssetId = to.getBalance(amountAssetId) - amount - MIN_FEE_FOR_EXCHANGE;
        }

        if (!amountAssetId.isWaves() || !priceAssetId.isWaves()) {
            long amountOrderNormalize = buy.amount().value();
            long priceOrderNormalize = buy.price().value();
            double spendAmount = amountOrderNormalize * priceOrderNormalize * (Math.pow(10, -decimals));
            buyerBalanceAfterTransactionPriceAsset = from.getBalance(priceAssetId) - (long) spendAmount;
            sellerBalanceAfterTransactionPriceAsset = to.getBalance(priceAssetId) + (long) spendAmount;
        }
    }
}
