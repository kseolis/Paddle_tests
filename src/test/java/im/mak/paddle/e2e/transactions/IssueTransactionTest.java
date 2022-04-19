package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.*;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class IssueTransactionTest {

    private static Account lukas;
    private long initBalance;

    @BeforeAll
    static void before() {
        lukas = new Account(DEFAULT_FAUCET);
    }

    @Test
    @DisplayName("test issue minimum assets via 'issue Transaction'")
    void issueMinimumAssetsTransactionTest() {
        issueTransaction("T_asset_min",
                         "Test Asset",
                                    ASSET_QUANTITY_MINIMUM,
                              "true",
                                    ASSET_DECIMALS_MIN,
                          true);
    }

    @Test
    @DisplayName("test issue maximum assets via 'issue Transaction'")
    void issueMaximumAssetsTransactionTest() {
        issueTransaction("T_asset",
                         "Test maximum quantity for assets",
                                    ASSET_QUANTITY_MAXIMUM,
                              null,
                                    ASSET_DECIMALS_MAX,
                          false);
    }

    @Test
    @DisplayName("test issue NFT")
    void issueNfrAsset() {
        issueTransactionForNft("Crazy_Sparrow", "Amazing NFT", null);
    }

    private void issueTransaction
            (String assetName, String description, long quantity, String script, byte decimals, boolean reIssuable) {
        initBalance = lukas.getWavesBalance();
        IssueTransaction tx = lukas.issue(i ->
                i.name(assetName)
                        .description(description)
                        .quantity(quantity)
                        .script(script)
                        .decimals(decimals)
                        .reissuable(reIssuable)).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(lukas.getAssetBalance(tx.assetId())).isEqualTo(quantity),
                () -> assertThat(lukas.getWavesBalance()).isEqualTo(initBalance - ONE_WAVES),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(ONE_WAVES)
        );
    }

    private void issueTransactionForNft(String assetName, String description, String script) {

        initBalance = lukas.getWavesBalance();

        IssueTransaction tx = lukas.issueNft(i -> i.name(assetName).description(description).script(script)).tx();

        TransactionInfo transactionInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(lukas.getWavesBalance()).isEqualTo(initBalance - MIN_FEE),
                () -> assertThat(transactionInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat((Object) transactionInfo.tx().fee().value()).isEqualTo(MIN_FEE)
        );
    }
}