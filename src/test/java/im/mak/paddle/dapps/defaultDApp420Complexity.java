package im.mak.paddle.dapps;

import im.mak.paddle.dapp.DApp;

import static im.mak.paddle.util.ScriptUtil.fromFile;

public class defaultDApp420Complexity extends DApp {
    public static final String INITIAL_SCRIPT = fromFile("scriptWith420Complexity.ride");

    public defaultDApp420Complexity(long initialBalance) {
        super(initialBalance, INITIAL_SCRIPT);
    }
}
