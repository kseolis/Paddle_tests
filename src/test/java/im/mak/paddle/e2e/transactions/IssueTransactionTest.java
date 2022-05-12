package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base64String;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import im.mak.paddle.dapps.defaultDApp420Complexity;
import im.mak.paddle.exceptions.ApiError;
import org.junit.jupiter.api.*;

import static com.wavesplatform.transactions.IssueTransaction.LATEST_VERSION;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.*;
import static im.mak.paddle.util.ScriptUtil.fromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class IssueTransactionTest {

    private static Account normalAccount;
    private static Account dAppAccount;

    @BeforeAll
    static void before() {
        normalAccount = new Account(DEFAULT_FAUCET);
        dAppAccount = new defaultDApp420Complexity(DEFAULT_FAUCET);
    }

    @Test
    @DisplayName("min assets, for all script version")
    void issueMinimumAssetsTransactionTest() {
        Base64String script = node().compileScript(fromFile("/banOnUpdatingKeyValues.ride")).script();
        long fee = ONE_WAVES;
        boolean isReissue = true;

        for (int v = 1; v <= LATEST_VERSION; v++) {
            IssueTransaction tx = IssueTransaction.builder("T_asset_min", ASSET_QUANTITY_MIN, ASSET_DECIMALS_MIN)
                    .description("Test minimum quantity for assets")
                    .script(script)
                    .isReissuable(isReissue)
                    .fee(fee)
                    .version(v)
                    .getSignedWith(normalAccount.privateKey());
            issueTransactionCheck(tx, ASSET_QUANTITY_MIN, fee, isReissue, normalAccount);
        }
    }

    @Test
    @DisplayName("max assets, for smart account and all script version")
    void issueMaximumAssetsTransactionTest() {
        long fee = ONE_WAVES + EXTRA_FEE;
        boolean isReissue = false;

        for (int v = 1; v <= LATEST_VERSION; v++) {
            IssueTransaction tx = IssueTransaction.builder("T_asset", ASSET_QUANTITY_MAX, ASSET_DECIMALS_MAX)
                    .description("Test maximum quantity for assets")
                    .script(null)
                    .isReissuable(isReissue)
                    .fee(fee)
                    .version(v)
                    .getSignedWith(dAppAccount.privateKey());
            issueTransactionCheck(tx, ASSET_QUANTITY_MAX, fee, isReissue, dAppAccount);
        }
    }

    @Test
    @DisplayName("NFT, all script version")
    void issueNfrAsset() {
        boolean isReissue = false;
        for (int v = 1; v <= LATEST_VERSION; v++) {
            IssueTransaction tx = IssueTransaction.builderNFT("Crazy_Sparrow" + "_v" + v)
                    .description("Amazing NFT")
                    .script(null)
                    .fee(MIN_FEE)
                    .version(v)
                    .getSignedWith(normalAccount.privateKey());
            issueTransactionCheck(tx, ASSET_QUANTITY_MIN, MIN_FEE, isReissue, normalAccount);
        }
    }

    private void issueTransactionCheck(IssueTransaction tx, long quantity, long fee, boolean reIssuable, Account acc) {
        long initBalance = acc.getWavesBalance();

        try {
            node().waitForTransaction(node().broadcast(tx).id());
        } catch (ApiError e) {
            assertThat(tx.version()).isEqualTo(1);
            return;
        }

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(acc.getAssetBalance(tx.assetId())).isEqualTo(quantity),
                () -> assertThat(acc.getWavesBalance()).isEqualTo(initBalance - fee),
                () -> assertThat(tx.sender()).isEqualTo(acc.publicKey()),
                () -> assertThat(tx.reissuable()).isEqualTo(reIssuable),
                () -> assertThat(tx.fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(tx.fee().value()).isEqualTo(fee),
                () -> assertThat(tx.type()).isEqualTo(3)
        );
    }
}
