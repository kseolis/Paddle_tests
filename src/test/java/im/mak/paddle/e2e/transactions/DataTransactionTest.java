package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.DataTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import com.wavesplatform.transactions.data.*;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wavesplatform.transactions.DataTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class DataTransactionTest {

    private static Account account;

    private final Base64String base64String = new Base64String(randomNumAndLetterString(6));
    private final BinaryEntry binaryEntry = BinaryEntry.as("BinEntry", base64String);
    private final BooleanEntry booleanEntry = BooleanEntry.as("Boolean", true);
    private final IntegerEntry integerEntry = IntegerEntry.as("Integer", 210);
    private final StringEntry stringEntry = StringEntry.as("String", "string");

    @BeforeAll
    static void before() {
        account = new Account(DEFAULT_FAUCET);
    }

    @Test
    @DisplayName("transaction of all data types on dataTransaction")
    void allTypesDataTransactionTest() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            dataEntryTransaction(v, binaryEntry, booleanEntry, integerEntry, stringEntry);
        }
    }

    @Test
    @DisplayName("transaction integer dataTransaction")
    void intTypeDataTransactionTest() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            dataEntryTransaction(v, integerEntry);
        }
    }

    @Test
    @DisplayName("transaction string dataTransaction")
    void stringTypeDataTransactionTest() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            dataEntryTransaction(v, stringEntry);
        }
    }

    @Test
    @DisplayName("transaction binary dataTransaction")
    void binaryTypeDataTransactionTest() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            dataEntryTransaction(v, binaryEntry);
        }
    }

    @Test
    @DisplayName("transaction boolean dataTransaction")
    void booleanTypeDataTransactionTest() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            dataEntryTransaction(v, booleanEntry);
        }
    }

    private void dataEntryTransaction(int version, DataEntry... dataEntries) {
        long balanceAfterTransaction = account.getWavesBalance() - MIN_FEE;
        List<DataEntry> dataEntriesAsList = Arrays.asList(dataEntries);
        Map<String, EntryType> entryMap = new HashMap<>();

        dataEntriesAsList.forEach(a -> entryMap.put(a.key(), a.type()));

        DataTransaction tx = DataTransaction
                .builder(dataEntries)
                .version(version)
                .getSignedWith(account.privateKey());
        node().waitForTransaction(node().broadcast(tx).id());

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getWavesBalance()).isEqualTo(balanceAfterTransaction),
                () -> assertThat(tx.fee().value()).isEqualTo(MIN_FEE),
                () -> assertThat(tx.fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(tx.sender()).isEqualTo(account.publicKey()),
                () -> assertThat(tx.type()).isEqualTo(12),
                () -> tx.data().forEach(
                        data -> assertThat(entryMap.get(data.key())).isEqualTo(data.type())
                )
        );
    }
}
