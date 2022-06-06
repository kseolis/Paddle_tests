package im.mak.paddle.helpers;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.exchange.Order;
import im.mak.paddle.Account;

import static im.mak.paddle.util.Constants.MIN_FEE_FOR_EXCHANGE;

public class Calculations {
    private static AssetId amountAssetId;
    private static AssetId priceAssetId;
    private static long buyerBalanceAfterTransactionAmountAssetId;
    private static long sellerBalanceAfterTransactionAmountAssetId;
    private static long buyerBalanceAfterTransactionPriceAsset;
    private static long sellerBalanceAfterTransactionPriceAsset;

    public static void calculateBalancesAfterTransaction(Account from, Account to, Order buy, long amount, int decimals) {

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


    public static AssetId getAmountAssetId() {
        return amountAssetId;
    }

    public static AssetId getPriceAssetId() {
        return priceAssetId;
    }

    public static long getBuyerBalanceAfterTransactionAmountAssetId() {
        return buyerBalanceAfterTransactionAmountAssetId;
    }

    public static long getSellerBalanceAfterTransactionAmountAssetId() {
        return sellerBalanceAfterTransactionAmountAssetId;
    }

    public static long getBuyerBalanceAfterTransactionPriceAsset() {
        return buyerBalanceAfterTransactionPriceAsset;
    }

    public static long getSellerBalanceAfterTransactionPriceAsset() {
        return sellerBalanceAfterTransactionPriceAsset;
    }
}
