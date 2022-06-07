package im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions;

import com.wavesplatform.crypto.base.Base58;
import com.wavesplatform.protobuf.order.OrderOuterClass;
import com.wavesplatform.protobuf.transaction.TransactionOuterClass;

import static im.mak.paddle.helpers.blockchain_updates_handler.subscribe.transactions.Transactions.getTransactionAtIndex;

public class ExchangeTransaction {
    public static TransactionOuterClass.ExchangeTransactionData getExchangeTransaction(int index) {
        return getTransactionAtIndex(index).getExchange();
    }

    public static long getAmountFromExchange(int index) {
        return getExchangeTransaction(index).getAmount();
    }

    public static long getPriceFromExchange(int index) {
        return getExchangeTransaction(index).getPrice();
    }

    public static long getBuyMatcherFeeFromExchange(int index) {
        return getExchangeTransaction(index).getBuyMatcherFee();
    }

    public static long getSellMatcherFeeFromExchange(int index) {
        return getExchangeTransaction(index).getSellMatcherFee();
    }

    public static OrderOuterClass.Order getOrdersFromExchange(int index, int orderIndex) {
        return getExchangeTransaction(index).getOrders(orderIndex);
    }

    public static String getSenderPublicKeyFromExchange(int index, int orderIndex) {
        return Base58.encode(getOrdersFromExchange(index, orderIndex)
                .getSenderPublicKey()
                .toByteArray());
    }

    public static String getMatcherPublicKeyFromExchange(int index, int orderIndex) {
        return Base58.encode(getOrdersFromExchange(index, orderIndex)
                .getMatcherPublicKey()
                .toByteArray());
    }

    public static String getPriceAssetIdFromExchange(int index, int orderIndex) {
        return Base58.encode(getOrdersFromExchange(index, orderIndex)
                .getAssetPair()
                .getAmountAssetId()
                .toByteArray());
    }

    public static long getOrderAmountFromExchange(int index, int orderIndex) {
        return getOrdersFromExchange(index, orderIndex).getAmount();
    }

    public static long getOrderPriceFromExchange(int index, int orderIndex) {
        return getOrdersFromExchange(index, orderIndex).getPrice();
    }

    public static long getMatcherFeeFromExchange(int index, int orderIndex) {
        return getOrdersFromExchange(index, orderIndex).getMatcherFee().getAmount();
    }

    public static long getOrderVersionFromExchange(int index, int orderIndex) {
        return getOrdersFromExchange(index, orderIndex).getVersion();
    }

    public static String getOrderSideFromExchange(int index, int orderIndex) {
        return getOrdersFromExchange(index, orderIndex).getOrderSide().toString();
    }
}
