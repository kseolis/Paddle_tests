package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.MassTransferTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;
import com.wavesplatform.transactions.mass.Transfer;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.wavesplatform.transactions.common.AssetId.WAVES;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MassTransferTransactionTest {
    private static Account alice;

    private static Account account0;
    private static Account account1;
    private static Account account2;
    private static Account account3;
    private static Account account4;

    private static AssetId issuedAsset;
    private static Base58String base58StringAttachment;

    long transactionCommission;

    @BeforeAll
    static void before() {
        base58StringAttachment = new Base58String("attachment");
        async(
                () -> {
                    alice = new Account(DEFAULT_FAUCET);
                    alice.createAlias(randomNumAndLetterString(15));
                    issuedAsset = alice.issue(i -> i.name("Test_Asset").quantity(9000_00000000L)).tx().assetId();
                },
                () -> account0 = new Account(DEFAULT_FAUCET),
                () -> account1 = new Account(DEFAULT_FAUCET),
                () -> account2 = new Account(DEFAULT_FAUCET),
                () -> account3 = new Account(DEFAULT_FAUCET),
                () -> account4 = new Account(DEFAULT_FAUCET)
        );
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' for five Accounts")
    void massTransferFiveAccounts() {
        massTransferTransaction(WAVES, MIN_TRANSFER_SUM, account0, account1, account2, account3, account4);
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' for three Accounts")
    void massTransferThreeAccounts() {
        massTransferTransaction(WAVES, ONE_WAVES, account0, account1, account2);
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' for one Accounts")
    void oneTransferInMassTransfer() {
        massTransferTransaction(WAVES, MIN_TRANSFER_SUM, account0);
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' issued asset for three Accounts")
    void massTransferThreeAccountsForIssueAsset() {
        massTransferTransaction(issuedAsset, 900, account0, account1, account2);
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' issued asset for five Accounts")
    void massTransferFiveAccountsForIssueAsset() {
        massTransferTransaction(issuedAsset, 700, account0, account1, account2, account3, account4);
    }

    @Test
    @DisplayName("issued asset transfer in a 'mass transfer transaction' for one Accounts")
    void oneTransferInMassTransferForIssueAsset() {
        massTransferTransaction(issuedAsset, 400, account0);
    }

    private void massTransferTransaction(AssetId assetId, long amount, Account... accounts) {
        List<Account> accountsList = Arrays.asList(accounts);
        List<Transfer> transfers = new ArrayList<>();
        List<Long> balancesAfterTransaction = new ArrayList<>();

        accountsList.forEach(a -> transfers.add(Transfer.to(a.address(), amount)));
        accountsList.forEach(a -> balancesAfterTransaction.add(a.getBalance(assetId) + amount));

        int numberOfAccounts = accountsList.size();

        long senderBalanceAfterMassTransfer = calculateSenderBalanceAfterTransfer(assetId, amount, numberOfAccounts);

        MassTransferTransaction tx = alice.massTransfer(i -> i
                .attachment(base58StringAttachment)
                .assetId(assetId)
                .transfers(transfers)
        ).tx();
        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(alice.getBalance(assetId)).isEqualTo(senderBalanceAfterMassTransfer),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.attachment()).isEqualTo(base58StringAttachment),
                () -> assertThat(tx.sender()).isEqualTo(alice.publicKey()),
                () -> assertThat(tx.transfers().size()).isEqualTo(numberOfAccounts),
                () -> tx.transfers().forEach(transfer -> assertThat(transfer.amount()).isEqualTo(amount)),
                () -> assertThat(tx.type()).isEqualTo(11),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(transactionCommission),
                () -> accountsList.forEach(
                        account -> assertThat(balancesAfterTransaction.remove(account.getBalance(assetId))).isTrue()
                )
        );
    }

    private long calculateSenderBalanceAfterTransfer(AssetId assetId, long amount, int numberOfAccounts) {
        long senderBalance = alice.getBalance(assetId);
        long numForRoundCheck = 100000;
        long additionalFeeForMassTransfer = FEE_FOR_MASS_TRANSFER * numberOfAccounts;
        if (additionalFeeForMassTransfer % numForRoundCheck != 0) { // The fee value is rounded up to three decimals.
            additionalFeeForMassTransfer = (long) Math.ceil(
                    (float) additionalFeeForMassTransfer / numForRoundCheck
            ) * numForRoundCheck;
        }

        transactionCommission = MIN_FEE + additionalFeeForMassTransfer;
        long transactionSum = amount * numberOfAccounts;

        if(assetId.equals(WAVES)) {
            return senderBalance - transactionCommission - transactionSum;
        }
        return senderBalance - transactionSum;
    }
}

