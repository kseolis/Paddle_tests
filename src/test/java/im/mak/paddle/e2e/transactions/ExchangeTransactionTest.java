package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.ExchangeTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.exchange.Order;

import com.wavesplatform.transactions.exchange.OrderType;
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
    private static long aliceWavesBalance;
    private static AssetId aliceIssuedAsset;

    private static Account bob;
    private static long bobWavesBalance;

    private static Order buy;
    private static Order sell;

    private Amount amount;
    private Amount price;

    @BeforeAll
    static void before() {
        async(
            () -> {
                alice = new Account(DEFAULT_FAUCET);
                aliceIssuedAsset = alice.issue(i -> i.name("Test_Asset").quantity(1000L).decimals(8)).tx().assetId();
                System.out.println("Alice balance " + alice.getWavesBalance());
            },
            () -> {
                bob = new Account(DEFAULT_FAUCET);
            }
        );
    }

    @Test
    @DisplayName("Minimum ")
    void exchangeMinimumAssets() {
        Amount amount = Amount.of(MIN_TRANSFER_SUM);
        Amount price = Amount.of(MIN_TRANSFER_SUM, AssetId.WAVES);
        buy = Order.builder(OrderType.BUY, amount, price, alice.publicKey()).getSignedWith(alice.privateKey());
        sell = Order.builder(OrderType.SELL, amount, price, alice.publicKey()).getSignedWith(bob.privateKey());
        System.out.println("Alice balance " + alice.getWavesBalance());
        exchangeTransaction(buy, sell, amount.value(), price.value(), MIN_FEE_FOR_EXCHANGE);
    }

    @Test
    @DisplayName("Maximum ")
    void exchangeMaximumAssets() {
        long buyPrice = alice.getBalance(aliceIssuedAsset) * 100_000_000L;

        amount = Amount.of(bob.getWavesBalance() - MIN_FEE_FOR_EXCHANGE);
        System.out.println(amount);
        price = Amount.of(buyPrice, aliceIssuedAsset);
        buy = Order.builder(OrderType.BUY, amount, price, alice.publicKey()).getSignedWith(alice.privateKey());
        sell = Order.builder(OrderType.SELL, amount, price, alice.publicKey()).getSignedWith(bob.privateKey());

        exchangeTransaction(buy, sell, 1, price.value(), MIN_FEE_FOR_EXCHANGE);
    }

    private void exchangeTransaction(Order buy, Order sell, long amount, long price, long fee) {
        aliceWavesBalance = alice.getWavesBalance();
        bobWavesBalance = bob.getWavesBalance();
        long balanceAfterTransaction = aliceWavesBalance + MIN_TRANSFER_SUM;

        System.out.println("Alice balance " + alice.getWavesBalance());
        System.out.println("Bob balance " + bobWavesBalance);
        System.out.println("Alice asset balance " + alice.getBalance(aliceIssuedAsset));
        System.out.println("Bob asset balance " + bob.getBalance(aliceIssuedAsset));

        ExchangeTransaction tx = alice.exchange(buy, sell, amount, price).tx();

        System.out.println("Alice balance " + alice.getWavesBalance());
        System.out.println("Bob balance " + bob.getWavesBalance());
        System.out.println("Alice asset balance " + alice.getBalance(aliceIssuedAsset));
        System.out.println("Bob asset balance " + bob.getBalance(aliceIssuedAsset));

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        System.out.println(txInfo);

        assertAll(
                () -> assertThat(alice.getWavesBalance()).isEqualTo(balanceAfterTransaction),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(fee)
        );
    }
}
