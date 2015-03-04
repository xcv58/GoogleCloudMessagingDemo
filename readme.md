You need first have a get request for this url:

```bash
https://maybe.xcv58.me/maybe-api-v1/devices/{deviceid}
```

It may return:
```json
{"message":"No Record(s) Found"}
```
or
```json
{"_id":"001","deviceid":"001","fake":[{"label":"simple test","choice":0},{"label":"another test","choice":0},{"label":"block test","choice":0},{"label":"third block test","choice":0},{"label":"another block test","choice":0}],"queryCount":7,"testing_inputs.maybe":[{"label":"simple test","choice":0},{"label":"another test","choice":0},{"label":"block test","choice":2},{"label":"third block test","choice":2},{"label":"another block test","choice":0}],"values":{"button1":false}}]
```

If it return no record(s) Found, you need use POST to create new record by:

```bash
curl https://maybe.xcv58.me/maybe-api-v1/devices -d '{"deviceid": "001", "gcmid" : "you-id" }'
```
It's the url with json data:
```bash
https://maybe.xcv58.me/maybe-api-v1/devices
```
with data
```json
{
  "deviceid": "001",
  "gcmid" : "your-id"
}
```

**Otherwise** you need update it by PUT:

```bash
curl -X PUT -d '{"$set":{"gcmid" : "your-id"}}' https://maybe.xcv58.me/maybe-api-v1/devices/{deviceid}
```
