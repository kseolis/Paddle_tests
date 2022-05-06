package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.DataTransaction;
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

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class DataTransactionTest {

    private static Account alice;

    private final Base64String base64String = new Base64String(randomNumAndLetterString(6));
    private final BinaryEntry binaryEntry = BinaryEntry.as("BinEntry", base64String);
    private final BooleanEntry booleanEntry = BooleanEntry.as("Boolean", true);
    private final IntegerEntry integerEntry = IntegerEntry.as("Integer", 210);
    private final StringEntry stringEntry = StringEntry.as("String", "string");

    @BeforeAll
    static void before() {
        alice = new Account(DEFAULT_FAUCET);
    }

    @Test
    @DisplayName("transaction of all data types on dataTransaction")
    void allTypesDataTransactionTest() {
        dataEntryTransaction(binaryEntry, booleanEntry, integerEntry, stringEntry);
    }

    @Test
    @DisplayName("transaction integer dataTransaction")
    void intTypeDataTransactionTest() {
        dataEntryTransaction(integerEntry);
    }

    @Test
    @DisplayName("transaction string dataTransaction")
    void stringTypeDataTransactionTest() {
        dataEntryTransaction(stringEntry);
    }

    @Test
    @DisplayName("transaction binary dataTransaction")
    void binaryTypeDataTransactionTest() {
        dataEntryTransaction(binaryEntry);
    }

    @Test
    @DisplayName("transaction boolean dataTransaction")
    void booleanTypeDataTransactionTest() {
        dataEntryTransaction(booleanEntry);
    }

    private void dataEntryTransaction(DataEntry... dataEntries) {
        long balanceAfterTransaction = alice.getWavesBalance() - MIN_FEE;
        List<DataEntry> dataEntriesAsList = Arrays.asList(dataEntries);
        Map<String, EntryType> entryMap = new HashMap<>();

        dataEntriesAsList.forEach(a -> entryMap.put(a.key(), a.type()));

        DataTransaction tx = alice.writeData(i -> i.data(dataEntries)).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(alice.getWavesBalance()).isEqualTo(balanceAfterTransaction),
                () -> assertThat(tx.fee().value()).isEqualTo(MIN_FEE),
                () -> assertThat(tx.fee().value()).isEqualTo(MIN_FEE),
                () -> assertThat(tx.sender()).isEqualTo(alice.publicKey()),
                () -> assertThat(tx.type()).isEqualTo(12),
                () -> tx.data().forEach(
                        data -> assertThat(entryMap.get(data.key())).isEqualTo(data.type())
                )
        );
    }
}
