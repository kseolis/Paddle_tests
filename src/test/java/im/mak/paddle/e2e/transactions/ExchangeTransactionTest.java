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

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
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

    private static Order buyerOrder;
    private static Order sellerOrder;

    @BeforeAll
    static void before() {
        async(
            () -> {
                alice = new Account(DEFAULT_FAUCET);
                testAssetId = alice.issue(i -> i.name("Test_Asset").quantity(1000L).decimals(8)).tx().assetId();
            },
            () -> {
                bob = new Account(DEFAULT_FAUCET);
                secondSmartAssetId = bob.issue(i -> i.name("S_Smart_Asset").script("{-# SCRIPT_TYPE ASSET #-} false")
                        .quantity(4000L).decimals(8)).tx().assetId();
            },
            () -> {
                cat = new Account(DEFAULT_FAUCET);
                firstSmartAssetId = cat.issue(i -> i.name("F_Smart_Asset").script("{-# SCRIPT_TYPE ASSET #-} true")
                        .quantity(4000L).decimals(8)).tx().assetId();
            }
        );
    }

    @Test
    @DisplayName("Buying maximum tokens for maximum price")
    void exchangeMaxAssets() {
        long sumSellerTokens = bob.getWavesBalance() - MIN_FEE_FOR_EXCHANGE;
        long offerForToken = 100;

        Amount amountsTokensForExchange = Amount.of(sumSellerTokens, AssetId.WAVES);
        Amount pricePerToken = Amount.of(offerForToken, testAssetId);

        buyerOrder = Order.buy(amountsTokensForExchange, pricePerToken, alice.publicKey()).getSignedWith(alice.privateKey());
        sellerOrder = Order.sell(amountsTokensForExchange, pricePerToken, alice.publicKey()).getSignedWith(bob.privateKey());

        exchangeTransaction(
            alice,
            buyerOrder,
            sellerOrder,
            amountsTokensForExchange.value(),
            pricePerToken.value(),
            MIN_FEE_FOR_EXCHANGE
        );
    }

    @Test
    @DisplayName("Buying minimum tokens, issued asset is smart")
    void exchangeTwoSmartAssets() {
        long sumSellerTokens = cat.getBalance(firstSmartAssetId) * 100_000 / 4;
        Amount amountsTokensForExchange = Amount.of(MIN_TRANSFER_SUM, testAssetId);
        Amount pricePerToken = Amount.of(sumSellerTokens, firstSmartAssetId);
        buyerOrder = Order.buy(amountsTokensForExchange, pricePerToken, cat.publicKey()).getSignedWith(cat.privateKey());
        sellerOrder = Order.sell(amountsTokensForExchange, pricePerToken, cat.publicKey()).getSignedWith(alice.privateKey());

        exchangeTransaction(
                cat,
                buyerOrder,
                sellerOrder,
                amountsTokensForExchange.value(),
                pricePerToken.value(),
                FEE_FOR_EXCHANGE
        );
    }

    private void exchangeTransaction
            (Account from, Order buyerOrder, Order sellerOrder, long amount, long price, long fee) {

        ExchangeTransaction tx = from.exchange(buyerOrder, sellerOrder, amount, price).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.type()).isEqualTo(7),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(fee)
        );
    }
}
