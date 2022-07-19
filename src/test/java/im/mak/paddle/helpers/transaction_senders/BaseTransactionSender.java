package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.wavesj.info.TransactionInfo;

public class BaseTransactionSender {
    protected static long balanceAfterTransaction;
    protected static long accountWavesBalance;
    protected static TransactionInfo txInfo;
    protected static int version;
    protected static long fee;
    protected static long extraFee;

    public static long getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public static long getAccountWavesBalance() {
        return accountWavesBalance;
    }

    public static TransactionInfo getTxInfo() {
        return txInfo;
    }


    public static void setVersion(int version) {
        BaseTransactionSender.version = version;
    }

    public static void setFee(long fee) {
        BaseTransactionSender.fee = fee;
    }

    public static void setExtraFee(long extraFee) {
        BaseTransactionSender.extraFee = extraFee;
    }

}
