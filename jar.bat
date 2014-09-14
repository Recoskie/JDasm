@echo off
call %cd:~0,3%java\bin\jar.exe -cmf m.mf DecoderApp.jar *.class AppPictures Icons EXEDecode
pause