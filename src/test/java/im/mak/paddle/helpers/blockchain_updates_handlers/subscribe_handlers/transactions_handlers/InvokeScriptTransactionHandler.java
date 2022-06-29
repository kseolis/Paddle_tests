package im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers;

import com.wavesplatform.crypto.base.Base58;

import static im.mak.paddle.helpers.blockchain_updates_handlers.subscribe_handlers.transactions_handlers.TransactionsHandler.getTransactionAtIndex;

public class InvokeScriptTransactionHandler {
    public static String getInvokeTransactionPublicKeyHash(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getInvokeScript()
                .getDApp()
                .getPublicKeyHash().toByteArray()
        );
    }

    public static String getInvokeTransactionFunctionCall(int txIndex) {
        return Base58.encode(getTransactionAtIndex(txIndex)
                .getInvokeScript()
                .getFunctionCall()
                .toByteArray()
        );
    }

    public static long getInvokeTransactionAmount(int txIndex, int paymentIndex) {
        return getTransactionAtIndex(txIndex)
                .getInvokeScript()
                .getPayments(paymentIndex)
                .getAmount();
    }
}
