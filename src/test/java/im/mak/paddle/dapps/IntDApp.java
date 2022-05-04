package im.mak.paddle.dapps;

import com.wavesplatform.transactions.invocation.Function;
import com.wavesplatform.transactions.invocation.IntegerArg;
import im.mak.paddle.dapp.DApp;
import im.mak.paddle.dapp.DAppCall;

import static im.mak.paddle.util.ScriptUtil.fromFile;

public class IntDApp extends DApp {

    public static final String INITIAL_SCRIPT = fromFile("invoke_scripts/simpleIntScript.ride");

    public IntDApp(long initialBalance) {
        super(initialBalance, INITIAL_SCRIPT);
    }

    public DAppCall setInt(int arg) {
        return new DAppCall(address(), Function.as("setInt", IntegerArg.as(arg)));
    }
}
