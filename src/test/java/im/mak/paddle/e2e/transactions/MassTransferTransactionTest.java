package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.wavesplatform.transactions.MassTransferTransaction.LATEST_VERSION;
import static com.wavesplatform.transactions.common.AssetId.WAVES;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.helpers.Calculations.getTransactionCommission;
import static im.mak.paddle.helpers.Randomizer.accountListGenerator;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.MassTransferTransactionSender.*;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MassTransferTransactionTest {
    private static Account account;

    private static AssetId issuedAsset;
    private static Base58String attachment;
    private static List<Account> minimumAccountsForMassTransfer;
    private static List<Account> maximumAccountsForMassTransfer;

    @BeforeAll
    static void before() {
        async(
                () -> {
                    account = new Account(DEFAULT_FAUCET);
                    issuedAsset = account.issue(i -> i.name("Test_Asset").quantity(900_000_000_000L)).tx().assetId();
                },
                () -> attachment = new Base58String("attachment"),
                () -> minimumAccountsForMassTransfer = accountListGenerator(MIN_NUM_ACCOUNT_FOR_MASS_TRANSFER),
                () -> maximumAccountsForMassTransfer = accountListGenerator(MAX_NUM_ACCOUNT_FOR_MASS_TRANSFER)
        );
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' for maximum Accounts")
    void massTransferForMaximumCountAccounts() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            int amount = getRandomInt(MIN_TRANSACTION_SUM, 100);
            massTransferTransactionSender(account, WAVES, amount, maximumAccountsForMassTransfer, v, attachment);
            checkMassTransferTransaction(WAVES, amount, maximumAccountsForMassTransfer);
        }
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' for minimum Accounts")
    void massTransferForMinimumCountAccounts() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            int amount = getRandomInt(MIN_TRANSACTION_SUM, 100);
            massTransferTransactionSender(account, WAVES, amount, minimumAccountsForMassTransfer, v, attachment);
            checkMassTransferTransaction(WAVES, amount, minimumAccountsForMassTransfer);
        }
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' issued asset for maximum Accounts")
    void massTransferForMaximumAccountsForIssueAsset() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            int amount = getRandomInt(MIN_TRANSACTION_SUM, 100);
            massTransferTransactionSender(account, issuedAsset, amount, maximumAccountsForMassTransfer, v, attachment);
            checkMassTransferTransaction(issuedAsset, amount, maximumAccountsForMassTransfer);
        }
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' issued asset for minimum Accounts")
    void massTransferForMinimumAccountsForIssueAsset() {
        for (int v = 1; v <= LATEST_VERSION; v++) {
            int amount = getRandomInt(MIN_TRANSACTION_SUM, 100);
            massTransferTransactionSender(account, issuedAsset, amount, minimumAccountsForMassTransfer, v, attachment);
            checkMassTransferTransaction(issuedAsset, amount, minimumAccountsForMassTransfer);
        }
    }

    private void checkMassTransferTransaction(AssetId assetId, long amount, List<Account> accountsList) {
        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(account.getBalance(assetId)).isEqualTo(getSenderBalanceAfterMassTransfer()),
                () -> assertThat(getMassTransferTx().attachment()).isEqualTo(attachment),
                () -> assertThat(getMassTransferTx().assetId()).isEqualTo(assetId),
                () -> assertThat(getMassTransferTx().fee().assetId()).isEqualTo(WAVES),
                () -> assertThat(getMassTransferTx().fee().value()).isEqualTo(getTransactionCommission()),
                () -> assertThat(getMassTransferTx().sender()).isEqualTo(account.publicKey()),
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

