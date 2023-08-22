package cn.snow.limiter.local;

import lombok.extern.java.Log;

@Log
public class BizController {

    public void loginByBsicAuth(){
        log.info("login by basic auth...");
    }

    public void loginByToken(){
        log.info("login by token...");
    }
}
