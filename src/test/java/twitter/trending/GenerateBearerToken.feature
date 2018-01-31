Feature: Get Bearer Token

Background:

Scenario: oauth 2 flow

* url 'https://api.twitter.com/oauth2/token'
* header Authorization = "Basic " + baseKey
* header Content-Type = 'application/x-www-form-urlencoded;charset=UTF-8'
* header Accept-Encoding = 'gzip'
* form field grant_type = 'client_credentials'
* method post
* status 200
