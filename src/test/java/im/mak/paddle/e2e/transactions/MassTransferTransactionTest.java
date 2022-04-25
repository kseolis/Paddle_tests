package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.MassTransferTransaction;
import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;
import com.wavesplatform.transactions.mass.Transfer;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.wavesplatform.transactions.common.AssetId.WAVES;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.accountListGenerator;
import static im.mak.paddle.helpers.Randomizer.getRandomInt;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MassTransferTransactionTest {
    private static Account alice;

    private static AssetId issuedAsset;
    private static Base58String base58StringAttachment;
    private static List<Account> minimumAccountsForMassTransfer;
    private static List<Account> maximumAccountsForMassTransfer;

    long transactionCommission;

    @BeforeAll
    static void before() {
        async(
                () ->  {
                    base58StringAttachment = new Base58String("attachment");
                    alice = new Account(DEFAULT_FAUCET);
                    issuedAsset = alice.issue(i -> i.name("Test_Asset").quantity(9000_00000000L)).tx().assetId();
                },
                () -> minimumAccountsForMassTransfer = accountListGenerator(MIN_NUM_ACCOUNT_FOR_MASS_TRANSFER),
                () -> maximumAccountsForMassTransfer = accountListGenerator(MAX_NUM_ACCOUNT_FOR_MASS_TRANSFER)
            );
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' for maximum Accounts")
    void massTransferForMaximumCountAccounts() {
        int amount = getRandomInt(MIN_TRANSFER_SUM, 10000);
        massTransferTransaction(WAVES, amount, maximumAccountsForMassTransfer);
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' for minimum Accounts")
    void massTransferForMinimumCountAccounts() {
        int amount = getRandomInt(MIN_TRANSFER_SUM, 10000);
        massTransferTransaction(WAVES, amount, minimumAccountsForMassTransfer);
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' issued asset for maximum Accounts")
    void massTransferForMaximumAccountsForIssueAsset() {
        int amount = getRandomInt(MIN_TRANSFER_SUM, 10000);
        massTransferTransaction(issuedAsset, amount, maximumAccountsForMassTransfer);
    }

    @Test
    @DisplayName("issued asset transfer in a 'mass transfer transaction' for minimum Accounts")
    void massTransferForMinimumAccountsForIssueAsset() {
        int amount = getRandomInt(MIN_TRANSFER_SUM, 10000);
        massTransferTransaction(issuedAsset, amount, minimumAccountsForMassTransfer);
    }

    private void massTransferTransaction(AssetId assetId, long amount, List<Account> accountsList) {
        List<Transfer> transfers = new ArrayList<>();
        Map<Address, Long> balancesAfterTransaction = new HashMap<>();

        accountsList.forEach(a -> transfers.add(Transfer.to(a.address(), amount)));
        accountsList.forEach(a -> balancesAfterTransaction.put(a.address(), a.getBalance(assetId) + amount));

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
                        account -> assertThat(
                                balancesAfterTransaction.get(account.address())).isEqualTo(account.getBalance(assetId)
                        )
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

