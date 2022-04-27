package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.SetAssetScriptTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import com.wavesplatform.wavesj.info.IssueTransactionInfo;
import com.wavesplatform.wavesj.info.SetAssetScriptTransactionInfo;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SetAssetScriptTransactionTest {

    private static Account alice;
    private static AssetId issuedAssetId;

    @BeforeAll
    static void before() {
        alice = new Account(DEFAULT_FAUCET);
        issuedAssetId = alice.issue(i -> i.name("Test_Asset")
                .script("{-# SCRIPT_TYPE ASSET #-} false")
                .quantity(1000_00000000L)).tx().assetId();
    }

    @Test
    @DisplayName("setScriptTransaction")
    void setScriptTransactionTest() {
        Base64String script = node().compileScript("{-# SCRIPT_TYPE ASSET #-} true").script();
        setAssetScriptTransaction(script);
    }

    private void setAssetScriptTransaction(Base64String script) {

        Account alice = new Account(10_00000000);

        SetAssetScriptTransaction tx = SetAssetScriptTransaction.builder(issuedAssetId, script).getSignedWith(alice.privateKey());
        node().waitForTransaction(node().broadcast(tx).id());

        TransactionInfo commonInfo = node().getTransactionInfo(tx.id());
        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(tx.sender()).isEqualTo(alice.publicKey()),
                () -> assertThat(tx.type()).isEqualTo(15),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(ONE_WAVES)
        );
    }
}
