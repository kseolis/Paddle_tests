package im.mak.paddle.helpers;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.exchange.Order;
import im.mak.paddle.Account;

import static com.wavesplatform.transactions.common.AssetId.WAVES;
import static im.mak.paddle.util.Constants.*;

public class Calculations {
    private static AssetId amountAssetId;
    private static AssetId priceAssetId;
    private static long buyerBalanceAfterTransactionAmountAsset;
    private static long sellerBalanceAfterTransactionAmountAsset;
    private static long buyerBalanceAfterTransactionPriceAsset;
    private static long sellerBalanceAfterTransactionPriceAsset;

    private static long transactionCommission;

    public static void calculateBalancesAfterTransaction(Account from, Account to, Order buy, long amount, int decimals) {
        amountAssetId = buy.assetPair().left();
        priceAssetId = buy.assetPair().right();

        buyerBalanceAfterTransactionAmountAsset = from.getBalance(amountAssetId) + amount;
        sellerBalanceAfterTransactionAmountAsset = to.getBalance(amountAssetId) - amount;

        if (amountAssetId.isWaves()) {
            sellerBalanceAfterTransactionAmountAsset = to.getBalance(amountAssetId) - amount - MIN_FEE_FOR_EXCHANGE;
        }

        if (!amountAssetId.isWaves() || !priceAssetId.isWaves()) {
            long amountOrderNormalize = buy.amount().value();
            long priceOrderNormalize = buy.price().value();
            double spendAmount = amountOrderNormalize * priceOrderNormalize * (Math.pow(10, -decimals));
            buyerBalanceAfterTransactionPriceAsset = from.getBalance(priceAssetId) - (long) spendAmount;
            sellerBalanceAfterTransactionPriceAsset = to.getBalance(priceAssetId) + (long) spendAmount;
        }
    }

    public static long calculateSenderBalanceAfterMassTransfer
            (Account account, AssetId assetId, long amount, int numberOfAccounts) {
        long senderBalance = account.getBalance(assetId);
        long numForRoundCheck = 100000;
        long additionalFeeForMassTransfer = FEE_FOR_MASS_TRANSFER * numberOfAccounts;
        if (additionalFeeForMassTransfer % numForRoundCheck != 0) { // The fee value is rounded up to three decimals.
            additionalFeeForMassTransfer = (long) Math.ceil(
                    (float) additionalFeeForMassTransfer / numForRoundCheck
            ) * numForRoundCheck;
        }

        long transactionSum = amount * numberOfAccounts;
        transactionCommission = MIN_FEE + additionalFeeForMassTransfer;

        if (assetId.equals(WAVES)) {
            return senderBalance - transactionSum - transactionCommission;
        }
        return senderBalance - transactionSum;
    }


    public static AssetId getAmountAssetId() {
        return amountAssetId;
    }

    public static AssetId getPriceAssetId() {
        return priceAssetId;
    }

    public static long getBuyerBalanceAfterTransactionAmountAsset() {
        return buyerBalanceAfterTransactionAmountAsset;
    }

    public static long getSellerBalanceAfterTransactionAmountAsset() {
        return sellerBalanceAfterTransactionAmountAsset;
    }

    public static long getBuyerBalanceAfterTransactionPriceAsset() {
        return buyerBalanceAfterTransactionPriceAsset;
    }

    public static long getSellerBalanceAfterTransactionPriceAsset() {
        return sellerBalanceAfterTransactionPriceAsset;
    }

    public static long getTransactionCommission() {
        return transactionCommission;
    }
}
