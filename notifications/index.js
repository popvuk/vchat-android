 var FCM = require('fcm-node');
 var firebase = require('firebase');
 var serverKey = 'AAAAgLi5DjM:APA91bGJuil4ffI_IiQgH0dWqIPOLs80WZBNPdWxVK1Sm4HjmnBkSzx4-pzEp2l3fplKVv3ZusxV6IzpkaIz72LZHCI1mBfB4T4QTFjDtwKfpGuZKlCKOoN0rHFezf_IRo8JqqIx4mL6';//put the generated private key path here    
 var fcm = new FCM(serverKey);
 var config = {
    apiKey: "AIzaSyABQO59aIGnDvONsmoFRhIqQfn3if8ScWk",
    databaseURL: "https://chatapp-72d9c.firebaseio.com",
    storageBucket: "chatapp-72d9c.appspot.com"
 };
 firebase.initializeApp(config);
 var database = firebase.database();
 
 function setListener()
 {
	 var users = database.ref('notifications');
	 users.on('child_added', function(snapshot){

	    var sender = snapshot.val().from;
		var receiver = snapshot.val().to;
		var mssg = snapshot.val().message;
		var mssgKey = snapshot.key;
		
		sendNotification(sender, receiver, mssg, mssgKey);
		
	 });
 }
 
 function sendNotification(senderId, receiverId, mssgBody, mssgKey)
 {
	var senderUsername;
	var receiverUsername;
	var receiverToken;
	
	var sender = database.ref('users/'+senderId);
	sender.once('value').then(function(snapshot){
		
		senderUsername = snapshot.val().username;
		
		var receiver = database.ref('users/'+receiverId);
		receiver.once('value').then(function(snapshot){
			
			receiverUsername = snapshot.val().username;
			receiverToken = snapshot.val().token;
			
			sendMessage(receiverToken, senderUsername, mssgBody, mssgKey);
		});
	});
 }

 function sendMessage(token, sender, message, mssgKey)
 {
	var message = { //this may vary according to the message type (single recipient, multicast, topic, et cetera)
        to: token, 
        
        notification: {
            title: sender, 
            body: message,
            sound: 'default'			
        },
    };
    
    fcm.send(message, function(err, response){
        if (err) 
		{
            console.log("Something has gone wrong!");			
        } 
		else 
		{
            console.log("Successfully sent with response: ", response);
			var notification = database.ref('notifications/'+mssgKey);
			notification.remove();
        }
    });  
 } 
 setListener();	
    