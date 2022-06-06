package im.mak.paddle.blockchain_updates.subscribe_tests;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.exchange.Order;
import im.mak.paddle.Account;
import im.mak.paddle.blockchain_updates.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.Constants.ORDER_V_4;

public class ExchangeTransactionSubscriptionTest extends BaseTest {
    private long amount;
    private int assetQuantity;
    private int assetDecimals;
    private String address;
    private String publicKey;
    private Account account;
    private String assetName;
    private String assetDescription;

    @BeforeEach
    void setUp() {
        amount = getRandomInt(100, 10000000);
        assetQuantity = getRandomInt(1000, 999_999_999);
        assetDecimals = getRandomInt(0, 8);
        account = new Account(DEFAULT_FAUCET);
        address = account.address().toString();
        publicKey = account.publicKey().toString();
        assetName = getRandomInt(1, 900000) + "asset";
        assetDescription = assetName + "test";
    }

    @Test
    @DisplayName("Check subscription on Exchange transaction")
    void subscribeTestForExchangeTransaction() {
        Account seller = new Account(DEFAULT_FAUCET);
        final int assetQuantity = getRandomInt(1000, 999_999_999);
        final int assetDecimals = getRandomInt(0, 8);
        IssueTransaction issueTx = account.issue(i -> i
                .name(assetName)
                .quantity(assetQuantity)
                .description(assetDescription)
                .decimals(assetDecimals)
                .reissuable(true)).tx();
        AssetId assetId = issueTx.assetId();

        long sumSellerTokens = seller.getWavesBalance() - MIN_FEE_FOR_EXCHANGE;
        long offerForToken = getRandomInt(1, 50);


        Amount amountsTokensForExchange = Amount.of(sumSellerTokens, AssetId.WAVES);
        Amount pricePerToken = Amount.of(offerForToken, assetId);

        Order buyerOrder = Order.buy(amountsTokensForExchange, pricePerToken, account.publicKey()).version(ORDER_V_3)
                .getSignedWith(account.privateKey());
        Order sellOrder = Order.sell(amountsTokensForExchange, pricePerToken, account.publicKey()).version(ORDER_V_4)
                .getSignedWith(seller.privateKey());

        height = node().getHeight();

    }
}
