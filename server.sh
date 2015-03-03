key="AIzaSyC7tvl67Wlq7b0Exdf1vVE0496voBRJZnY"
regid="APA91bGUqidKEZ9n2ch_ABDZIhrVkiDQVTpnSxoa43HDdjFwrlPtk7kSx51DRVRMojk9wVYt3-eGTG6FHGGklgH0h1xJqmEu-_vQbzuD6ytoHXFgbu1V8vuqvI09usMBv3OeBXhzC4598tvh-MFezolkkqFVzJSz6WK0qqbEBu5Yz97zYCtWKqrhd-P_6vV8vM-M7TurA4AH"

json='{"registration_ids" : ["APA91bGUqidKEZ9n2ch_ABDZIhrVkiDQVTpnSxoa43HDdjFwrlPtk7kSx51DRVRMojk9wVYt3-eGTG6FHGGklgH0h1xJqmEu-_vQbzuD6ytoHXFgbu1V8vuqvI09usMBv3OeBXhzC4598tvh-MFezolkkqFVzJSz6WK0qqbEBu5Yz97zYCtWKqrhd-P_6vV8vM-M7TurA4AH"], "data" : { "a" : "a", "b": "b"}}'
json="'"${json}"'"
echo "curl --header 'Authorization: key=${key}' --header Content-Type:'application/json' https://android.googleapis.com/gcm/send -d ${json}"
