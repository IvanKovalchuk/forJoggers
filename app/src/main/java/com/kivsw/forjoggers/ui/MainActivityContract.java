package com.kivsw.forjoggers.ui;

/**
 * this class determinates interfaces for interaction
 * between MainActivity and MainActivityPresenter
 */
public interface MainActivityContract {
    interface IPresenter
        extends IBasePresenter
    {
        void onCreateActivity(MainActivity activity);
        void onDestroyActivity();
        void onStartActivity();
        void onStopActivity();
        boolean actionSaveTrack(String fileName);
        boolean actionLoadTrack(String fileName);
        void actionShowCurrentTrack();
        void actionAnimateTrack();
        void actionExit();
    }
    interface IView
    {

    }
}
