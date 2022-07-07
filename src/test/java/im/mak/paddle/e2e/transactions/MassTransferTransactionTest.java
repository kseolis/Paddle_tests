package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;
import im.mak.paddle.dapps.DefaultDApp420Complexity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.wavesplatform.transactions.MassTransferTransaction.LATEST_VERSION;
import static com.wavesplatform.transactions.common.AssetId.WAVES;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.helpers.Calculations.getTransactionCommission;
import static im.mak.paddle.helpers.Randomizer.*;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.MassTransferTransactionSender.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MassTransferTransactionTest {
    private static Account account;

    private static AssetId issuedAsset;
    private static List<Account> minimumAccountsList;
    private static List<Account> maximumAccountsList;

    private static DefaultDApp420Complexity dAppAccount;
    private static AssetId issuedSmartAssetId;

    @BeforeAll
    static void before() {
        async(
                () -> {
                    account = new Account(DEFAULT_FAUCET);
                    issuedAsset = account.issue(i -> i.name("Test_Asset").quantity(900_000_000_000L).script(null)
                    ).tx().assetId();
                },
                () -> {
                    dAppAccount = new DefaultDApp420Complexity(DEFAULT_FAUCET);
                    dAppAccount.createAlias(randomNumAndLetterString(15));
                    issuedSmartAssetId = dAppAccount.issue(i -> i.name("Smart")
                            .quantity(900_000_000_000L)
                            .script(SCRIPT_PERMITTING_OPERATIONS)).tx().assetId();
                },
                () -> minimumAccountsList = accountListGenerator(MIN_NUM_ACCOUNT_FOR_MASS_TRANSFER),
                () -> maximumAccountsList = accountListGenerator(MAX_NUM_ACCOUNT_FOR_MASS_TRANSFER)
        );
    }

    @Test
    @DisplayName("for maximum Accounts")
    void massTransferForMaximumCountAccounts() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            int amount = getRandomInt(MIN_TRANSACTION_SUM, 100);
            massTransferTransactionSender(account, WAVES, amount, maximumAccountsList, v);
            checkMassTransferTransaction(account, WAVES, amount, maximumAccountsList);
        }
    }

    @Test
    @DisplayName("for minimum Accounts")
    void massTransferForMinimumCountAccounts() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            int amount = getRandomInt(MIN_TRANSACTION_SUM, 100);
            massTransferTransactionSender(account, WAVES, amount, minimumAccountsList, v);
            checkMassTransferTransaction(account, WAVES, amount, minimumAccountsList);
        }
    }

    @Test
    @DisplayName("issued asset for maximum Accounts")
    void massTransferForMaximumAccountsForIssueAsset() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            int amount = getRandomInt(MIN_TRANSACTION_SUM, 100);
            massTransferTransactionSender(account, issuedAsset, amount, maximumAccountsList, v);
            checkMassTransferTransaction(account, issuedAsset, amount, maximumAccountsList);
        }
    }

    @Test
    @DisplayName("issued asset for minimum Accounts")
    void massTransferForMinimumAccountsForIssueAsset() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            int amount = getRandomInt(MIN_TRANSACTION_SUM, 100);
            massTransferTransactionSender(account, issuedAsset, amount, minimumAccountsList, v);
            checkMassTransferTransaction(account, issuedAsset, amount, minimumAccountsList);
        }
    }

    @Test
    @DisplayName("issued smart asset for maximum Accounts")
    void massTransferForMaximumAccountsForIssueSmartAsset() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            int amount = getRandomInt(MIN_TRANSACTION_SUM, 100);
            massTransferTransactionSender(dAppAccount, issuedSmartAssetId, amount, maximumAccountsList, v);
            checkMassTransferTransaction(dAppAccount, issuedSmartAssetId, amount, maximumAccountsList);
        }
    }

    @Test
    @DisplayName("issued smart asset for minimum Accounts")
    void massTransferForMinimumAccountsForIssueSmartAsset() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            int amount = getRandomInt(MIN_TRANSACTION_SUM, 100);
            massTransferTransactionSender(dAppAccount, issuedSmartAssetId, amount, minimumAccountsList, v);
            checkMassTransferTransaction(dAppAccount, issuedSmartAssetId, amount, minimumAccountsList);
        }
    }

    private void checkMassTransferTransaction(Account sender, AssetId assetId, long amount, List<Account> accountsList) {
        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(sender.getBalance(assetId)).isEqualTo(getSenderBalanceAfterMassTransfer()),
                () -> assertThat(getMassTransferTx().attachment()).isEqualTo(getAttach()),
                () -> assertThat(getMassTransferTx().assetId()).isEqualTo(assetId),
                () -> assertThat(getMassTransferTx().fee().assetId()).isEqualTo(WAVES),
                () -> assertThat(getMassTransferTx().fee().value()).isEqualTo(getTransactionCommission()),
                () -> assertThat(getMassTransferTx().sender()).isEqualTo(sender.publicKey()),
                () -> assertThat(getMassTransferTx().transfers().size()).isEqualTo(getAccountsSize()),
                () -> assertThat(getMassTransferTx().type()).isEqualTo(11),
                () -> getMassTransferTx().transfers().forEach(
                        transfer -> assertThat(transfer.amount()).isEqualTo(amount)),
                () -> accountsList.forEach(
                        account -> assertThat(getBalancesAfterTransaction()
                                .get(account.address())).isEqualTo(account.getBalance(assetId))
                )
        );
    }
}


