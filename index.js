const functions = require('firebase-functions');
const request = require('request');
const rp = require('request-promise');
const Jimp = require('jimp');
const {
  Storage
} = require('@google-cloud/storage');
const storage = new Storage({
  projectId: 'data-reciever'
});
const Firestore = require('@google-cloud/firestore');
const firestore = new Firestore({
  projectId: 'data-reciever'
});
const vision = require('@google-cloud/vision');
const client = new vision.ImageAnnotatorClient();


exports.filter = functions.https.onCall((data, context) => {
  const text = data.text;
  const url = "gs://data-reciever.appspot.com/" + text;


  var syncronizer = 0;

  const promises = [];
  const p1 = client
    .labelDetection(url);
  promises.push(p1);

  const p2 = client
    .objectLocalization(url);
  promises.push(p2);


  const p3 = client
    .landmarkDetection(url);
  promises.push(p3);

  const p4 = client
    .logoDetection(url);
  promises.push(p4);

  const p5 = client
    .webDetection(url);
  promises.push(p5);

  return Promise.all(promises).then(results => {
    var globalArray = [];
    var labelArr = [];
    var objectArr = [];
    var locationArr = [];
    var logoArr = [];
    var webEquivArr = [];

    const labels = results[0][0].labelAnnotations;

    labels.forEach(label => {
      if (label.score >= 0.5 && !globalArray.includes(label)) {
        labelArr.push(label);
        globalArray.push(label);
      }
    });

    const objects = results[1][0].localizedObjectAnnotations;
    objects.forEach(object => {
      var words = object.name.split(" ");
      for(var i = 0 ; i < words.length ; i++){
        if(!globalArray.includes(words[i])){
            objectArr.push(words[i]);
            globalArray.push(words[i]);
        }
      }
    });

    const landmarks = results[2][0].landmarkAnnotations;
    landmarks.forEach(landmark => {
      if(!globalArray.includes(landmark.description)){
        locationArr.push(landmark.description);
        globalArray.push(landmark.description);
      }
    });

    const logos = results[3][0].logoAnnotations;
    logos.forEach(logo => {
      if(!globalArray.includes(logo.description)){
        logoArr.push(logo.description);
        globalArray.push(logo.description);
      }
    });

    const webDetection = results[4][0].webDetection;
    webDetection.webEntities.forEach(webEntity => {
      if (webEntity.score > 0.7 && !globalArray.includes(webEntity.description)) {
          webEquivArr.push(webEntity.description);
          globalArray.push(webEntity.description);
      }
    });
    console.log({
      webMatches: webEquivArr,
      logoMatches: logoArr,
      objectMatches: objectArr,
      locationMatches: locationArr,
      labelMatches: labelArr
    });
    return {
      webMatches: webEquivArr,
      logoMatches: logoArr,
      objectMatches: objectArr,
      locationMatches: locationArr,
      labelMatches: labelArr
    };
  });


});


exports.searchSingle = functions.https.onCall((data, context) => {

  const tag = data.tag;
  const timestamp = data.timestamp;
  const id = data.id;
  const url = data.url;
  const apiSlot = data.apiSlot;
  const isLocation = data.isLocation;

  //apiSlots (for load balancing optimization)
  var apiKeys = ["0V0Z0cEeG7hhKsL16yyXyVc5IP1yPCmE",
    "Tm5l1xhQjAAAqhzkFuHcjhZXOm9PwpkC", 'lvqthSVLKz7nYoZu6eRotJEGNrEiMEPc'
  ];

  const agentIDs = [84758, 97490, 97496];


  return crawl(tag, timestamp, id, url, agentIDs, apiKeys, apiSlot,isLocation, 0);

});

