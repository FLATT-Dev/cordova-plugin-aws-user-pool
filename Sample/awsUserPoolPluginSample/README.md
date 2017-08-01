#NOTE TO ANDROID DEVELOPERS
Added Android support for basic login and registration without confirmation. 
Login takes about 2 seconds on my low end Galaxy 3 - a bit better than 10-15 seconds it took with AWS javascript ....
The sample app sign up button does not work -  don't have time to  tinker with it.
That's all I need at the moment. Feel free to contribute
I'm going to add features as I need them in my project

Andy


-----------------------------------------------
# How to make sample work

1) Change the credential :

go to the file www/js/index.js and change the following by your credential :

CognitoIdentityUserPoolId: "eu-west-1_*********"

CognitoIdentityUserPoolAppClientId: "*********************"

CognitoIdentityUserPoolAppClientSecret: "************************"

CognitoArnIdentityPoolId: "eu-west-1:********-****-****-****-************"

Change the region by yours, here are the possibilities :

AwsUserPoolPlugin.AwsUserPoolPluginEnum = {
	RegionUnknown: 0,
	UsEast1: 1,
	UsEast2: 2,
	UsWest1: 3,
	UsWest2: 4,
	ApSouth1: 5,
	ApNortheast1: 6,
	ApNortheast2: 7,
	ApSoutheast1: 8,
	ApSoutheast2: 9,
	EuCentral1: 10,
	EuWest1: 11,
	EuWest2: 12
}

If you don't have any CognitoIdentityUserPoolAppClientSecret just set it to null

Here is where you can find your settings :

- UserPoolAppClients

![alt text](https://img15.hostingpics.net/pics/534932userPoolAppClients.png)

- UserPoolPoolDetails :

![alt text](https://img15.hostingpics.net/pics/193176UserPoolPoolDetails.png)

- FederalIdentitiesSettings

![alt text](https://img15.hostingpics.net/pics/549772FederalIdentitiesSettings.png)

cordova platform add ios

cordova plugin add ../../

cordova build ios