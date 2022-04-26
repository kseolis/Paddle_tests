package im.mak.paddle.helpers;

import com.wavesplatform.crypto.Crypto;
import com.wavesplatform.transactions.account.PrivateKey;
import im.mak.paddle.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Randomizer {

    public static String randomNumAndLetterString(final long targetStringLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return generatedString.toLowerCase();
    }

    public static List<Account> accountListGenerator(final int numberAccounts) {
        List<Account> accountsList = new ArrayList<>(numberAccounts);
        for(int i = 0; i < numberAccounts; i++) {
            accountsList.add(new Account(PrivateKey.fromSeed(Crypto.getRandomSeedPhrase())));
        }
        return accountsList;
    }

    public static Integer getRandomInt(final int min, final int max) {
        Random random = new Random();
        int diff = max - min;
        int i = random.nextInt(diff + 1);
        return i + min;
    }
}
