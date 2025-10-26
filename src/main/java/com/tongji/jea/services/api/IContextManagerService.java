package com.tongji.jea.services.api;

import com.tongji.jea.model.ContextItem;
import com.tongji.jea.services.ContextManagerService;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 上下文管理接口。
 */
public interface IContextManagerService {

    void addListener(@NotNull ContextManagerService.Listener l);

    void removeListener(@NotNull ContextManagerService.Listener l);

    interface Listener {
        void onContextChanged(@NotNull List<ContextItem> newList);
    }

    void addContext(@NotNull ContextItem item);

    void removeContext(@NotNull ContextItem item);

    void clearContexts();

    @NotNull List<ContextItem> getAllContexts();

    String buildFullContextText();

    void addListener(@NotNull Listener l);

    void removeListener(@NotNull Listener l);
}
