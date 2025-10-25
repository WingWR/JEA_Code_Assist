package com.tongji.jea.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.tongji.jea.model.ContextItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 项目级 Service：存储上下文并发布变更通知。
 */
@Service(Service.Level.PROJECT)
public final class ContextManagerService {

    public interface Listener {
        /**
         * 上下文列表发生变化（增/删/清空）时回调。
         * 注意：监听器必须在被调用时自行保证在 UI 线程操作（或在回调前使用 invokeLater）。
         */
        void onContextChanged(@NotNull List<ContextItem> newList);
    }

    private final List<ContextItem> items = new ArrayList<>();
    // 线程安全的监听器列表（可在任意线程注册/取消、在任意线程触发）
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    public ContextManagerService(Project project) {
        // constructor can be empty
    }

    public static ContextManagerService getInstance(@NotNull Project project) {
        return project.getService(ContextManagerService.class);
    }

    public synchronized void addContext(@NotNull ContextItem item) {
        items.add(item);
        notifyListeners();
    }

    public synchronized void removeContext(@NotNull ContextItem item) {
        items.remove(item);
        notifyListeners();
    }

    public synchronized void clearContexts() {
        items.clear();
        notifyListeners();
    }

    public synchronized List<ContextItem> getAllContexts() {
        return List.copyOf(items);
    }

    public synchronized String buildFullContextText() {
        if (items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            ContextItem it = items.get(i);
            sb.append("【上下文 ").append(i + 1).append("】\n");
            sb.append("标签: ").append(it.getLabel()).append("\n");
            sb.append(it.getContent()).append("\n\n");
        }
        return sb.toString();
    }

    /** 注册监听器（可在任意线程调用） */
    public void addListener(@NotNull Listener l) {
        listeners.addIfAbsent(l);
    }

    /** 取消监听器 */
    public void removeListener(@NotNull Listener l) {
        listeners.remove(l);
    }

    /** 通知所有监听器（在 Service 锁内调用） */
    private void notifyListeners() {
        // 创建快照，避免并发问题
        List<ContextItem> snapshot = new ArrayList<>(items);
        for (Listener l : listeners) {
            try {
                l.onContextChanged(Collections.unmodifiableList(snapshot));
            } catch (Throwable t) {
                // 保护性捕获，避免单个监听器抛异常影响其他监听器
                t.printStackTrace();
            }
        }
    }
}
