package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.SetAssetScriptTransaction;
import com.wavesplatform.transactions.TransferTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
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
                .script("{-# SCRIPT_TYPE ASSET #-} true")
                .quantity(1000_00000000L)).tx().assetId();
    }

    @Test
    @DisplayName("set script to disable transactions")
    void setAssetScriptTransactionTest() {
        Base64String script = node().compileScript("{-# SCRIPT_TYPE ASSET #-} false").script();
        setAssetScriptTransaction(alice, script, issuedAssetId);
    }

    private void setAssetScriptTransaction(Account account, Base64String script, AssetId assetId) {
        long balanceAfterTransaction = account.getWavesBalance() - ONE_WAVES;
        SetAssetScriptTransaction setAssetScriptTx = SetAssetScriptTransaction
                .builder(issuedAssetId, script).getSignedWith(account.privateKey());
        node().waitForTransaction(node().broadcast(setAssetScriptTx).id());
        TransactionInfo setAssetScriptTxInfo = node().getTransactionInfo(setAssetScriptTx.id());

        assertAll(
                () -> assertThat(setAssetScriptTxInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(setAssetScriptTx.fee().value()).isEqualTo(ONE_WAVES),
                () -> assertThat(setAssetScriptTx.sender()).isEqualTo(alice.publicKey()),
                () -> assertThat(setAssetScriptTx.script()).isEqualTo(script),
                () -> assertThat(setAssetScriptTx.assetId()).isEqualTo(assetId),
                () -> assertThat(setAssetScriptTx.type()).isEqualTo(15),
                () -> assertThat(account.getWavesBalance()).isEqualTo(balanceAfterTransaction)
        );
    }
}
