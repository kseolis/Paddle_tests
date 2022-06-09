package im.mak.paddle.helpers.transaction_senders;

import com.wavesplatform.transactions.DataTransaction;
import com.wavesplatform.transactions.data.DataEntry;
import com.wavesplatform.transactions.data.EntryType;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.MIN_FEE;

public class DataTransactionsSender {
    private static DataTransaction dataTx;
    private static TransactionInfo dataTxInfo;
    private static long balanceAfterTransaction;
    private static final Map<String, EntryType> dataTxEntryMap = new HashMap<>();

    public static void dataEntryTransactionSender(Account account, int version, DataEntry... dataEntries) {
        balanceAfterTransaction = account.getWavesBalance() - MIN_FEE;
        List<DataEntry> dataEntriesAsList = Arrays.asList(dataEntries);

        dataEntriesAsList.forEach(a -> dataTxEntryMap.put(a.key(), a.type()));

        dataTx = DataTransaction.builder(dataEntries)
                .version(version)
                .getSignedWith(account.privateKey());
        node().waitForTransaction(node().broadcast(dataTx).id());
        dataTxInfo = node().getTransactionInfo(dataTx.id());
    }

    public static DataTransaction getDataTx() {
        return dataTx;
    }

    public static TransactionInfo getDataTxInfo() {
        return dataTxInfo;
    }

    public static Map<String, EntryType> getDataTxEntryMap() {
        return dataTxEntryMap;
    }

    public static long getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

}
