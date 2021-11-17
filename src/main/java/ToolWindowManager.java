import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import utils.*;

import javax.swing.*;


public class ToolWindowManager implements ToolWindowFactory {
    private JPanel mPanel;

    private final StockWindow stockWindow = new StockWindow();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        //先加载代理
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        //股票
        Content content_stock = contentFactory.createContent(stockWindow.getmPanel(), "Stock", false);

        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.addContent(content_stock);
        contentManager.setSelectedContent(content_stock);
        LogUtil.setProject(project);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return true;
    }
}
