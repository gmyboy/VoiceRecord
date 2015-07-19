package com.gmy.voicerecord.view;

/**
 * 录音按钮接口
 *
 * @author GMY
 * @mail 2275964276@qq.com
 * @date 2015年6月2日
 */
public interface IRecordButton {
    /**
     * 录音准备工作，重置录音文件名等
     */
    public void ready();

    /**
     * 开始录音
     */
    public void start();

    /**
     * 完成完整录音操作
     */
    public void complite(float time);

    /**
     * 录音结束
     */
    public void stop();

    /**
     * 录音失败时删除原来的旧文件
     */
    public void deleteOldFile();

    /**
     * 获取录音音量的大小
     *
     * @return
     */
    public double getAmplitude();

    /**
     * 返回录音文件完整路径
     *
     * @return
     */
    public String getFilePath();
}
