package iservice.sdk.core;

import iservice.sdk.entity.ServiceClientOptions;
import iservice.sdk.exception.WebSocketConnectException;
import iservice.sdk.net.WebSocketClient;
import iservice.sdk.net.WebSocketClientOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mitch on 2020/9/16.
 */
public final class ServiceClient {

    private ServiceClientOptions optionsCache;
    private ServiceClientOptions options;

    private final List<AbstractServiceListener> LISTENERS_CACHE = new ArrayList<>();
    private final List<AbstractServiceListener> LISTENERS = new ArrayList<>();

    private volatile WebSocketClient webSocketClient = null;

    ServiceClient(ServiceClientOptions options, List<AbstractServiceListener> listeners) {
        this.options = options;
        this.LISTENERS.addAll(listeners);
    }

    public void startWebSocketClient() {
        prepareStart();
        if (options.getUri() == null) {
            throw new WebSocketConnectException("uri is null!");
        }
        if (webSocketClient == null) {
            synchronized (this) {
                if (webSocketClient == null) {
                    WebSocketClientOption webSocketClientOption = new WebSocketClientOption();
                    webSocketClientOption.setUri(options.getUri());
                    webSocketClient = new WebSocketClient(webSocketClientOption);
                }
            }
        }
        webSocketClient.start();
    }

    private void prepareStart() {
        if (options != null && LISTENERS.size()>0 ) {
            return;
        }
        options = new ServiceClientOptions();
        options.setUri(optionsCache.getUri());
        LISTENERS.addAll(LISTENERS_CACHE);
    }

    public <T> void callService(T msg) {
        webSocketClient.send(msg);
    }

    <T> void send(T msg) {
        webSocketClient.send(msg);
    }

    void doNotifyAllListener(String msg) {
        // TODO 创建 tx 并签名广播
        System.out.println("Sending request ...");
        System.out.println(msg);

        this.LISTENERS.forEach(listener -> {
            listener.callback(msg);
        });
    }

    /**
     *
     * @param options
     */
    public void setOptions(ServiceClientOptions options) {
        this.optionsCache = options;
    }

    public void refreshListeners(List<AbstractServiceListener> listeners) {
        clearListeners();
        addListeners(listeners);
    }

    public void addListeners(List<AbstractServiceListener> listeners) {
        this.LISTENERS_CACHE.addAll(listeners);
    }

    public void clearListeners() {
        this.LISTENERS_CACHE.clear();
    }

    public WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }
}
