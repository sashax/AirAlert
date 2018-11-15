Air Alert

Android App for checking air quality.

To successfully compile this app you need to sign up for an API Key at https://docs.airnowapi.org/

This app is currently notably unscalable, as airnow only allows 100 requests/key/hour, which obvs aint gonna cut it. 

The app assumes there is a String resource called `api_key` that contains your API Key (I put it in a vars.xml file, but it doesn't matter as long as it's in the res/values/ folder.
