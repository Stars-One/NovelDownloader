package wan.noveldownloader.bean;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import wan.noveldownloader.controller.ItemController;
import wan.noveldownloader.utils.DownloadTool;

/**
 * @author StarsOne
 * @date Create in  2019/7/28 0028 21:54
 * @description
 */
public class DownloadingItem {
    private String downloadPath;
    private Map<Integer, String> maps;
    private String name;
    private String imgPath;
    private StringProperty flagTextProperty = new SimpleStringProperty("下载目录");
    private StringProperty percentProgressTextProperty = new SimpleStringProperty("0.00%");//百分比text
    private int allCount;//全部章节数
    private int hasDownloadCount = 0;//已下载章节
    private String progressText = "";// 文字（已下载/总下载 2/168）
    private boolean isPause = false;//暂停标志
    private ItemController item;
    private Map<Integer, String> tempFileMaps = new HashMap<>();//已缓存的章节，需要合并
    private Task<Void> task;

    public DownloadingItem(String name, String imgPath, String downloadPath, Map<Integer, String> maps) {
        this.downloadPath = downloadPath;
        this.maps = maps;
        this.name = name;
        this.imgPath = imgPath;
        this.allCount = maps.size();
        setProcessText();

        //缓存文件名开头
        String url = maps.get(0);
        int start = url.indexOf("k");
        int end = url.lastIndexOf("/");
        String tempName = url.substring(start + 2, end) + "_";

        task = new Task<Void>() {
            @Override
            protected void succeeded() {
                super.succeeded();
                System.out.println("下载成功");
            }

            @Override
            protected Void call() throws Exception {
                int i = 0;
                //下载每一章
                while (i < maps.size()) {
                    if (!isPause) {
                        String url = maps.get(i);
                        DownloadTool.downloadChapter(url, downloadPath, i);
                        //保存章节缓存，之后合并文件需要
                        tempFileMaps.put(i, downloadPath+tempName + i + ".txt");
                        //更新进度条的进度
                        updateProgress(i, maps.size());
                        hasDownloadCount = i;
                        //更新已下载章节数
                        setProcessText();
                        updateMessage(getProcessText());
                        //百分比进度条文字显示
                        setPercentProgressTextProperty();
                        i++;
                    } else {
                        Thread.sleep(10);
                    }
                }
                percentProgressTextProperty.set("100.00%");
                //合并
                flagTextProperty.set("合并文件中");
                for (i = 0; i < allCount; i++) {
                    String s = tempFileMaps.get(i);
                    try {
                        List<String> list = FileUtils.readLines(new File(s), "GBK");
                        FileUtils.writeLines(new File("Q:\\test", name + ".txt"), "GBK", list, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (i + 1 == allCount) {
                        flagTextProperty.set("合并完成");
                    }
                }
                return null;
            }
        };
    }

    private void setPercentProgressTextProperty() {
        double temp = hasDownloadCount / (allCount * 1.0);
        DecimalFormat df = new DecimalFormat("#.00");
        percentProgressTextProperty.set(df.format(temp * 100) + "%");
    }

    public StringProperty getPercentProgressTextProperty() {
        return percentProgressTextProperty;
    }
    public StringProperty getFlagTextProperty() {
        return flagTextProperty;
    }

    private void setProcessText() {
        progressText = hasDownloadCount + 1 + "/" + allCount;
    }

    private String getProcessText() {
        return progressText;
    }

    public void setFlag() {
        this.isPause = !isPause;
    }

    public void setFlag(boolean flag) {
        this.isPause = flag;
    }


    public void setFlagTextProperty(String flagTextProperty) {
        this.flagTextProperty.set(flagTextProperty);
    }

    public ItemController getItem() {
        return item;
    }

    public void setItem(ItemController item) {
        this.item = item;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public Map<Integer, String> getMaps() {
        return maps;
    }

    public String getName() {
        return name;
    }

    public String getImgPath() {
        return imgPath;
    }

    public Task<Void> getTask() {
        return task;
    }
}