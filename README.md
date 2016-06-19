# BgmService
This project demonstrates an Android application that BGM is played in.  
In the demo app, the BGM pauses when the application is in background, and resumes in foreground.

##Usage
###1.Sets a music file
You can set a music file that BgmService plays by **BgmSettings.setFileName()**

ex)
```
BgmSettings.setFileName("foo.mp3");
```

###2.Starts BgmService
Start BgmService by calling **Context.bindService()** or **Context.startService()**.

ex)
```
bindService(new Intent(this, BgmService.class), mConnection, BIND_AUTO_CREATE);

private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BgmService.BgmBinder binder = (BgmService.BgmBinder) service;
            mService = binder.getService();

            /*
            when connected with service, starts music
             */
            BgmSettings.setFileName(DEFAULT_BGM_FILE);

            if (!mService.isPlaying()) {
                mService.start(BgmSettings.getFileName());
            }
            mBoundToService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unbindService(mConnection);
            mBoundToService = false;
            mService = null;
        }
    };

```
###3. Controls BGM state
You can send commands to BgmService to control playback states.  
If you would like to pause music, you can call **BgmService.pause()**.

ex)
```
@Override
public void onPause() {
  /* stop music when Activity pauses */ 
  mService.pause();
}
```

And you can resume music as below.

```
@Override
public void onResume() {
  /* resumes music when Activity resumes. */
  mService.resume();
}
```


※In the demo, *SampleBgmApplication* class manages *BgmService*.   
However, you should use *Activity* if you would like to control the playback state precisely.