function crawl(originalTag, timestamp, id, url, agentIDs, apiKeys, apiSlot,isLocation, searchOccurences) {

  const tag = isLocation?originalTag:'#'+originalTag.toLowerCase().replace(/[\s()-]/g, "");

  //crawling http request meta-data
  var headers = {
    'Content-Type': 'application/json',
    'X-Phantombuster-Key-1': apiKeys[apiSlot]
  };

  dataString = '{"output":"result-object","argument":{"spreadsheetUrl":"' + tag + '","sessionCookie":"7120273387%3AFUFJEsjHiEd1uM%3A27","numberOfLinesPerLaunch":10,"maxPosts":500}}';

  tagCrawlOptions = {
    method: 'POST',
    uri: 'https://phantombuster.com/api/v1/agent/' + agentIDs[apiSlot] + '/launch',
    headers: headers,
    body: dataString
  };

  return rp(tagCrawlOptions)
    .then(function(body) {

      const postPromises = [];
      const posts = JSON.parse(body).data.resultObject;

      //retrieve image from url
      postPromises.push(Jimp.read(url));

      //go through each post
      for (var i = 0; i < posts.length; i++) {
        var post = posts[i];

        if (  post.imgUrl !== "" ) {
          //retrieve each image from url
          postPromises.push(Jimp.read(post.imgUrl));
          postPromises.push(new Promise((resolve, reject) => resolve(post.profileUrl)));
        }
      }
      return Promise.all(postPromises);
    }).then(resu => {
      const promises = [];
      //go through each image
      for (var i = 1; i < resu.length; i += 2) {
        //compare perceptual hash. If the two images are the same then it equals 0.
        if (Jimp.distance(resu[0], resu[i]) === 0) {

          //matched
          //profileCrawlingHTTP requests
          const dataString1 = '{"output":"result-object","argument":{"sessionCookie":"7120273387%3AFUFJEsjHiEd1uM%3A27","spreadsheetUrl":"' + resu[i + 1] + '","numberOfPostsPerProfile":200,"numberOfProfilesPerLaunch":10}}';
          const dataString2 = '{"output":"result-object","argument":{"sessionCookie":"7120273387%3AFUFJEsjHiEd1uM%3A27","spreadsheetUrl":"' + resu[i + 1] + '","numberOfProfilesPerLaunch":10}}';

          var headerOfProfileCrawl = {
            'Content-Type': 'application/json',
            'X-Phantombuster-Key-1': '5YjufraEbo0GQT3nvHXc0Iibpf9VicIj'
          };

          var profilePostsCrawlOptions = {
            uri: 'https://phantombuster.com/api/v1/agent/97484/launch',
            method: 'POST',
            headers: headerOfProfileCrawl,
            body: dataString1
          };

          var profileCrawlOptions = {
            uri: 'https://phantombuster.com/api/v1/agent/97481/launch',
            method: 'POST',
            headers: headerOfProfileCrawl,
            body: dataString2
          };


          const p1 = rp(profilePostsCrawlOptions);
          promises.push(p1);

          const p2 = rp(profileCrawlOptions);
          promises.push(p2);
          break;
        }

      }
      return Promise.all(promises);


    }).then(resu => {
      //if no match occured
      if (resu.length === 0) {
        if(searchOccurences === 0 && !isLocation && originalTag.indexOf(' ') === -1 ){
        return new Promise((resolve, reject) => resolve(crawl(tag,
            timestamp,
            id,
            url,
            agentIDs,
            apiKeys,
            apiSlot,
            isLocation,
             searchOccurences + 1)));
        }

        return new Promise((resolve, reject) => resolve({
          status: "fail1",
          tag: tag
        }));
      }

      //store in firebase
      const postResult = JSON.parse(resu[0]).data.resultObject;
      var batch = firestore.batch();


      var delRef = firestore.collection("store").doc(id);
      batch.delete(delRef);

      for (var i = 0; i < postResult.length; i++) {
        var pRef = firestore.collection("store").doc(id).collection("posts").doc();
        batch.set(pRef, postResult[i]);
      }

      const profileResult = JSON.parse(resu[1]).data.resultObject;
      var profileRef = firestore.collection("users").doc(id);

      batch.set(profileRef, profileResult[0]);
      return batch.commit().then(output => {
        return new Promise((resolve, reject) => resolve({
          status: "success"
        }));
      });
    })
    .catch(function(err) {
      //if error occurs try another api slot
      if (searchOccurences <= 2) {
        newApiSlot = (apiSlot + 1 === 3) ? 0 : apiSlot + 1;

        return new Promise((resolve, reject) => resolve(crawl(tag,
          timestamp,
          id,
          url,
          agentIDs,
          apiKeys,
          newApiSlot,
          isLocation,
           searchOccurences + 1)));
      }

      return new Promise((resolve, reject) => resolve({
        status: "fail"
      }));
    });
}
