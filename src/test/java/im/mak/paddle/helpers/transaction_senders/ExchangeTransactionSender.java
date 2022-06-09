package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.ExchangeTransaction;
import com.wavesplatform.transactions.exchange.Order;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Calculations.calculateBalancesAfterTransaction;
import static im.mak.paddle.util.Constants.DEFAULT_DECIMALS;
import static im.mak.paddle.util.Constants.MIN_FEE_FOR_EXCHANGE;

public class ExchangeTransactionSender extends BaseTransactionSender {
    private static ExchangeTransaction exchangeTx;

    public static void exchangeTransactionSender
            (Account from, Account to, Order buy, Order sell, long amount, long price, long extraFee, int version) {
        calculateBalancesAfterTransaction(from, to, buy, amount, DEFAULT_DECIMALS);
        exchangeTx = ExchangeTransaction
                .builder(buy, sell, amount, price, MIN_FEE_FOR_EXCHANGE, MIN_FEE_FOR_EXCHANGE)
                .extraFee(extraFee)
                .version(version)
                .getSignedWith(from.privateKey());

        node().waitForTransaction(node().broadcast(exchangeTx).id());

        txInfo = node().getTransactionInfo(exchangeTx.id());
    }

    public static ExchangeTransaction getExchangeTx() {
        return exchangeTx;
    }

}
