package im.mak.paddle.dapps;

import im.mak.paddle.dapp.DApp;

import static im.mak.paddle.util.ScriptUtil.fromFile;

public class DefaultDApp420Complexity extends DApp {
    public static final String INITIAL_SCRIPT = fromFile("ride_scripts/scriptWith420Complexity.ride");

    public DefaultDApp420Complexity(long initialBalance) {
        super(initialBalance, INITIAL_SCRIPT);
    }
}
