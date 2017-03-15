import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;

/**
 * Created by mac on 2017/3/15.
 */
public class JudegNullAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        //获取Editor和Project对象
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (editor == null || project == null)
            return;
        //获取SelectionModel和Document对象
        SelectionModel selectionModel = editor.getSelectionModel();
        Document document = editor.getDocument();
        //拿到选中部分字符串
        String selectedText = selectionModel.getSelectedText();
        //得到选中字符串的起始和结束位置
        int startOffset = selectionModel.getSelectionStart();
        int endOffset = selectionModel.getSelectionEnd();
        //得到最大插入字符串
        int maxOffset = document.getTextLength() - 1;
        //计算选中字符串所在的行号，并通过行号得到下一行的第一个字符的起始偏移量
        int curLineNumber = document.getLineNumber(endOffset);
        int nextLineStartOffset = document.getLineStartOffset(curLineNumber + 1);
        //计算字符串的插入位置
        int insertOffset = maxOffset > nextLineStartOffset ? nextLineStartOffset : maxOffset;
        //对文档进行操作部分代码，需要放入Runnable接口中实现，由IDEA在内部将其通过一个新线程执行
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                document.insertString(insertOffset , genGetterAndSetter(selectedText));
            }
        };
        //加入任务，由IDEA调度执行这个任务
        WriteCommandAction.runWriteCommandAction(project, runnable);
    }

    private String genGetterAndSetter(String str) {
        if (str == null || (str = str.trim()).equals(""))
            return "";
        String text = "\n" + "if(null != " + str + "){" + "\n" + "}";
        return text;
    }

    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        SelectionModel selectionModel = editor.getSelectionModel();
        //如果没有字符串被选中，那么无需显示该Action
        e.getPresentation().setVisible(editor != null && selectionModel.hasSelection());
    }
}
