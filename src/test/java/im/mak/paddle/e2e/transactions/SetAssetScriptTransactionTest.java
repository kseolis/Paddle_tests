package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.SetAssetScriptTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getBalanceAfterTransaction;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.SetAssetScriptTransactionSender.getSetAssetScriptTx;
import static im.mak.paddle.helpers.transaction_senders.SetAssetScriptTransactionSender.setAssetScriptTransactionSender;
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
                .script(SCRIPT_PERMITTING_OPERATIONS)
                .quantity(1000_00000000L)).tx().assetId();
    }

    @Test
    @DisplayName("set asset script 'ban on updating key values'")
    void setAssetScriptTransactionTest() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            Base64String script = node()
                    .compileScript(fromFile("ride_scripts/permissionOnUpdatingKeyValues.ride")).script();
            setAssetScriptTransactionSender(alice, script, issuedAssetId, v);
            checkSetAssetScriptTransaction(alice, script, issuedAssetId, v);
        }
    }

    private void checkSetAssetScriptTransaction(Account account, Base64String script, AssetId assetId, int version) {
        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(getSetAssetScriptTx().fee().value()).isEqualTo(ONE_WAVES),
                () -> assertThat(getSetAssetScriptTx().fee().value()).isEqualTo(ONE_WAVES),
                () -> assertThat(getSetAssetScriptTx().sender()).isEqualTo(account.publicKey()),
                () -> assertThat(getSetAssetScriptTx().script()).isEqualTo(script),
                () -> assertThat(getSetAssetScriptTx().assetId()).isEqualTo(assetId),
                () -> assertThat(getSetAssetScriptTx().version()).isEqualTo(version),
                () -> assertThat(getSetAssetScriptTx().type()).isEqualTo(15),
                () -> assertThat(account.getWavesBalance()).isEqualTo(getBalanceAfterTransaction())
        );
    }
}
