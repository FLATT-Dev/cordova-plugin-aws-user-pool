
echo Removing aws plugin..
call cordova plugin remove cordova-plugin-aws-user-pool
echo adding aws plugin..
call cordova plugin add ..\..\ --nofetch
echo running android build
call cordova run android --device