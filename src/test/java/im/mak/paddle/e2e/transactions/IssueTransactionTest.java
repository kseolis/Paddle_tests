package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.IssueTransaction;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import im.mak.paddle.dapps.defaultDApp420Complexity;
import org.junit.jupiter.api.*;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class IssueTransactionTest {

    private static Account alice;
    private static Account bob;
    private long initBalance;

    @BeforeAll
    static void before() {
        alice = new Account(DEFAULT_FAUCET);
        bob = new defaultDApp420Complexity(DEFAULT_FAUCET);
    }

    @Test
    @DisplayName("test issue minimum assets via 'issue Transaction'")
    void issueMinimumAssetsTransactionTest() {
        issueTransaction(alice,
                "T_asset_min",
                "Test Asset",
                ASSET_QUANTITY_MINIMUM,
                "true",
                ASSET_DECIMALS_MIN,
                true,
                ONE_WAVES);
    }

    @Test
    @DisplayName("test issue maximum assets via 'issue Transaction' for smart account")
    void issueMaximumAssetsTransactionTest() {
        long fee = ONE_WAVES + EXTRA_FEE;
        issueTransaction(bob,
                "T_asset",
                "Test maximum quantity for assets",
                ASSET_QUANTITY_MAXIMUM,
                null,
                ASSET_DECIMALS_MAX,
                false,
                fee);
    }

    @Test
    @DisplayName("test issue NFT")
    void issueNfrAsset() {
        issueTransactionForNft(alice, "Crazy_Sparrow", "Amazing NFT", null);
    }

    private void issueTransaction(Account account, String assetName, String description, long quantity, String script,
                                  byte decimals, boolean reIssuable, long fee) {
        initBalance = account.getWavesBalance();
        IssueTransaction tx = account.issue(i ->
                i.name(assetName)
                        .description(description)
                        .quantity(quantity)
                        .script(script)
                        .decimals(decimals)
                        .reissuable(reIssuable)).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(account.getAssetBalance(tx.assetId())).isEqualTo(quantity),
                () -> assertThat(account.getWavesBalance()).isEqualTo(initBalance - fee),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.sender()).isEqualTo(account.publicKey()),
                () -> assertThat(tx.reissuable()).isEqualTo(reIssuable),
                () -> assertThat(tx.fee().value()).isEqualTo(fee),
                () -> assertThat(tx.type()).isEqualTo(3)
        );
    }

    private void issueTransactionForNft(Account account, String assetName, String description, String script) {

        initBalance = account.getWavesBalance();

        IssueTransaction tx = account.issueNft(i -> i.name(assetName).description(description).script(script)).tx();

        TransactionInfo transactionInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(account.getWavesBalance()).isEqualTo(initBalance - MIN_FEE),
                () -> assertThat(transactionInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.sender()).isEqualTo(account.publicKey()),
                () -> assertThat(tx.fee().value()).isEqualTo(MIN_FEE),
                () -> assertThat(tx.reissuable()).isEqualTo(false),
                () -> assertThat(tx.type()).isEqualTo(3)
        );
    }
}
