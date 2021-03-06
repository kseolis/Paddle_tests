package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.SetAssetScriptTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.SetAssetScriptTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.ScriptUtil.fromFile;
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
    @DisplayName("set asset script 'ban on updating key values'")
    void setAssetScriptTransactionTest() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            Base64String script = node().compileScript(fromFile("/permissionOnUpdatingKeyValues.ride")).script();
            setAssetScriptTransaction(alice, script, issuedAssetId, v);
        }
    }

    private void setAssetScriptTransaction(Account account, Base64String script, AssetId assetId, int version) {
        long balanceAfterTransaction = account.getWavesBalance() - ONE_WAVES;
        SetAssetScriptTransaction setAssetScriptTx = SetAssetScriptTransaction
                .builder(issuedAssetId, script).version(version).getSignedWith(account.privateKey());
        node().waitForTransaction(node().broadcast(setAssetScriptTx).id());
        TransactionInfo setAssetScriptTxInfo = node().getTransactionInfo(setAssetScriptTx.id());

        assertAll(
                () -> assertThat(setAssetScriptTxInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(setAssetScriptTx.fee().value()).isEqualTo(ONE_WAVES),
                () -> assertThat(setAssetScriptTx.fee().value()).isEqualTo(ONE_WAVES),
                () -> assertThat(setAssetScriptTx.sender()).isEqualTo(account.publicKey()),
                () -> assertThat(setAssetScriptTx.script()).isEqualTo(script),
                () -> assertThat(setAssetScriptTx.assetId()).isEqualTo(assetId),
                () -> assertThat(setAssetScriptTx.version()).isEqualTo(version),
                () -> assertThat(setAssetScriptTx.type()).isEqualTo(15),
                () -> assertThat(account.getWavesBalance()).isEqualTo(balanceAfterTransaction)
        );
    }
}
