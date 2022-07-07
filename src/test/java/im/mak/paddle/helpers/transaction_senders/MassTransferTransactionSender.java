package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.MassTransferTransaction;
import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;
import com.wavesplatform.transactions.mass.Transfer;
import im.mak.paddle.Account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Calculations.*;

public class MassTransferTransactionSender extends BaseTransactionSender {
    private static final Map<Address, Long> balancesAfterTransaction = new HashMap<>();
    private static long senderBalanceAfterMassTransfer;
    private static long senderBalanceBeforeMassTransfer;
    private static int accountsSize;
    private static final Base58String attach = new Base58String("attachment");
    private static MassTransferTransaction massTransferTx;

    public static void massTransferTransactionSender
            (Account sender, AssetId id, long amount, List<Account> accounts, int version) {
        List<Transfer> transfers = new ArrayList<>();
        accounts.forEach(a -> transfers.add(Transfer.to(a.address(), amount)));
        accounts.forEach(a -> balancesAfterTransaction.put(a.address(), a.getBalance(id) + amount));

        accountsSize = accounts.size();
        senderBalanceBeforeMassTransfer = sender.getBalance(id);
        senderBalanceAfterMassTransfer = calculateSenderBalanceAfterMassTransfer(sender, id, amount, accountsSize);

        massTransferTx = MassTransferTransaction
                .builder(transfers)
                .assetId(id)
                .fee(getTransactionCommission())
                .attachment(attach)
                .version(version)
                .getSignedWith(sender.privateKey());

        node().waitForTransaction(node().broadcast(massTransferTx).id());
        balanceAfterTransaction = sender.getWavesBalance();
        txInfo = node().getTransactionInfo(massTransferTx.id());
    }

    public static Map<Address, Long> getBalancesAfterTransaction() {
        return balancesAfterTransaction;
    }

    public static long getSenderBalanceBeforeMassTransfer() {
        return senderBalanceBeforeMassTransfer;
    }

    public static long getSenderBalanceAfterMassTransfer() {
        return senderBalanceAfterMassTransfer;
    }

    public static int getAccountsSize() {
        return accountsSize;
    }

    public static MassTransferTransaction getMassTransferTx() {
        return massTransferTx;
    }

    public static Base58String getAttach() {
        return attach;
    }
}
